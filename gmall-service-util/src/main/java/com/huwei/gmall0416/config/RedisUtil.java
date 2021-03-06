package com.huwei.gmall0416.config;

import com.alibaba.dubbo.config.annotation.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host,int port,int database){

        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
         //总数
         jedisPoolConfig.setMaxIdle(200);
         //获取连接时等待的最大毫秒
        // jedisPoolConfig.setMaxWaitMillis(10*1000);
         // 最少剩余数
         jedisPoolConfig.setMinIdle(10);
        //如果到最大数,设置等待
         jedisPoolConfig.setBlockWhenExhausted(true);
         //等待时间
        jedisPoolConfig.setMaxWaitMillis(2000);
        // 创建连接池
        jedisPool = new  JedisPool(jedisPoolConfig,host,port,20*1000);
    }
   //获取reids实例
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
