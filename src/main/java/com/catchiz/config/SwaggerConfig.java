package com.catchiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()// 通过.select()方法，去配置扫描接口,RequestHandlerSelectors配置如何扫描接口
                .apis(RequestHandlerSelectors.basePackage("com.catchiz.controller"))
                .build();
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("Catchiz", "https://github.com/Zeetc", "1375535806@qq.com");
        return new ApiInfo(
                "NetDisk", // 标题
                "小网盘  --by Catchiz", // 描述
                "v1.0", // 版本
                "https://github.com/Zeetc", // 组织链接
                contact, // 联系人信息
                "我自己许可我自己", // 许可
                "https://github.com/Zeetc", // 许可连接
                new ArrayList<>()// 扩展
        );
    }
}
