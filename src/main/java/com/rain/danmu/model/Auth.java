package com.rain.danmu.model;

import com.rain.danmu.util.HttpUtil;
import com.rain.danmu.util.WbiSignUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * B站弹幕服务器认证信息类
 */
public class Auth {

    // 用户ID
    public final long uid;

    // 直播间房间号
    public final long roomid;

    // BUVID标识符，用于识别用户设备
    public final String buvid;

    // 连接弹幕服务器的密钥
    public final String key;

    /**
     * @param roomid 直播间房间号
     * @param uid    用户ID
     * @param buvid  设备标识符
     * @param key    连接密钥
     */
    public Auth(long roomid, long uid, String buvid, String key) {
        this.roomid = roomid;
        this.uid = uid;
        this.buvid = buvid;
        this.key = key;
    }

    /**
     * 使用cookie创建认证信息（适用于已登录用户）
     *
     * @param roomid 直播间房间号
     * @param cookie B站登录Cookie
     * @return Auth认证对象
     */
    public static Auth create(long roomid, String cookie) {
        try {
            String key = getKey(roomid, cookie),
                    buvid = extractCookieValue("buvid3", cookie);
            long uid = 0;
            try {
                // 提取用户ID，失败使用0
                String uidStr = extractCookieValue("DedeUserID", cookie);
                if (uidStr != null) {
                    uid = Long.parseLong(Objects.requireNonNull(uidStr));
                }
            } catch (Exception e) {
                // 忽略解析错误，使用默认值0
            }
            return new Auth(roomid, uid, buvid, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取弹幕服务器认证密钥
     * 通过调用B站API获取连接弹幕服务器所需的token
     *
     * @param roomId 直播间房间号
     * @param cookie Cookie
     * @return 弹幕服务器认证密钥
     */
    private static String getKey(long roomId, String cookie) throws IOException {
        // 使用WBI签名参数
        String signed;
        try {
            signed = WbiSignUtil.wbiSign(new HashMap<>() {{
                put("id", roomId);
                put("type", "0");
            }});
        } catch (Exception e) {
            throw new IOException("Failed to sign WBI parameters", e);
        }
        String res = HttpUtil.get("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?" + signed
                , Map.of("Accept", "*/*",
                        "Cookie", cookie,
                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
        return new JSONObject(res).getJSONObject("data").getString("token");
    }

    /**
     * 从Cookie中提取指定名称的值
     *
     * @param cookieName   Cookie键名
     * @param cookieString 完整的Cookie字符串
     * @return 对应的Cookie值，如果未找到则返回null
     */
    private static String extractCookieValue(String cookieName, String cookieString) {
        if (cookieString == null || cookieString.isEmpty()) {
            return null;
        }
        String[] cookies = cookieString.split(";");
        for (String cookie : cookies) {
            cookie = cookie.trim();
            if (cookie.startsWith(cookieName + "=")) {
                return cookie.substring(cookieName.length() + 1);
            }
        }
        return null;
    }
}