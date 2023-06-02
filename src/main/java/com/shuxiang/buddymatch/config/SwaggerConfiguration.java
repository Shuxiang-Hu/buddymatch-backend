package com.shuxiang.buddymatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableOpenApi
@EnableSwagger2
@Profile("dev")
public class SwaggerConfiguration {

    @Bean
    public Docket createUserApi(){
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.shuxiang.buddymatch.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /*
    * 可以创建其他bean来配置其他分组
    * 与createUserApi()类似
    * */


    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("Buddy Match")
                .description("Description")
                .build();
    }

}

