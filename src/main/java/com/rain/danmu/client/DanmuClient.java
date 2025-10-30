package com.rain.danmu.client;

import com.rain.danmu.enums.Operation;
import com.rain.danmu.model.Auth;
import com.rain.danmu.model.Danmu;
import com.rain.danmu.model.Message;
import com.rain.danmu.model.Packet;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * B站弹幕客户端
 */
public class DanmuClient {

    // 弹幕服务器地址
    private final URI serverUri;

    // WebSocket客户端实例
    private WebSocketClient wsClient;

    // 弹幕接收回调函数
    private final Consumer<Danmu> onDanmuReceived;

    // 心跳定时器
    private Timer heartbeatTimer;

    /**
     * 构造函数，使用默认服务器地址
     *
     * @param onDanmuReceived 弹幕接收回调函数
     */
    public DanmuClient(Consumer<Danmu> onDanmuReceived) {
        this(URI.create("wss://broadcastlv.chat.bilibili.com:2245/sub"), onDanmuReceived);
    }

    /**
     * 构造函数，可指定服务器地址
     *
     * @param serverUri       服务器地址
     * @param onDanmuReceived 弹幕接收回调函数
     */
    public DanmuClient(URI serverUri, Consumer<Danmu> onDanmuReceived) {
        this.serverUri = serverUri;
        this.onDanmuReceived = onDanmuReceived;
    }

    /**
     * 连接到弹幕服务器
     *
     * @param auth 认证信息
     */
    public void connect(Auth auth) {
        if (wsClient != null) {
            disconnect();
        }
        // 创建WebSocket客户端实例
        wsClient = new WebSocketClient(serverUri) {
            /**
             * WebSocket连接成功时调用
             * @param handshakedata 握手数据
             */
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    // 发送认证包
                    JSONObject authJson = new JSONObject();
                    authJson.put("uid", auth.uid);
                    authJson.put("roomid", auth.roomid);
                    authJson.put("buvid", auth.buvid);
                    authJson.put("key", auth.key);
                    byte[] body = authJson.toString().getBytes(StandardCharsets.UTF_8);
                    send(new Packet(Operation.AUTH, body).pack());
                    // 启动心跳
                    startHeartbeat();
                } catch (Exception e) {
                    System.err.println("认证出错: " + e.getMessage());
                }
            }

            /**
             * WebSocket连接关闭时调用
             * @param code 关闭代码
             * @param reason 关闭原因
             * @param remote 是否由远程关闭
             */
            @Override
            public void onClose(int code, String reason, boolean remote) {
                stopHeartbeat();
            }

            /**
             * WebSocket发生错误时调用
             * @param ex 异常信息
             */
            @Override
            public void onError(Exception ex) {
            }

            /**
             * 接收到文本消息时调用（本系统不使用）
             * @param message 文本消息
             */
            @Override
            public void onMessage(String message) {
                // 不处理文本消息
            }

            /**
             * 接收到二进制消息时调用
             * B站弹幕系统使用二进制消息传输数据
             * @param bytes 二进制消息数据
             */
            @Override
            public void onMessage(ByteBuffer bytes) {
                // 处理二进制消息
                ArrayList<Packet> packets = Packet.unPack(bytes);
                packets.forEach(packet -> onPacket(packet));
            }
        };
        wsClient.connect();
    }

    /**
     * 断开与弹幕服务器的连接
     */
    public void disconnect() {
        if (wsClient != null) {
            wsClient.close();
            wsClient = null;
        }
        stopHeartbeat();
    }

    /**
     * 处理接收到的数据包
     *
     * @param packet 数据包
     */
    private void onPacket(Packet packet) {
        // 只处理弹幕消息包
        if (packet.operation == Operation.SEND_SMS_REPLY) {
            String bodyStr = new String(packet.body, StandardCharsets.UTF_8);
            Message message;
            try {
                JSONObject json = new JSONObject(bodyStr);
                message = new Message();
                message.cmd = json.optString("cmd", null);
                if (json.has("data")) {
                    message.data = json.getJSONObject("data");
                }
                if (json.has("info")) {
                    message.info = json.getJSONArray("info");
                }
            } catch (Exception ex) {
                System.err.println("解析消息出错: " + ex.getMessage());
                return;
            }

            // 处理弹幕消息
            if (!Objects.isNull(message.cmd) && message.cmd.equals("DANMU_MSG") && !Objects.isNull(message.info)) {
                try {
                    // 解析弹幕
                    Danmu danmu = new Danmu();
                    JSONArray infoArray = message.info;
                    danmu.user.uid = infoArray.getJSONArray(2).get(0).toString();
                    danmu.user.name = infoArray.getJSONArray(2).get(1).toString();
                    danmu.body = infoArray.getString(1);
                    onDanmuReceived.accept(danmu);
                } catch (Exception e) {
                    System.err.println("解析弹幕出错: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 启动心跳定时器
     */
    private void startHeartbeat() {
        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (wsClient != null && wsClient.isOpen()) {
                    wsClient.send(new Packet(Operation.HEARTBEAT, new byte[0]).pack());
                }
            }
            // 每30秒发送一次心跳
        }, 0, 30 * 1000);
    }

    /**
     * 停止心跳定时器
     */
    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }
    }
}