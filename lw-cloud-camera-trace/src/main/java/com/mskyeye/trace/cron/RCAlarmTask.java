package com.mskyeye.trace.cron;

import cn.hutool.core.util.ObjectUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.trace.model.LwCameraStatusPacket;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzAlarmEvent;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.proc.DhCameraProc;
import com.mskyeye.trace.proc.GplCameraProc;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.service.IYzAlarmEventService;
import com.mskyeye.trace.utils.DisAndAngleUtils;
import com.mskyeye.trace.utils.RedisCache;
import com.mskyeye.trace.utils.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mskyeye.trace.common.GlResources.*;
import static java.lang.Math.toDegrees;

/**
 * @ClassName:RCAlarmTask
 * @Description:雷光警戒定时任务
 * @Author:R.Gong
 * @Date:2024/5/15 17:18
 * @Version:1.0
 **/
@Component
public class RCAlarmTask {

    private static final Logger log = LoggerFactory.getLogger(RCAlarmTask.class);
    @Autowired
    private HpCameraProc hpCameraProc;
    @Autowired
    private HkCameraProc hkCameraProc;
    @Autowired
    private DhCameraProc dhCameraProc;

    @Autowired
    private GplCameraProc gplCameraProc;

    @Autowired
    private IYzAlarmEventService iYzAlarmEventService;

    private Integer DIS_THROS = 10000;//相机探测距离阈值


    @Value("${alarm_stg_url}")
    private String alarmStgUrl;

    @Autowired
    private RedisCache redisCache;

