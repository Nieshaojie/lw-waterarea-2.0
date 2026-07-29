package com.mskyeye.handler.service;

import com.mskyeye.common.utils.DistanceBetweenLonAndLat;
import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.model.*;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.mskyeye.handler.common.GlobalResources.*;

/**
 * @ClassName: DockAlarmService
 * @Description: 搭靠预警服务
 *  计算对象：仅 AIS 真实/外推 + 雷达融合目标
 *  告警规则：配对两艘船舶中必须包含一艘【外籍轮船（外轮）】才触发搭靠预警
 *  计算优化：仅外籍轮船主动发起测距，遍历同站点所有 AIS+融合目标；国内船舶/无效船舶仅清理缓存，不执行测距计算
 * @Author: R.Gong
 * @Date: 2026/7/22
 * @Version: 2.1
 **/
@Service
public class DockAlarmService {

    private static final Logger log = LoggerFactory.getLogger(DockAlarmService.class);

    /** 距离计算工具（静态复用，避免频繁实例化） */
    private static final DistanceBetweenLonAndLat DOCK_DISTANCE_UTIL = new DistanceBetweenLonAndLat();

    // ========== 目标类型常量 ==========
    private static final Integer DEL_TARGET = 0;
    private static final Integer AIS_TARGET = 1;
    private static final Integer AIS_FWD_PRETARGET = 3;
    private static final Integer AIS_NEG_PRTARGET = 4;
    private static final Integer MERGE_TARGET = 2;

    // ========== 时效配置 ==========
    /** 真实AIS超时时间（毫秒） */
    private static final long AIS_REAL_TIMEOUT_MS = 10000L;
    /** 外推AIS超时时间（毫秒）— 外推坐标漂移快，缩短时效 */
    private static final long AIS_EXTRAPOLATED_TIMEOUT_MS = 3000L;
    /** 融合目标超时时间（毫秒） */
    private static final long MERGE_TIMEOUT_MS = 10000L;
    /** 已告警配对保留时长（毫秒）— 10分钟 */
    private static final long ALARMED_PAIR_KEEP_MS = 600000L;
    /** 未告警配对无更新清理时长（毫秒）— 30秒无更新视为失效 */
    private static final long UNALARMED_PAIR_EXPIRE_MS = 30000L;
    /** MMSI融合缓存刷新间隔（毫秒） */
    private static final long MMSI_CACHE_REFRESH_MS = 10000L;

    // ========== MMSI融合缓存（减少全量遍历，提升性能）==========
    private volatile Set<Long> mergedMmsiCache = Collections.emptySet();
    private volatile long mmsiCacheLastUpdate = 0L;

    // ========== 国内船 MMSI 前缀 ==========
    private static final int MMSI_PREFIX_412 = 412;
    private static final int MMSI_PREFIX_413 = 413;
    private static final int MMSI_PREFIX_414 = 414;

