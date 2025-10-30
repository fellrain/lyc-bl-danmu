package com.rain;

import com.rain.danmu.client.Auth;
import com.rain.danmu.client.DanmuClient;

/**
 * B站弹幕客户端主程序入口
 * 这是一个简单的示例程序，展示如何使用DanmuClient接收并显示B站直播间弹幕
 */
public class DanmuMain {
    // B站登录凭证Cookie，用于获取弹幕服务器认证信息
    private static final String cookie = "";
    // 要监听的B站直播间房间号
    private static final int roomId = 1;

    public static void main(String[] args) {
        try {
            // 创建弹幕客户端实例，设置接收到弹幕时的回调函数
            DanmuClient client = new DanmuClient(danmu -> {
                // 收到弹幕消息时的回调
                System.out.println("[" + danmu.user.name + "]: " + danmu.body);
            });
            // 使用房间号和Cookie创建认证信息
            Auth auth = Auth.create(roomId, cookie);
            // 连接
            client.connect(auth);
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }
}