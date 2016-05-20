package com.zzy.util.sharded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;

/**
 * 分片redis
 * 
 * @author zhengzhiyuan
 * @since May 20, 2016
 */
public class ShardedRedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisUtil.class);

    private static final String DEFAULT_REDIS_SEPARATOR = ";";

    private static final String HOST_PORT_SEPARATOR = ":";

    private ShardedJedisPool shardedJedisPool = null;

    private static final ShardedRedisUtil INSTANCE = new ShardedRedisUtil();

    private ShardedRedisUtil() {
        initialShardedPool();
    }

    private void initialShardedPool() {
        // 操作超时时间,默认2秒
        int timeout = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.timeout"), 2000);
        // jedis池最大连接数总数，默认8
        int maxTotal = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxTotal"), 8);
        // jedis池最大空闲连接数，默认8
        int maxIdle = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxIdle"), 8);
        // jedis池最少空闲连接数
        int minIdle = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.minIdle"), 0);
        // jedis池没有对象返回时，最大等待时间单位为毫秒
        long maxWaitMillis = NumberUtils.toLong(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxWaitTime"), -1);
        // 在borrow一个jedis实例时，是否提前进行validate操作
        boolean testOnBorrow = Boolean.parseBoolean(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.testOnBorrow"));

        // 设置jedis连接池配置
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(testOnBorrow);

        // 取得redis的url
        String redisUrls = SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.urls");
        if (redisUrls == null || redisUrls.trim().isEmpty()) {
            throw new IllegalStateException("the urls of redis is not configured");
        }
        LOGGER.info("the urls of redis is {}", redisUrls);

        // 生成连接池
        List<JedisShardInfo> shardedPoolList = new ArrayList<JedisShardInfo>();
        for (String redisUrl : redisUrls.split(DEFAULT_REDIS_SEPARATOR)) {
            String[] redisUrlInfo = redisUrl.split(HOST_PORT_SEPARATOR);
            JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrlInfo[0], Integer.parseInt(redisUrlInfo[1]), timeout);
            shardedPoolList.add(Jedisinfo);
        }

        // 构造池
        this.shardedJedisPool = new ShardedJedisPool(poolConfig, shardedPoolList, Hashing.MURMUR_HASH);
    }

    public static ShardedRedisUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 实现jedis连接的获取和释放，具体的redis业务逻辑由executor实现
     * 
     * @param executor RedisExecutor接口的实现类
     * @return
     */
    public <T> T execute(String key, ShardedRedisExecutor<T> executor) {
        ShardedJedis jedis = shardedJedisPool.getResource();
        T result = null;
        try {
            result = executor.execute(jedis);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public String set(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.set(key, value);
            }
        });
    }

    public String set(final String key, final String value, final String nxxx, final String expx, final long time) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }

    public String get(final String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public Boolean exists(final String key) {
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    public Long setnx(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }

    public String setex(final String key, final int seconds, final String value) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }

    public Long expire(final String key, final int seconds) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }

    public Long incr(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    public Long decr(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    public Long hset(final String key, final String field, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    public String hget(final String key, final String field) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public String hmset(final String key, final Map<String, String> hash) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }

    public List<String> hmget(final String key, final String... fields) {
        return execute(key, new ShardedRedisExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    public Long del(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.del(key);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) {
        return execute(key, new ShardedRedisExecutor<Map<String, String>>() {
            @Override
            public Map<String, String> execute(ShardedJedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    public void destroy() {
        this.shardedJedisPool.close();
    }
}