    /**
     * 搭靠预警检测入口
     * @param lwTrackPacket 当前航迹包
     * @return 满足搭靠预警返回【搭靠预警】，无搭靠返回空字符串
     */
    public String checkDockAlarm(LwTrackPacket lwTrackPacket) {
        // 全局开关关闭直接返回
        if (!GlobalResources.dockAlarmEnable) {
            return "";
        }
        // 空报文防护
        if (lwTrackPacket == null || lwTrackPacket.getITEM() == null || lwTrackPacket.getITEM().isEmpty()) {
            return "";
        }
        Content cnt = lwTrackPacket.getITEM().get(0);
        if (cnt == null || cnt.getSOURCE() == null || cnt.getTID() == null) {
            return "";
        }

        Integer source = cnt.getSOURCE();
        Long curTid = cnt.getTID();

        // 前置过滤：仅 AIS（真实/外推）和融合目标参与搭靠体系
        if (!isDockAlarmTarget(source)) {
            if (Objects.equals(cnt.getSTATUS(), DEL_TARGET)) {
                cleanDockPairByTargetId(curTid);
            }
            return "";
        }

        // 删除目标：清空该目标所有关联配对缓存
        if (Objects.equals(cnt.getSTATUS(), DEL_TARGET)) {
            cleanDockPairByTargetId(curTid);
            return "";
        }

        // 基础字段空值校验
        Integer stationId = cnt.getSTATIONID();
        Double curLon = cnt.getLON();
        Double curLat = cnt.getLAT();
        Long curMmsi = cnt.getMMSI();
        if (stationId == null || curLon == null || curLat == null) {
            return "";
        }

        long now = System.currentTimeMillis();

        // AIS原始目标已融合，则跳过，避免融合目标与原始AIS重复计算
        if (isAisSource(source) && isMmsiMerged(curMmsi)) {
            return "";
        }

        // ============================================================
        // 核心逻辑：仅【外籍轮船】执行搭靠测距；国内船舶仅清理过期配对，不计算
        // ============================================================
        if (!isForeignVessel(curMmsi)) {
            cleanExpiredDockPairByTarget(curTid, now, stationId);
            return "";
        }

        // 当前船舶为外籍轮船，执行全站点船舶搭靠判定
        ShipTarget foreignShip = buildShipTarget(curTid, curMmsi, curLon, curLat, stationId, source);
        List<ShipTarget> stationShips = collectStationDockShips(stationId, now);

        boolean triggerDockAlarm = false;
        long timeThreshold = GlobalResources.dockTimeThreshold * 1000L;

        // 距离阈值校验
        Double distanceThreshold = GlobalResources.dockDistanceThreshold;
        if (distanceThreshold == null) {
            log.warn("搭靠距离阈值未配置，跳过本次计算，站点:{} MMSI:{}", stationId, curMmsi);
            return "";
        }
        double thresholdVal = distanceThreshold;

        // 外籍轮船 与 同站点所有有效船舶逐一测距
        for (ShipTarget otherShip : stationShips) {
            // 跳过自身
            if (otherShip.getTargetId().equals(curTid)) {
                continue;
            }
            // MMSI相同判定为同一艘船，不计算
            if (isSameShipByMmsi(foreignShip.getMmsi(), otherShip.getMmsi())) {
                continue;
            }

            String pairKey = generatePairKey(curTid, otherShip.getTargetId());
            double dist = DOCK_DISTANCE_UTIL.get_distance(
                    foreignShip.getLon(), foreignShip.getLat(),
                    otherShip.getLon(), otherShip.getLat()
            );

            // 超出搭靠距离，移除配对缓存
            if (dist > thresholdVal) {
                GlobalResources.dockAlarmPairMap.remove(pairKey);
                continue;
            }

            // 原子更新配对缓存，并发安全
            boolean newlyAlarmed = updateDockPairAtomic(pairKey, foreignShip, otherShip, dist, now, timeThreshold);
            if (newlyAlarmed) {
                triggerDockAlarm = true;
                log.info("【搭靠预警触发】外籍轮船MMSI={} 对接船舶MMSI={} 距离={}米 站点={}",
                        curMmsi, otherShip.getMmsi(), String.format("%.1f", dist), stationId);
            }
        }

        // 全局清理站点过期、失效配对，防止内存溢出
        cleanExpiredDockPair(now, stationShips, stationId);

        return triggerDockAlarm ? "搭靠预警" : "";
    }

    // ============================================================
    // 船舶国籍判断：是否外籍轮船（外轮）
    // 国内船MMSI前缀：412 / 413 / 414，其余判定为外籍轮船
    // ============================================================
    /**
     * 判断是否为外籍轮船（外轮）
     * @param mmsi 船舶9位MMSI
     * @return true=外籍轮船，false=国内船舶/无效MMSI
     */
    private boolean isForeignVessel(Long mmsi) {
        // MMSI为空、不足9位、超过9位均判定无效，不属于外籍轮船
        if (mmsi == null || mmsi < 100000000L || mmsi > 999999999L) {
            return false;
        }
        // 截取前三位MMSI前缀
        int prefix = (int) (mmsi / 1000000L);
        // 412/413/414 国内船，其余为外籍轮船
        return prefix != MMSI_PREFIX_412 && prefix != MMSI_PREFIX_413 && prefix != MMSI_PREFIX_414;
    }

    // ============================================================
    // 原子更新搭靠配对（ConcurrentHashMap compute 保证并发安全）
    // ============================================================
    private boolean updateDockPairAtomic(String pairKey, ShipTarget ship1, ShipTarget ship2,
                                         double dist, long now, long timeThreshold) {
        final boolean[] newlyAlarmed = {false};

        GlobalResources.dockAlarmPairMap.compute(pairKey, (key, pair) -> {
            if (pair == null) {
                // 新建配对记录
                pair = new DockAlarmPair();
                pair.setShipId1(Math.min(ship1.getTargetId(), ship2.getTargetId()));
                pair.setShipId2(Math.max(ship1.getTargetId(), ship2.getTargetId()));
                pair.setMmsi1(ship1.getMmsi());
                pair.setMmsi2(ship2.getMmsi());
                pair.setFirstCloseTime(now);
                pair.setLastUpdateTime(now);
                pair.setHasAlarm(false);
                pair.setLastDistance(dist);
                pair.setLat1(ship1.getLat());
                pair.setLon1(ship1.getLon());
                pair.setLat2(ship2.getLat());
                pair.setLon2(ship2.getLon());
                pair.setStationId(ship1.getStationId());
            } else {
                // 更新实时位置与距离
                pair.setLastDistance(dist);
                pair.setLat1(ship1.getLat());
                pair.setLon1(ship1.getLon());
                pair.setLat2(ship2.getLat());
                pair.setLon2(ship2.getLon());
                pair.setLastUpdateTime(now);

                // 持续贴近时长达标，首次触发告警
                long keepTime = now - pair.getFirstCloseTime();
                if (keepTime >= timeThreshold && !pair.getHasAlarm()) {
                    pair.setHasAlarm(true);
                    newlyAlarmed[0] = true;
                }
            }
            return pair;
        });

        return newlyAlarmed[0];
    }

