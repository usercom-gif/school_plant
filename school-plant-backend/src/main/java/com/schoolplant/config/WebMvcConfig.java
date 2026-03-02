package com.schoolplant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${school-plant.profile:./uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /profile/** 到本地文件目录
        File dir;
        if (uploadPath.startsWith("./") || uploadPath.startsWith("../")) {
            dir = new File(System.getProperty("user.dir"), uploadPath);
        } else {
            dir = new File(uploadPath);
        }
        
        String path = "file:" + dir.getAbsolutePath() + File.separator;
        registry.addResourceHandler("/profile/**").addResourceLocations(path);
    }
}
