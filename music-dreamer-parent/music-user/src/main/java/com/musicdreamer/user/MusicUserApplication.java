package com.musicdreamer.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.musicdreamer") // 扫描common模块的Bean
@MapperScan("com.musicdreamer.user.mapper")
public class MusicUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicUserApplication.class, args);
    }
}