package com.zzy.util.hash;

import java.util.ResourceBundle;

/**
 * 读取redis配置
 * 
 * @author zhengzhiyuan
 * @since May 20, 2016
 */
public class HashRedisConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "hashRedis";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);

    public static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
