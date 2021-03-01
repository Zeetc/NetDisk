package com.catchiz.config;

import com.catchiz.handler.MyAccessDeniedHandler;
import com.catchiz.handler.MyAuthenticationEntryPoint;
import com.catchiz.service.impl.CustomAuthenticationProvider;
import com.catchiz.service.impl.SysUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Resource
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Resource
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Resource
    private MyAuthenticationEntryPoint myAuthenticationEntryPoint;

    @Resource
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Resource
    private SysUserDetailsService sysUserDetailsService;

    @Resource
    private AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource;

    /* 自己编写的认证逻辑替换系统自带认证 */
    @Resource
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /* 注册自己编写的认证逻辑 */
        auth.authenticationProvider(customAuthenticationProvider);
        auth.userDetailsService(sysUserDetailsService).passwordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return s.equals(charSequence.toString());
            }
        });
    }

    @Override
    public void configure(WebSecurity web) {
        /* 设置拦截忽略文件夹，可以对静态资源放行 */
        web.ignoring().antMatchers("/css/**", "/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin()
                .loginProcessingUrl("/auth/login")
                .usernameParameter("id")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                //.and()
                //.logout()
                //.logoutUrl("/auth/logout")
                //.logoutSuccessUrl("/login.html")
                /* 验证码 */
                .authenticationDetailsSource(authenticationDetailsSource)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/**", "/favicon.ico","/pages/**").permitAll()
                .anyRequest()
                /* 认证+验权处理 */
                /* 自定义认证类，因为禁用了session，抛弃了RULE验证 */
                .access("@rbacService.hasPermission(request)")
                .and()
                /* 禁用session（完全禁用）  */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();

        /* 注册自定义异常 */
        http.exceptionHandling().
                authenticationEntryPoint(myAuthenticationEntryPoint).
                accessDeniedHandler(myAccessDeniedHandler);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        //测试阶段，为了方便数据库维护，使用不加密的密码
        return NoOpPasswordEncoder.getInstance();
        //return new BCryptPasswordEncoder();
    }
}