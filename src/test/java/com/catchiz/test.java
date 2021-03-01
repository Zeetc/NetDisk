package com.catchiz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class test {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test(){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("hello","world");
        String hello=operations.get("hello");
        System.out.println(hello);
    }

}
