package com.zzy.util;

import redis.clients.jedis.Jedis;

// redis具体逻辑接口
public interface RedisExecutor<T> {
    T execute(Jedis jedis);
}