    /**
     * 1000ms查询一次雷光警戒缓存，转换成雷光警戒目标
     */
    @Scheduled(fixedDelay = 500)
    public void targetHandle() throws Exception {
        //预警目标转换成跟踪目标
        if (!GL_RCAlarmMap.isEmpty()) {
            Iterator<Map.Entry<Long, LwTrackPacket>> iter = GL_RCAlarmMap.entrySet().iterator();
//            System.out.println("1 ---- 转换成雷光警戒目标 准备循环 "+GL_RCAlarmMap.size());
            while (iter.hasNext()) {
                Map.Entry<Long, LwTrackPacket> entry = iter.next();
                LwTrackPacket lwTrackPacket = entry.getValue();
                Content cnt = lwTrackPacket.getITEM().get(0);
                //TODO
                //1分钟还没有被分配相机则删除该预警目标
                if (System.currentTimeMillis() - lwTrackPacket.getTIME() > 5000) {
                    iter.remove();
//                    System.out.println("2 ---- 5秒之前的信息则删除该预警目标 "+cnt.getTID());
                    continue;
                }
                double minDis = 99999;
                YzCameraInfo cameraInfo = null;//选定雷光警戒的相机
//                System.out.println(" 3----循环准备 选定雷光警戒的相机 "+GL_RCAlarmMap.size());
                for (Map.Entry<Long, YzCameraInfo> entry1 : GL_CameraInfoMap.entrySet()) {
                    YzCameraInfo yzCameraInfo = entry1.getValue();
//                    System.out.println(" 4----进入循环 选定雷光警戒的相机计算 "+GL_RCAlarmMap.size());

                    //计算相机和预警目标的距离
                    double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                            cnt.getLAT(), cnt.getLON());
                    //相机必须处于雷光警戒状态  或者  警戒抓拍状态
                    if (dis < minDis && GL_TraceInfoMap.containsKey(yzCameraInfo.getId())
                            && GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType() == 6) {
//                        minDis = dis;
//                        System.out.println(" 5----循环内 选定雷光警戒的相机更新 "+GL_RCAlarmMap.size());
                        cameraInfo = yzCameraInfo;
                    }
                }
                //TODO
                //要满足小于10公里，否则表示没找到合适的相机
                /*if (minDis > DIS_THROS) {
                    continue;
                }*/
                TraceProInfo cached = redisCache.getCacheObject(ALERT_CAPTURE + cnt.getTID());
                if (cameraInfo != null && cached == null){
                    TraceProInfo traceProInfo = GL_TraceInfoMap.get(cameraInfo.getId());
                    traceProInfo.setTraceType(7);
                    traceProInfo.setTargetId(cnt.getTID());
                    traceProInfo.setTraceLat(cnt.getLAT());
                    traceProInfo.setTraceLon(cnt.getLON());
                    traceProInfo.setClock(0);
                    traceProInfo.setTargetWidth((double)cnt.getSIZEMETRES());
                    GL_TraceInfoMap.put(cameraInfo.getId(), traceProInfo);
                    System.out.println("6 ----匹配相机为 " + cameraInfo.getName()+ "新增目标id"+cnt.getTID());
                }
            }
        }
    }


    /**
     * 1000ms执行一次雷光警戒处理
     */
    @Scheduled(fixedDelay = 500)
    public void rcAlarmHandle() throws Exception {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newCachedThreadPool();
        }
        Iterator<Map.Entry<Long, TraceProInfo>> iter = GL_TraceInfoMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, TraceProInfo> entry = iter.next();
            TraceProInfo traceProInfo = entry.getValue();
            if(traceProInfo.getCameraId() == null){
                continue;
            }
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());

            if (!yzCameraInfo.isRcAlarmPaused()) {
                System.out.println("当前停止跟踪，跳过执行:"+yzCameraInfo.isRcAlarmPaused());
                return; // 当前停止跟踪，跳过执行
            }
            System.out.println("当前正在执行警戒拍照。。。。。。目标id："+traceProInfo.getTargetId());
            if (traceProInfo.getTraceType() == 7) {
                //缓存当前警戒抓拍的对象，防止10分钟内重复抓拍
                redisCache.setCacheObject(ALERT_CAPTURE+traceProInfo.getTargetId(),traceProInfo,10,TimeUnit.MINUTES);
                String[] urlArray = new String[2];
                Integer clock = traceProInfo.getClock();
                if (executor == null) {
                    executor = Executors.newCachedThreadPool();
                }
                //第1秒开启引导
                if (clock == 1) {
                    //相机转到该经纬度
//                    ctrlCameraByLonLat(traceProInfo);
//                    System.out.println("当前正在执行警戒拍照。。。。。。开启引导");
                    traceProInfo.setChannelId(redisCache.getCacheObject(yzCameraInfo.getLightCode()));
                    gplCameraProc.aiTrackCtrl(traceProInfo);
                    //缓存目标信息转发前端绘制相机指示线距离
                    redisCache.setCacheObject("LGJJ"+yzCameraInfo.getId(),traceProInfo,300,TimeUnit.SECONDS);
                    GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
                }
               /* //第3秒开始图像跟踪
                if (clock == 3) {
                    //和普相机开启智能跟踪机制用于辅助抓拍
                    if(yzCameraInfo.getManu().equals("hp")){
//                        if (traceProInfo.getChannelId() == 1) {
//                            traceProInfo.setChannelId(2);
//                        } else if (traceProInfo.getChannelId() == 2) {
//                            traceProInfo.setChannelId(1);
//                        }

                        hpCameraProc.photoTrackingCtrl(yzCameraInfo, false, traceProInfo.getChannelId());
                        //下面这个操作必须添加
                        hpCameraProc.boxTrackCtrl(yzCameraInfo, false, 1, 0, 0, 0, 0);
                        //发送图像跟踪指令
                        hpCameraProc.photoTrackingCtrl(yzCameraInfo, traceProInfo.getbTracking(), 2);
                    }
                }*/
                //第5秒开始录制视频
                if (clock == 5) {
//                    System.out.println("当前正在执行警戒拍照。。。。。。录制");
                    urlArray = alertCapVideo(yzCameraInfo, traceProInfo);
                    traceProInfo.setAlarmAbsUrl(urlArray[0]);
                    traceProInfo.setAlarmRelUrl(urlArray[1]);
                }
                //第50秒开始抓拍照片
                if (clock == 50) {
                    if (traceProInfo.getAlarmAbsUrl() != null) {
                        alertCapPhoto(yzCameraInfo, traceProInfo);
                    }
                }
                //第60秒结束取证
                if (clock == 60) {
                    clock = 0;
                    traceProInfo.setTraceType(6);//返回雷光警戒
                    traceProInfo.setClock(clock);
                    GL_TraceInfoMap.put(traceProInfo.getCameraId(), traceProInfo);
                    hpCameraProc.photoTrackingCtrl(yzCameraInfo, false, traceProInfo.getChannelId());
                    //下面这个操作必须添加
                    hpCameraProc.boxTrackCtrl(yzCameraInfo, false, 1, 0, 0, 0, 0);
                    continue;
                }
                clock++;
                traceProInfo.setClock(clock);
                GL_TraceInfoMap.put(traceProInfo.getCameraId(), traceProInfo);
            }
        }
    }

    /**
     * 定时删除过期的取证材料
     *
     * @throws Exception
     */
    @Scheduled(cron = "0 0 6 * * ?") // 每天早上6点执行
    public void delExpEvi() throws Exception {
        String folderPath = alarmStgUrl;
        File folder = new File(folderPath);
        cleanFolders(folder);
    }



    private static void cleanFolders(File folder) {
        File[] subFolders = folder.listFiles();
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                if (subFolder.isDirectory()) {
                    long createTime = subFolder.lastModified();
                    Instant now = Instant.now();
                    Instant folderCreateTime = Instant.ofEpochMilli(createTime);
//                    long hours = Duration.between(folderCreateTime, now).toHours();
                    long days = Duration.between(folderCreateTime, now).toDays();

                    //默认保存三个月的资源
                    if (days > 30 * 3) {
                        try {
                            deleteFolder(subFolder);
                            System.out.println("Deleted folder: " + subFolder.getName());
                        } catch (IOException e) {
                            System.out.println("Failed to delete folder: " + subFolder.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // 递归删除子文件夹
                } else {
                    Files.delete(file.toPath()); // 删除文件
                }
            }
        }
        Files.delete(folder.toPath()); // 删除空文件夹
    }

    /**
     * 抓拍视频
     *
     * @param yzCameraInfo
     * @param traceProInfo
     */
    private String[] alertCapVideo(YzCameraInfo yzCameraInfo, TraceProInfo traceProInfo) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatted1 = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        String date1 = currentDateTime.format(formatted1);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String date2 = currentDateTime.format(formatter2);
        String[] urlArray = new String[2];

        //数据库存储地址
        String dbStorgePath = "/profile/alarm/" + date1 + yzCameraInfo.getName() + "_" + traceProInfo.getTargetId() + "_" + date2 + ".mp4";
        //绝对路径地址
        String outputPath = alarmStgUrl + date1;

        String fileName = yzCameraInfo.getName() + "_" + traceProInfo.getTargetId() + "_" + date2 + ".mp4";
        // 创建输出目录（如果不存在）
        File outputDirectory = new File(outputPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        String filePath = new File(outputDirectory, fileName).getAbsolutePath();
        urlArray[0] = filePath;
        urlArray[1] = dbStorgePath;

        LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Shanghai"));
        LocalTime start = LocalTime.of(19, 0); // 晚上7点
        LocalTime end = LocalTime.of(6, 0); // 第二天凌晨6点
        String rtspUrl;
        //晚上7点到第二天凌晨6点是热成像,其它时间是可见光
//        if (currentTime.isAfter(start) || currentTime.isBefore(end)) {
        //红外是2，可见光是1
        if(traceProInfo.getChannelId() == 2){
            rtspUrl = yzCameraInfo.getThermalRtsp();
        }else{
            rtspUrl = yzCameraInfo.getLightRtsp();
        }
        // 构建FFmpeg命令,抓拍时间为20s
        String[] ffmpegCommand = {
                "ffmpeg",
                "-rtsp_transport", "tcp",
                "-i", rtspUrl,
                "-t", "20",
                "-c:v", "copy",
                "-y",  // 覆盖现有文件
                filePath
        };
        Runnable task = () -> {
            // 执行FFmpeg命令
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand)
                    .redirectErrorStream(true); // 将错误输出和正常输出合并

            try {
                Process process = processBuilder.start();

                // 处理标准输出流
                StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");
                executor.execute(outputGobbler);

                // 处理标准错误流
                StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
                executor.execute(errorGobbler);

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("视频截取成功");
                } else {
                    System.out.println("视频截取失败");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        };
        // 提交任务给线程池执行
        executor.submit(task);
        //保存数据库
        return urlArray;
    }

    private void alertCapPhoto(YzCameraInfo yzCameraInfo, TraceProInfo traceProInfo) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatted1 = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        String date1 = currentDateTime.format(formatted1);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String date2 = currentDateTime.format(formatter2);


        //数据库存储地址
        String dbStorgePath = "/profile/alarm/" + date1 + yzCameraInfo.getName() + "_" + traceProInfo.getTargetId() + "_" + date2 + ".jpg";
        //绝对路径地址
        String outputPath = alarmStgUrl + date1;

        String fileName = yzCameraInfo.getName() + "_" + traceProInfo.getTargetId() + "_" + date2 + ".jpg";
        // 创建输出目录（如果不存在）
        File outputDirectory = new File(outputPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        String filePath = new File(outputDirectory, fileName).getAbsolutePath();
        try {
            for(int i = 1; i<= 3; ++i){
                String timeInfo = String.valueOf(i * 5);
                // 构建FFmpeg命令
                String[] ffmpegCommand = {
                        "ffmpeg",
                        "-ss", timeInfo,
                        "-i", traceProInfo.getAlarmAbsUrl(),
                        "-vframes", "1",
                        filePath
                };
                // 执行FFmpeg命令
                ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand)
                        .redirectErrorStream(true); // 将错误输出和正常输出合并
                Process process = processBuilder.start();

                InputStream in = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);  // 打印FFmpeg执行过程中的输出
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("照片抓拍成功");
                    break;
                } else {
                    System.out.println("照片抓拍失败");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //保存数据库
        YzAlarmEvent yzAlarmEvent = new YzAlarmEvent();
        yzAlarmEvent.setAlarmTime(new Date());
        yzAlarmEvent.setCameraName(yzCameraInfo.getName());
        yzAlarmEvent.setEventType(3L);//警戒抓拍
        yzAlarmEvent.setLat(traceProInfo.getTraceLat());
        yzAlarmEvent.setLon(traceProInfo.getTraceLon());
        yzAlarmEvent.setAiPointName("");
        yzAlarmEvent.setCameraId(yzCameraInfo.getId());
        yzAlarmEvent.setDeptId(yzCameraInfo.getDeptId());
        yzAlarmEvent.setVideoUrl(traceProInfo.getAlarmRelUrl());
        yzAlarmEvent.setPhotoUrl1(dbStorgePath);
        iYzAlarmEventService.insertYzAlarmEvent(yzAlarmEvent);//告警事件插入数据库
    }


    private Boolean ctrlCameraByLonLat(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        //偏移校准值
        double pCorVal = yzCameraInfo.getAngle();
        double tCorVal = yzCameraInfo.getCurTVal();
        double zFixVal = yzCameraInfo.getzVal();
        double height = yzCameraInfo.getHeight();
        //相对于相机的角度
        double dBear = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        //相对于相机的距离
        double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;
        //TODO 计算出的T值
        double tVal = -1 * toDegrees(Math.atan2(height, dis));
        if (yzCameraInfo.getManu().equals("gpl")) {
            tVal = toDegrees(Math.atan2(height, dis));
            tVal = tVal < 0 ? 0 : tVal;
        }
        if (yzCameraInfo.getManu().equals("hik")) {
            hkCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("dh")) {
            dhCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("hp")) {
            hpCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("gpl")) {
            gplCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal,traceProInfo.getChannelId());
        }
        return true;
    }
}
