package com.example.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web 相关配置：把"上传目录"暴露成可访问的静态资源。
 *
 * <p>为什么要这一步？Spring Boot 默认只把 classpath 下的 static/ 当静态资源。
 * 壁纸是上传到项目目录里的 uploads/ 文件夹，必须显式告诉 Spring：
 * "收到 /uploads/xxx.jpg 的请求时，去磁盘上的 uploads/ 目录找文件"。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        // 结尾必须带 /，否则 Spring 会当成文件而不是目录
        String location = "file:" + dir.toString().replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
