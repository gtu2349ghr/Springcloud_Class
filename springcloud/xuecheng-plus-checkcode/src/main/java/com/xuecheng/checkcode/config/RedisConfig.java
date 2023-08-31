package com.xuecheng.checkcode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     Title: getRedisTemplate

     * <p>Description: 构造StringRedisTemplate
     * @param factory redis连接工厂
     * @return org.springframework.data.redis.core.StringRedisTemplate
     * @author HTP
     * @since 2022/2/20 21:59
     */
    @Bean
    public StringRedisTemplate getRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate =new StringRedisTemplate(factory);
        stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return stringRedisTemplate;
    }
}