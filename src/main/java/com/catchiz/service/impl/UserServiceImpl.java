package com.catchiz.service.impl;

import com.catchiz.controller.FileController;
import com.catchiz.domain.User;
import com.catchiz.mapper.FileMapper;
import com.catchiz.mapper.UserMapper;
import com.catchiz.service.UserService;
import com.catchiz.utils.FileUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


import java.io.File;
import java.sql.Timestamp;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {
    private final FileMapper fileMapper;
    private final UserMapper userMapper;
    private final FileUtils fileUtils;

    public UserServiceImpl(FileMapper fileMapper, UserMapper userMapper, FileUtils fileUtils) {
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
        this.fileUtils = fileUtils;
    }

    @Override
    public List<User> getAllUser() {
        return userMapper.getAllUser();
    }

    @Override
    public int register(User user) throws DataIntegrityViolationException, SQLIntegrityConstraintViolationException {
        user.setRegisterDate(new Timestamp(System.currentTimeMillis()));
        userMapper.register(user);
        int userId=user.getId();
        File file=new File(FileController.fileStorePath+"\\"+userId);
        file.mkdir();
        return userId;
    }

    @Override
    public User login(User user) throws EmptyResultDataAccessException{
        return userMapper.login(user);
    }

    @Override
    public void delUser(int userId) {
        User user=userMapper.getUserById(userId);
        if(user.getIsManager()==1)return;
        fileMapper.delFileByUser(userId);
        userMapper.delUser(userId);
        fileUtils.delFile(FileController.fileStorePath+"\\"+userId);
    }

    @Override
    public User managerLogin(User user) {
        return userMapper.managerLogin(user);
    }
}
