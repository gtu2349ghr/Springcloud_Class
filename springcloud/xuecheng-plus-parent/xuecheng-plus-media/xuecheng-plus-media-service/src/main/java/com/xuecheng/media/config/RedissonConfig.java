package com.xuecheng.media.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 配置类
        Config config = new Config();
        // 添加Redis地址，这里添加单节点的地址，也可以使用 config.userClusterServers() 添加集群
        config.useSingleServer().setAddress("redis://192.168.117.128:6379")
                .setPassword("518610");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
