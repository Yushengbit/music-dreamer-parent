package com.musicdreamer.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置类
 *
 * <p>作用：把 application.yml 里 <code>music.file</code> 开头的配置自动绑定到这个类的字段上。
 * 以后想改保存位置，只改 yml，不用改代码。
 *
 * <p>对应关系（yml 的短横线命名 ↔ Java 的驼峰命名，Spring 会自动转换）：
 * <pre>
 * music:
 *   file:
 *     storage-path: ./uploads   →   storagePath
 *     access-prefix: /file      →   accessPrefix
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "music.file")
public class FileStorageProperties {

    /**
     * 文件保存的根目录（本地磁盘真实路径）
     * 相对路径（如 ./uploads）是相对「服务启动目录」生成的
     */
    private String storagePath = "./uploads";

    /**
     * 对外访问前缀，只用于拼成返回给前端的 URL（不含域名、不含端口）
     * 因为前端会经过网关 /file/** 路由过来，所以这里写 /file
     */
    private String accessPrefix = "/file";
}
