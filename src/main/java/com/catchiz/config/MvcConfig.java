package com.catchiz.config;

import com.catchiz.interceptor.ManagerInterceptor;
import com.catchiz.interceptor.PrivilegeInterceptor;
import org.springframework.context.annotation.Configuration;
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
        registry.addInterceptor(privilegeInterceptor).addPathPatterns("/user/*","/file/*").excludePathPatterns("/user/login","/user/register");
        registry.addInterceptor(managerInterceptor).addPathPatterns("/manager/*").excludePathPatterns("/manager/login");
    }
}
