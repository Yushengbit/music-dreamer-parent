package com.musicdreamer.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@ComponentScan(basePackages = "com.musicdreamer") // 扫描 common 模块的 Bean（GlobalExceptionHandler、JwtUtil 等）
@MapperScan("com.musicdreamer.auth.mapper")
public class MusicAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicAuthApplication.class, args);
    }
}