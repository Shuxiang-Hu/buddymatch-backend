package com.shuxiang.buddymatch.service.impl;


import com.shuxiang.buddymatch.model.domain.User;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void Test(){
        ;
        redisTemplate.opsForValue().set("huStr","String");
        redisTemplate.opsForValue().set("huInt",123);
        redisTemplate.opsForValue().set("huDouble",12.12);
        User user = new User();
        user.setUsername("HuX");
        user.setUserAccount("123123");
        user.setId(9999L);
        redisTemplate.opsForValue().set("huUser",user);

        System.out.println(redisTemplate.opsForValue().get("huStr"));
        System.out.println(redisTemplate.opsForValue().get("huUser"));

    }
}