    // ============================================================
    // 目标类型判断工具方法
    // ============================================================
    private boolean isDockAlarmTarget(Integer source) {
        if (source == null) {
            return false;
        }
        return Objects.equals(source, AIS_TARGET)
                || Objects.equals(source, AIS_FWD_PRETARGET)
                || Objects.equals(source, AIS_NEG_PRTARGET)
                || Objects.equals(source, MERGE_TARGET);
    }

    private boolean isAisSource(Integer source) {
        return Objects.equals(source, AIS_TARGET)
                || Objects.equals(source, AIS_FWD_PRETARGET)
                || Objects.equals(source, AIS_NEG_PRTARGET);
    }

    // ============================================================
    // MMSI去重：同一船舶不参与搭靠计算
    // ============================================================
    private boolean isSameShipByMmsi(Long mmsi1, Long mmsi2) {
        if (mmsi1 == null || mmsi2 == null) {
            return false;
        }
        return mmsi1.equals(mmsi2);
    }

    // ============================================================
    // MMSI融合缓存刷新逻辑
    // ============================================================
    private boolean isMmsiMerged(Long mmsi) {
        if (mmsi == null) {
            return false;
        }
        refreshMergedMmsiCacheIfNeeded();
        return mergedMmsiCache.contains(mmsi);
    }

