package com.hoxinte.tool.clients.redis;

import lombok.Data;

import java.io.Serializable;

/**
 * Redis 队列请求消息
 *
 * @author dominate
 * @since 2022/09/14
 */
@Data
public class RedisMsgBody<T> implements Serializable {
    /**
     * 消息体
     */
    private T msg;
    /**
     * 消息key
     */
    private String key;

    public RedisMsgBody(T msg, String key) {
        this.key = key;
        this.msg = msg;
    }

}
