package com.rain.danmu.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * HTTP工具类
 */
public final class HttpUtil {

    private static final int BUFFER_SIZE = 8192;
    private static final String DEFAULT_ACCEPT_HEADER = "application/json";

    private HttpUtil() {
    }

    public static String get(String url, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL parsedUrl = new URL(url);
            connection = (HttpURLConnection) parsedUrl.openConnection();
            configureConnection(connection, headers);
            try (InputStream in = connection.getInputStream()) {
                return readStream(in);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!Objects.isNull(connection)) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream in) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder response = new StringBuilder();
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return response.toString();
    }

    private static void configureConnection(HttpURLConnection conn, Map<String, String> headers) throws IOException {
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setUseCaches(true);
        conn.setInstanceFollowRedirects(true);
        if (!CollUtil.isEmpty(headers)) {
            headers.forEach(conn::setRequestProperty);
        }
    }

    public static String encodeParam(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }


    public static String toQueryStr(Map<String,String> params) {
       return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

}
