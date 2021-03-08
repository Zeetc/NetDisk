package com.catchiz.service.impl;

import com.catchiz.controller.FileController;
import com.catchiz.pojo.User;
import com.catchiz.mapper.FileMapper;
import com.catchiz.mapper.UserMapper;
import com.catchiz.service.UserService;
import com.catchiz.utils.FileUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

@Service("userService")
@Transactional
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
    public int register(User user) throws DataIntegrityViolationException, SQLIntegrityConstraintViolationException, IOException {
        user.setId(null);
        user.setRegisterDate(new Timestamp(System.currentTimeMillis()));
        user.setIsManager(false);
        userMapper.register(user);
        int userId=user.getId();
        File file=new File(FileController.FILE_STORE_PATH +"\\"+userId);
        if(file.mkdir()){
            fileUtils.copyFileAndRename(userId);
            return userId;
        }
        userMapper.delUser(userId);
        return -1;
    }

    @Override
    public boolean delUser(int userId) {
        User user=userMapper.getUserById(userId);
        if(user.getIsManager())return false;
        fileMapper.delFileByUser(userId);
        userMapper.delUser(userId);
        return fileUtils.delFile(FileController.FILE_STORE_PATH +"\\"+userId,false)&&fileUtils.delUserIcon(userId);
    }

    @Override
    public boolean resetPassword(Integer userId, String password) {
        if(userId==null)return false;
        userMapper.resetPassword(userId, password);
        return true;
    }

    @Override
    public String getEmailById(int id) {
        return userMapper.getEmailById(id);
    }

    @Override
    public boolean checkEmailExist(String email) {
        Integer emailCount= userMapper.getEmailCount(email);
        return emailCount!=null&&emailCount!=0;
    }

    @Override
    public User getUserById(int id) {
        return userMapper.getUserById(id);
    }

    @Override
    public boolean resetEmail(int userId, String email) {
        return userMapper.resetEmail(userId,email)==1;
    }

    @Override
    public boolean resetUsername(int userId, String username) {
        return userMapper.resetUsername(userId,username)==1;
    }

    @Override
    public int checkPassword(int userId, String password) {
        return userMapper.checkPassword(userId,password);
    }

}
