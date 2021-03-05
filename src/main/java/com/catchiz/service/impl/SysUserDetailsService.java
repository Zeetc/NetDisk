package com.catchiz.service.impl;

import com.catchiz.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SysUserDetailsService implements UserDetailsService {

    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.equals(""))throw new UsernameNotFoundException("用户ID不合法！");
        int userId;
        try{
            userId= Integer.parseInt(username);
        }catch (NumberFormatException e){
            throw new UsernameNotFoundException("用户ID不合法！");
        }
        com.catchiz.pojo.User user = userService.getUserById(userId);
        if (user == null){
            throw new UsernameNotFoundException("用户不存在！");
        }
        log.info("用户存在，用户:"+username);
        List<SimpleGrantedAuthority> userGrantedAuthorities = new ArrayList<>();
        userGrantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if(user.getIsManager())userGrantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        //返回数据对象(手动已加密)
        User.UserBuilder userBuilder = User.withUsername(username);
        userBuilder.password(user.getPassword());
        userBuilder.authorities(userGrantedAuthorities);
        return userBuilder.build();
    }
}