package com.zzy.util;

import org.junit.Test;

import com.zzy.util.hash.HashRedisUtil;

public class HashRedisUtilTest {
    @Test
    public void test() {
        HashRedisUtil redisUtil = HashRedisUtil.getInstance();
        System.out.println(redisUtil.set("keyTest","valueTest"));
        System.out.println(redisUtil.get("keyTest"));
        redisUtil.destroy();
    }
}
