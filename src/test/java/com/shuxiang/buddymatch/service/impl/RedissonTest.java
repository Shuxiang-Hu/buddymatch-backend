package com.shuxiang.buddymatch.service.impl;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    public void test(){
        //list
        RList<String> list = redissonClient.getList("test-list");
        list.add("huhu");
        System.out.println(list.get(0));
        //map

        //set

        //stack
    }
}