    private void refreshMergedMmsiCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - mmsiCacheLastUpdate < MMSI_CACHE_REFRESH_MS) {
            return;
        }
        synchronized (this) {
            if (now - mmsiCacheLastUpdate < MMSI_CACHE_REFRESH_MS) {
                return;
            }
            Set<Long> mmsiSet = new HashSet<>();
            for (MergeTrackCache merge : mergeResultMap.values()) {
                if (merge != null && merge.getMerAisMmsi() != null) {
                    mmsiSet.add(merge.getMerAisMmsi());
                }
            }
            mergedMmsiCache = Collections.unmodifiableSet(mmsiSet);
            mmsiCacheLastUpdate = now;
        }
    }

    // ============================================================
    // 配对缓存清理工具
    // ============================================================
    private void cleanDockPairByTargetId(Long targetId) {
        if (targetId == null) {
            return;
        }
        ConcurrentHashMap<String, DockAlarmPair> pairMap = GlobalResources.dockAlarmPairMap;
        pairMap.entrySet().removeIf(entry -> {
            DockAlarmPair pair = entry.getValue();
            return pair != null
                    && (Objects.equals(pair.getShipId1(), targetId)
                    || Objects.equals(pair.getShipId2(), targetId));
        });
    }

    /**
     * 单船舶关联过期配对清理（国内船快速返回时调用）
     */
    private void cleanExpiredDockPairByTarget(Long curTid, long now, Integer stationId) {
        ConcurrentHashMap<String, DockAlarmPair> pairMap = GlobalResources.dockAlarmPairMap;
        if (pairMap.isEmpty() || curTid == null) {
            return;
        }
        pairMap.entrySet().removeIf(entry -> {
            DockAlarmPair pair = entry.getValue();
            if (pair == null) return true;
            if (!Objects.equals(pair.getStationId(), stationId)) return false;
            // 仅清理当前船舶相关配对
            if (!Objects.equals(pair.getShipId1(), curTid) && !Objects.equals(pair.getShipId2(), curTid)) {
                return false;
            }
            // 未告警30s无更新清理
            if (!pair.getHasAlarm() && pair.getLastUpdateTime() != null
                    && now - pair.getLastUpdateTime() > UNALARMED_PAIR_EXPIRE_MS) {
                return true;
            }
            // 已告警超过10分钟清理
            if (pair.getHasAlarm() && pair.getFirstCloseTime() != null
                    && now - pair.getFirstCloseTime() > ALARMED_PAIR_KEEP_MS) {
                return true;
            }
            return false;
        });
    }

    /**
     * 站点全局过期配对清理
     */
    private void cleanExpiredDockPair(long now, List<ShipTarget> validShips, Integer stationId) {
        ConcurrentHashMap<String, DockAlarmPair> pairMap = GlobalResources.dockAlarmPairMap;
        if (pairMap.isEmpty() || stationId == null) {
            return;
        }

        Set<Long> validTidSet = new HashSet<>((int) (validShips.size() / 0.75f) + 1);
        for (ShipTarget ship : validShips) {
            if (ship.getTargetId() != null) {
                validTidSet.add(ship.getTargetId());
            }
        }

        pairMap.entrySet().removeIf(entry -> {
            DockAlarmPair pair = entry.getValue();
            if (pair == null) return true;
            if (!Objects.equals(pair.getStationId(), stationId)) return false;
            // 告警配对按保留时长清理
            if (pair.getHasAlarm()) {
                return pair.getFirstCloseTime() != null
                        && now - pair.getFirstCloseTime() > ALARMED_PAIR_KEEP_MS;
            }
            // 未告警无更新清理
            if (pair.getLastUpdateTime() != null
                    && now - pair.getLastUpdateTime() > UNALARMED_PAIR_EXPIRE_MS) {
                return true;
            }
            // 任一船舶失效则清理配对
            boolean ship1Valid = pair.getShipId1() != null && validTidSet.contains(pair.getShipId1());
            boolean ship2Valid = pair.getShipId2() != null && validTidSet.contains(pair.getShipId2());
            return !ship1Valid || !ship2Valid;
        });
    }

    // ============================================================
    // 采集站点全部有效AIS+融合目标
    // ============================================================
    private List<ShipTarget> collectStationDockShips(Integer stationId, long now) {
        List<ShipTarget> result = new ArrayList<>();
        if (stationId == null) {
            return result;
        }

        refreshMergedMmsiCacheIfNeeded();

        // 拷贝快照遍历，防止并发修改异常
        // 1. AIS真实/外推目标
        for (AisTrackCache ais : new ArrayList<>(aisTrackMap.values())) {
            if (ais == null) continue;
            if (!Objects.equals(ais.getStationId(), stationId)) continue;
            if (ais.getTargetId() == null) continue;
            if (ais.getShipLon() == null || ais.getShipLat() == null) continue;
            // 已融合AIS跳过
            if (ais.getIMmsi() != null && mergedMmsiCache.contains(ais.getIMmsi())) continue;
            // 超时过滤
            long timeout = Boolean.TRUE.equals(ais.isExtrapolated())
                    ? AIS_EXTRAPOLATED_TIMEOUT_MS : AIS_REAL_TIMEOUT_MS;
            if (ais.getRefreshTime() == null || now - ais.getRefreshTime() > timeout) continue;

            Integer source = Boolean.TRUE.equals(ais.isExtrapolated())
                    ? AIS_FWD_PRETARGET : AIS_TARGET;
            result.add(buildShipTarget(ais.getTargetId(), ais.getIMmsi(),
                    ais.getShipLon(), ais.getShipLat(), stationId, source));
        }

        // 2. 雷达融合目标
        for (MergeTrackCache merge : new ArrayList<>(mergeResultMap.values())) {
            if (merge == null) continue;
            if (!Objects.equals(merge.getStationId(), stationId)) continue;
            if (merge.getMerRadarId() == null) continue;
            if (merge.getShipLon() == null || merge.getShipLat() == null) continue;
            if (merge.getRefreshTime() == null || now - merge.getRefreshTime() > MERGE_TIMEOUT_MS) continue;

            result.add(buildShipTarget(merge.getMerRadarId(), merge.getMerAisMmsi(),
                    merge.getShipLon(), merge.getShipLat(), stationId, MERGE_TARGET));
        }

        return result;
    }

    // ============================================================
    // 通用工具方法
    // ============================================================
    private ShipTarget buildShipTarget(Long tid, Long mmsi, Double lon, Double lat,
                                       Integer station, Integer source) {
        ShipTarget target = new ShipTarget();
        target.setTargetId(tid);
        target.setMmsi(mmsi);
        target.setLon(lon);
        target.setLat(lat);
        target.setStationId(station);
        target.setSource(source);
        return target;
    }

    /** 生成有序配对Key，避免正反重复 */
    private String generatePairKey(Long id1, Long id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }

    /**
     * 船舶简易载体，封装计算所需字段
     */
    private static class ShipTarget {
        private Long targetId;
        private Long mmsi;
        private Double lon;
        private Double lat;
        private Integer stationId;
        private Integer source;

        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public Long getMmsi() { return mmsi; }
        public void setMmsi(Long mmsi) { this.mmsi = mmsi; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Integer getStationId() { return stationId; }
        public void setStationId(Integer stationId) { this.stationId = stationId; }
        public Integer getSource() { return source; }
        public void setSource(Integer source) { this.source = source; }
    }
}