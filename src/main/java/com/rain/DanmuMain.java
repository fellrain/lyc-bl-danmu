package com.rain;

import com.rain.danmu.client.SimpleDanmuClient;
import com.rain.danmu.model.Auth;
import com.rain.danmu.model.Danmu;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 弹幕客户端示例程序
 */
public class DanmuMain {

    // B站登录凭证Cookie，用于获取弹幕服务器认证信息
    private static final String cookie = "";
    // 要监听的B站直播间房间号
    private static final int roomId = 400730;

    public static void main(String[] args) {
        // 1.第一种实现
        SimpleDanmuClient.with(danmu -> {
            // 收到弹幕消息时的回调
            System.out.println("[" + danmu.user.name + "]: " + danmu.body);
            // 使用房间号和Cookie连接到直播间
        }).connect(roomId, cookie);

        // 2.第二种实现
        SimpleDanmuClient client2 = SimpleDanmuClient.with(handler());
        // 使用房间号和Cookie连接到直播间
        client2.connect(roomId, cookie);

        // 3.第三种实现
        SimpleDanmuClient client3 = SimpleDanmuClient.with(handler());
        // 使用自定义Auth创建连接
        client3.connect(Auth.create(roomId, cookie));
        // 以上可任意组合
    }

    /**
     * 额外定义的方法处理逻辑
     */
    public static Consumer<Danmu> handler() {
        return danmu -> {
            // TODO 收到弹幕消息时的回调
            // 缺省的逻辑
        };
    }
}