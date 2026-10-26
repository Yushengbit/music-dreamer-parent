package com.musicdreamer.singer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.musicdreamer") // 扫描common模块的Bean
@MapperScan("com.musicdreamer.singer.mapper")
@EnableFeignClients(basePackages = "com.musicdreamer.singer.client")// 扫描feign接口
public class MusicSingerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicSingerApplication.class, args);
    }
}
