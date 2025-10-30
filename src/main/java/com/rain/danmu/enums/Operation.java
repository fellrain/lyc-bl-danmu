package com.rain.danmu.enums;

/**
 * B站弹幕协议操作码枚举
 */
public enum Operation {
    /**
     * 客户端发送的心跳包(30秒发送一次)
     */
    HEARTBEAT(2),
    
    /**
     * 服务器收到心跳包的回复
     */
    HEARTBEAT_REPLY(3),
    
    /**
     * 服务器推送的弹幕消息包
     * 包含实际的弹幕内容
     */
    SEND_SMS_REPLY(5),
    
    /**
     * 客户端发送的鉴权包(客户端发送的第一个包)
     * 用于向服务器证明身份，获得接收弹幕的权限
     */
    AUTH(7),
    
    /**
     * 服务器收到鉴权包后的回复
     */
    AUTH_REPLY(8);
    
    // 操作码
    public final int code;

    Operation(int code) {
        this.code = code;
    }

    /**
     * 根据操作码解析对应的枚举
     */
    public static Operation parse(int code) {
        return switch (code) {
            case 2 -> Operation.HEARTBEAT;
            case 3 -> Operation.HEARTBEAT_REPLY;
            case 5 -> Operation.SEND_SMS_REPLY;
            case 7 -> Operation.AUTH;
            case 8 -> Operation.AUTH_REPLY;
            default -> throw new IllegalStateException("Unexpected value: " + code);
        };
    }
}