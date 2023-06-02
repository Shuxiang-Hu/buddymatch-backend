package com.shuxiang.buddymatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan("com.shuxiang.buddymatch.mapper")
@EnableOpenApi
@EnableSwagger2
@EnableScheduling
public class BuddyMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuddyMatchApplication.class, args);
    }

}
