package com.rain.danmu.model;

import com.rain.danmu.enums.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

/**
 * B站弹幕协议数据包类
 * 负责数据包的打包和解包操作
 * 充血模型
 * B站弹幕协议使用固定格式的数据包进行通信：
 * 1. 包长度（4字节）
 * 2. 头部长度（2字节）
 * 3. 协议版本（2字节）
 * 4. 操作码（4字节）
 * 5. 序列号（4字节）
 * 6. 数据体（变长）
 */
public class Packet {

    // 日志记录器
    static final Logger LOGGER = LoggerFactory.getLogger(Packet.class);

    // 数据包头部长度（固定16字节）
    public static final short HEADER_LENGTH = 16;

    // 序列号（固定为0）
    public static final int SEQUENCE_ID = 0;

    // 数据包体
    public final byte[] body;

    // 操作码
    public final Operation operation;

    /**
     * 协议版本
     * 0：Body中就是实际发送的数据
     * 2：Body中是经过压缩后的数据，请使用zlib解压，然后解析
     */
    public final short version = 0;

    /**
     * 构造函数
     *
     * @param operation 操作码
     * @param body      数据包体
     */
    public Packet(Operation operation, byte[] body) {
        this.operation = operation;
        this.body = body;
    }

    /**
     * 将数据包打包成ByteBuffer格式
     *
     * @return 打包后的ByteBuffer
     */
    public ByteBuffer pack() {
        int length = body.length + HEADER_LENGTH;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        // 包长度
        buffer.putInt(length);
        // 头部长度
        buffer.putShort(HEADER_LENGTH);
        // 协议版本
        buffer.putShort(version);
        // 操作码
        buffer.putInt(operation.code);
        // 序列号
        buffer.putInt(SEQUENCE_ID);
        // 数据体
        buffer.put(body);
        buffer.position(0);
        return buffer;
    }

    /**
     * 解包ByteBuffer数据
     *
     * @param buffer 待解包的ByteBuffer
     * @return 解包后的数据包列表
     */
    public static ArrayList<Packet> unPack(ByteBuffer buffer) {
        // 创建用于存储解析结果的列表
        ArrayList<Packet> packs = new ArrayList<>();
        // 初始化偏移量和数据长度
        int offset = 0;
        int len = buffer.limit();
        // 循环解析数据包，直到剩余数据不足一个包头长度
        while (offset + HEADER_LENGTH <= len) {
            // 从包头中读取数据体长度
            int bodyLen = buffer.getInt(offset) - HEADER_LENGTH;
            try {
                // 读取协议版本（偏移量6处的2字节）
                short ver = buffer.getShort(offset + 6);
                // 读取操作码（偏移量8处的4字节）
                Operation op = Operation.parse(buffer.getInt(8));
                // 创建数据体字节数组
                byte[] body = new byte[bodyLen];
                // 从缓冲区中提取数据体内容
                buffer.get(offset + HEADER_LENGTH, body, 0, bodyLen);
                // 判断协议版本
                if (ver == 2) {
                    // 版本2表示数据体是压缩的，需要解压
                    try {
                        body = decompress(body);
                        // 递归解析解压后的数据
                        packs.addAll(unPack(ByteBuffer.wrap(body)));
                    } catch (IOException e) {
                        LOGGER.error("解压失败", e);
                    }
                    // 跳过后续处理，继续下一个包
                    continue;
                }
                // 将解析出的数据包添加到列表中
                packs.add(new Packet(op, body));
            } finally {
                // 更新偏移量，指向下个数据包的开始位置
                offset += bodyLen + HEADER_LENGTH;
            }
        }
        return packs;
    }

    /**
     * 解压数据
     *
     * @param data 待解压的数据
     * @return 解压后的数据
     * @throws IOException IO异常
     */
    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        InflaterInputStream iis = new InflaterInputStream(inputStream);
        return iis.readAllBytes();
    }
}