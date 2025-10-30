package com.rain.danmu.model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * B站弹幕消息实体
 */
public class Message {
    // 消息命令类型
    public String cmd;

    // 消息数据部分
    public JSONObject data;

    // 消息信息部分（弹幕消息中包含用户信息和弹幕内容）
    public JSONArray info;
    
    /**
     * 默认构造函数
     */
    public Message() {}
}