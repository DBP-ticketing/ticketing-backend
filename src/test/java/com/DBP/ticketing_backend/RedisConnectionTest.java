//package com.DBP.ticketing_backend;
//
//import org.junit.jupiter.api.Test;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//
//@SpringBootTest
//@TestPropertySource(
//        properties = {
//            "jwt.secret=dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBqd3QgdGVzdGluZw==",
//            "jwt.access-token-expiration=3600000",
//            "jwt.refresh-token-expiration=86400000"
//        })
//public class RedisConnectionTest {
//
//    @Autowired private RedissonClient redissonClient;
//
//    @Test
//    void redisConnectionTest() {
//        // 간단한 데이터 저장 및 조회 테스트
//        redissonClient.getBucket("test-key").set("Hello Redis");
//
//        String value = (String) redissonClient.getBucket("test-key").get();
//
//        System.out.println("========================================");
//        System.out.println("Redis Test Result: " + value);
//        System.out.println("========================================");
//
//        assert value.equals("Hello Redis");
//    }
//}
