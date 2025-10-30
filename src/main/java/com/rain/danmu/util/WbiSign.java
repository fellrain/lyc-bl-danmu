package com.rain.danmu.util;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * B站WBI签名工具类
 */
public class WbiSign {

    // WBI密钥混淆表，用于生成Mixin Key
    private static final int[] MIXIN_KEY_ENC_TAB = {46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52};

    // 缓存的WBI密钥
    private static volatile Map<String, String> cachedKeys = new ConcurrentHashMap<>(10);

    // 上次更新密钥的时间
    private static volatile LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1);

    // 密钥缓存有效期（分钟）
    private static final long CACHE_DURATION_MINUTES = 10;

    /**
     * 根据原始字符串生成Mixin Key
     */
    private static String getMixinKey(String orig) {
        StringBuilder sb = new StringBuilder();
        for (int index : MIXIN_KEY_ENC_TAB) {
            if (index < orig.length()) {
                sb.append(orig.charAt(index));
            }
        }
        return sb.substring(0, 32);
    }

    /**
     * MD5哈希函数
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 执行WBI签名
     */
    public static String encWbi(Map<String, Object> p, String imgKey, String subKey) {
        String mixinKey = getMixinKey(imgKey + subKey);
        long currTime = System.currentTimeMillis() / 1000;
        p.put("wts", currTime);
        Map<String, String> encodedParams = new TreeMap<>();
        p.forEach((k, v)
                -> encodedParams.put(HttpUtil.encodeParam(k), HttpUtil.encodeParam(v.toString())));
        String q = HttpUtil.toQueryStr(encodedParams);
        // 添加签名参数
        return q + "&w_rid=" + md5(q + mixinKey);
    }

    /**
     * 获取WBI密钥，带缓存机制
     *
     * @return 包含img_key和sub_key的映射
     */
    public static Map<String, String> getWbiKeys() {
        // 检查缓存是否有效
        if (isCacheValid()) {
            return cachedKeys;
        }
        synchronized (WbiSign.class) {
            try {
                if (isCacheValid()) {
                    return cachedKeys;
                }
                String res = HttpUtil.get("https://api.bilibili.com/x/web-interface/nav"
                        , Map.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", "Referer", "https://www.bilibili.com"));
                JSONObject root = new JSONObject(res),
                        wbiImg = root.getJSONObject("data").getJSONObject("wbi_img");
                String imgUrl = wbiImg.getString("img_url"),
                        subUrl = wbiImg.getString("sub_url");
                Map<String, String> result = new HashMap<>() {{
                    put("img_key", extractKeyFromUrl(imgUrl));
                    put("sub_key", extractKeyFromUrl(subUrl));
                }};
                if (200 == root.getInt("code")) {
                    // 临时缓存
                    cachedKeys = result;
                    lastUpdate = LocalDateTime.now();
                }
                return result;
            } catch (Exception e) {
                // 如果获取失败但有缓存，使用旧缓存
                if (!cachedKeys.isEmpty()) {
                    System.err.println("Failed to fetch new WBI keys, using cached keys: " + e.getMessage());
                    return cachedKeys;
                }
                throw e;
            }
        }
    }

    /**
     * 检查缓存是否有效
     *
     * @return 如果缓存有效返回true，否则返回false
     */
    private static boolean isCacheValid() {
        return !cachedKeys.isEmpty() &&
                LocalDateTime.now().isBefore(lastUpdate.plusMinutes(CACHE_DURATION_MINUTES));
    }

    /**
     * 从URL中提取密钥
     *
     * @param url 完整URL
     * @return 提取的密钥
     */
    private static String extractKeyFromUrl(String url) {
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf(".");
        return url.substring(start, end);
    }

    /**
     * WBI签名入口方法
     *
     * @param params 请求参数
     * @return 签名后的查询字符串
     */
    public static String wbiSign(Map<String, Object> params) {
        Map<String, String> keys = getWbiKeys();
        return encWbi(params, keys.get("img_key"), keys.get("sub_key"));
    }
}