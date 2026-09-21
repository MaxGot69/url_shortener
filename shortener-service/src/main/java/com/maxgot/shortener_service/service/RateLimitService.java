package com.maxgot.shortener_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;
    private static final long LIMIT = 10; //(не больше 10 запросов)
    private static final long WINDOW_SECONDS = 60; //(за 60 секунд)

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRateLimited(String key){
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if(currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return currentCount != null && currentCount > LIMIT;
    }
}