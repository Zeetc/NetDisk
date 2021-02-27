package com.catchiz.config;

import com.catchiz.interceptor.ManagerInterceptor;
import com.catchiz.interceptor.PrivilegeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    private final PrivilegeInterceptor privilegeInterceptor;
    private final ManagerInterceptor managerInterceptor;

    public MvcConfig(PrivilegeInterceptor privilegeInterceptor, ManagerInterceptor managerInterceptor) {
        this.privilegeInterceptor = privilegeInterceptor;
        this.managerInterceptor = managerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(privilegeInterceptor).addPathPatterns("/user/*","/file/*").excludePathPatterns("/user/login","/user/register");
        //registry.addInterceptor(managerInterceptor).addPathPatterns("/manager/*").excludePathPatterns("/manager/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                .allowedOrigins("*")  //也可以指定域名 .allowedOrigins("http://192.168.0.0:8080","http://192.168.0.1:8081")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
