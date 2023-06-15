package com.shuxiang.buddymatch.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {


    private String host;

    private String port;
    @Bean
    public RedissonClient redisClient(){
        // 1. Create config object
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);


        // 2. Create Redisson instance

        // Sync and Async API
        RedissonClient redisson = Redisson.create(config);

//        // Reactive API
//        RedissonReactiveClient redissonReactive = redisson.reactive();
//
//        // RxJava3 API
//        RedissonRxClient redissonRx = redisson.rxJava();

//        // 3. Get Redis based implementation of java.util.concurrent.ConcurrentMap
//        RMap<MyKey, MyValue> map = redisson.getMap("myMap");
//
//        RMapReactive<MyKey, MyValue> mapReactive = redissonReactive.getMap("myMap");
//
//        RMapRx<MyKey, MyValue> mapRx = redissonRx.getMap("myMap");

        return redisson;

    }
}
