package com.mskyeye.trace.netty.control.service.GplCamera;

import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.model.YzCameraInfo;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

@Slf4j
public class GplWebSocketClient extends org.java_websocket.client.WebSocketClient {

    private final Long cameraId;

    /** 是否人为关闭 */
    private final AtomicBoolean manuallyClosed = new AtomicBoolean(false);

    /** 是否正在重连（防止重复重连） */
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    /** 重试次数（用于指数退避） */
    private volatile int retryCount = 0;

    private static final int CONNECT_TIMEOUT_SEC = 5;
    private static final int MAX_RECONNECT_DELAY_MS = 60000;

    /** 客户端缓存 */
    private static final Map<Long, GplWebSocketClient> CLIENT_MAP = new ConcurrentHashMap<>();

    /** 调度线程池 */
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "gpl-ws-reconnect");
                t.setDaemon(true);
                return t;
            });

    // ==================== 创建/获取 ====================

    public static GplWebSocketClient getOrCreate(Long cameraId, String ip, Long port) {
        if (cameraId == null || ip == null || port == null) {
            log.error("创建WebSocket参数为空: cameraId={}, ip={}, port={}", cameraId, ip, port);
            return null;
        }

        return CLIENT_MAP.compute(cameraId, (key, existing) -> {

            if (existing != null && existing.isOpen() && !existing.manuallyClosed.get()) {
                return existing;
            }

            if (existing != null) {
                existing.manuallyClosed.set(true);
                existing.close();
            }

            try {
                String wsUrl = String.format("ws://%s:%d", ip, port);
                GplWebSocketClient client = new GplWebSocketClient(cameraId, new URI(wsUrl));
                client.setConnectionLostTimeout(30);

                boolean ok = client.connectBlocking(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);
                if (!ok) {
                    log.error("WebSocket连接超时: cameraId={}", cameraId);
                    return null;
                }

                log.info("WebSocket创建成功: cameraId={}, url={}", cameraId, wsUrl);
                return client;

            } catch (Exception e) {
                log.error("创建WebSocket失败: cameraId={}, error={}", cameraId, e.getMessage());
                return null;
            }
        });
    }

    public static GplWebSocketClient get(Long cameraId) {
        if (cameraId == null) return null;

        GplWebSocketClient client = CLIENT_MAP.get(cameraId);
        if (client == null || !client.isOpen() || client.manuallyClosed.get()) {
            return null;
        }
        return client;
    }

    public static void remove(Long cameraId) {
        if (cameraId == null) return;

        GplWebSocketClient client = CLIENT_MAP.remove(cameraId);
        if (client != null) {
            client.manuallyClosed.set(true);
            client.close();
            log.info("手动关闭连接: cameraId={}", cameraId);
        }
    }

    // ==================== 发送 ====================

    public static boolean sendCommand(Long cameraId, JSONObject json) {

        if (cameraId == null || json == null) {
            log.error("发送参数为空");
            return false;
        }

        GplWebSocketClient client = get(cameraId);

        if (client == null) {
            log.warn("连接不存在，尝试重建: cameraId={}", cameraId);

            YzCameraInfo info = GL_CameraInfoMap.get(cameraId);
            if (info == null) {
                log.error("摄像头信息不存在: cameraId={}", cameraId);
                return false;
            }

            client = getOrCreate(cameraId, info.getAiIp(), info.getAiPort());
            if (client == null) {
                return false;
            }
        }

        try {
            if (!client.isOpen()) {
                log.warn("连接未打开，发送失败: cameraId={}", cameraId);
                return false;
            }

            String msg = json.toJSONString();
            client.send(msg);

            log.info("发送成功: cameraId={}, msg={}", cameraId, msg);
            return true;

        } catch (Exception e) {
            log.error("发送失败: cameraId={}, error={}", cameraId, e.getMessage());

            client.manuallyClosed.set(false);
            client.close(); // 触发重连

            return false;
        }
    }

    // ==================== 构造 ====================

    private GplWebSocketClient(Long cameraId, URI serverUri) {
        super(serverUri);
        this.cameraId = cameraId;
    }

    // ==================== 生命周期 ====================

    @Override
    public void onOpen(ServerHandshake handshake) {
        manuallyClosed.set(false);
        reconnecting.set(false);
        retryCount = 0;

        CLIENT_MAP.put(cameraId, this);

        log.info("连接成功: cameraId={}, remote={}", cameraId, getRemoteSocketAddress());
    }

    @Override
    public void onMessage(String message) {
        log.debug("收到消息: cameraId={}, msg={}", cameraId, message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

        log.info("连接关闭: cameraId={}, code={}, reason={}, remote={}",
                cameraId, code, reason, remote);

        if (!manuallyClosed.get()) {
            scheduleReconnect();
        } else {
            CLIENT_MAP.remove(cameraId);
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket异常: cameraId={}, error={}", cameraId, ex.getMessage());
    }

    // ==================== 重连 ====================

    private void scheduleReconnect() {

        if (!reconnecting.compareAndSet(false, true)) {
            return;
        }

        long delay = Math.min(MAX_RECONNECT_DELAY_MS, (1L << retryCount) * 1000L);
        retryCount++;

        log.info("准备重连: cameraId={}, delay={}ms", cameraId, delay);

        SCHEDULER.schedule(() -> {

            if (manuallyClosed.get() || isOpen()) {
                reconnecting.set(false);
                return;
            }

            Future<Boolean> future = SCHEDULER.submit(this::reconnectBlocking);

            boolean success = false;

            try {
                success = future.get(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.error("重连超时: cameraId={}", cameraId);
                future.cancel(true);
            } catch (Exception e) {
                log.error("重连异常: cameraId={}, error={}", cameraId, e.getMessage());
            }

            if (success) {
                log.info("重连成功: cameraId={}", cameraId);
                CLIENT_MAP.put(cameraId, this);
                retryCount = 0;
            } else {
                log.warn("重连失败: cameraId={}", cameraId);
                reconnecting.set(false);
                scheduleReconnect(); // 继续重试
            }

        }, delay, TimeUnit.MILLISECONDS);
    }

    // ==================== 释放 ====================

    public static void destroy() {
        log.info("关闭WebSocket管理器");

        SCHEDULER.shutdown();

        CLIENT_MAP.values().forEach(client -> {
            client.manuallyClosed.set(true);
            client.close();
        });

        CLIENT_MAP.clear();
    }
}