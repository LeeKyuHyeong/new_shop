package com.kh.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file:/// 형식으로 절대 경로 지정
        String resourceLocation = "file:///" + uploadDir.replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}