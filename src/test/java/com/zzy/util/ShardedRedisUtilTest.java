package com.zzy.util;

import org.junit.Test;

import com.zzy.util.sharded.ShardedRedisUtil;

public class ShardedRedisUtilTest {
    @Test
    public void test() {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        System.out.println(redisUtil.get("keyTest"));
        System.out.println(redisUtil.set("keyTest","valueTest2"));
        System.out.println(redisUtil.get("keyTest"));
        redisUtil.destroy();
    }
}
