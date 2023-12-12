package com.hoxinte.tool.clients.redis;

import org.junit.Test;

/**
 * @author dominate
 * @since 2022/9/14
 */
public class DebugRedisClient {

    @Test
    public void debugRedis() {
        String key = "test:redis:key";
        String value = "test:value";
        assert !RedisClient.hasKey(key);
        RedisClient.set(key, value);
        assert value.equals(RedisClient.get(key));
        assert 1 == RedisClient.removeKey(key);
    }
}
