//package com.DBP.ticketing_backend.global.config;
//
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RedissonConfig {
//
//    @Value("${spring.data.redis.host}")
//    private String host;
//
//    @Value("${spring.data.redis.port}")
//    private int port;
//
//    @Value("${spring.data.redis.password:}")
//    private String password;
//
//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//
//        // "redis://" 프로토콜 접두사는 필수
//        String address = "redis://" + host + ":" + port;
//
//        config.useSingleServer().setAddress(address);
//
//        // 비밀번호가 있는 경우에만 설정
//        if (password != null && !password.isEmpty()) {
//            config.useSingleServer().setPassword(password);
//        }
//
//        return Redisson.create(config);
//    }
//}
