package com.zzy.util.hash;

import redis.clients.jedis.Jedis;

// redis具体逻辑接口
public interface HashRedisExecutor<T> {
    T execute(Jedis jedis);
}
