package com.zzy.util.sharded;

import redis.clients.jedis.ShardedJedis;

// redis具体逻辑接口
public interface ShardedRedisExecutor<T> {
    T execute(ShardedJedis jedis);
}
