package com.schoolplant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.schoolplant.mapper")
@EnableScheduling
@EnableAsync
public class SchoolPlantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolPlantApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  校园植物认养系统后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
