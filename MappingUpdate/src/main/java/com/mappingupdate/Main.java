package com.mappingupdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 注解说明：开启 Spring Boot 自动配置和组件扫描
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(Main.class, args);
    }
}