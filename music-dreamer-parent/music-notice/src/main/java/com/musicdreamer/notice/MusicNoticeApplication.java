package com.musicdreamer.notice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.musicdreamer") // 扫描common模块的Bean
@MapperScan("com.musicdreamer.notice.mapper")
public class MusicNoticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicNoticeApplication.class, args);
    }
}
