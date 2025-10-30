package com.rain.danmu.model;

/**
 * 弹幕实体
 */
public class Danmu {

    // 发送弹幕的用户信息
    public User user = new User();

    // 弹幕内容
    public String body;
}