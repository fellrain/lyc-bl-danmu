package com.rain.danmu.client;

import com.rain.danmu.model.Auth;
import com.rain.danmu.model.Danmu;

import java.util.function.Consumer;

/**
 * 弹幕客户端模板类
 */
public class SimpleDanmuClient {

    // 弹幕客户端实例
    private final DanmuClient danmuClient;

    // 认证信息
    private Auth auth;

    /**
     * 构造函数
     *
     * @param onDanmuReceived 弹幕接收回调函数
     */
    private SimpleDanmuClient(Consumer<Danmu> onDanmuReceived) {
        this.danmuClient = new DanmuClient(onDanmuReceived);
    }

    public static SimpleDanmuClient with(Consumer<Danmu> onDanmuReceived) {
        return new SimpleDanmuClient(onDanmuReceived);
    }

    /**
     * 使用Cookie创建认证信息并连接到指定直播间
     *
     * @param roomId 直播间房间号
     * @param cookie B站登录Cookie
     */
    public void connect(long roomId, String cookie) {
        try {
            this.auth = Auth.create(roomId, cookie);
            this.connect(auth);
        } catch (Exception e) {
            // 内部捕获异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用自定义认证信息连接到指定直播间
     *
     * @param auth 认证信息
     */
    public void connect(Auth auth) {
        this.auth = auth;
        this.danmuClient.connect(auth);
    }

    /**
     * 断开与弹幕服务器的连接
     */
    public void disconnect() {
        this.danmuClient.disconnect();
    }

    /**
     * 获取当前认证信息
     *
     * @return 当前认证信息
     */
    public Auth getAuth() {
        return auth;
    }
}