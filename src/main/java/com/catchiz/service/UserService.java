package com.catchiz.service;

import com.catchiz.pojo.User;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService {
    List<User> getAllUser();

    int register(User user) throws SQLIntegrityConstraintViolationException, IOException;

    boolean delUser(int userId);

    boolean resetPassword(Integer userId,String password);

    String getEmailById(int id);

    boolean checkEmailExist(String email);

    User getUserById(int id);

    boolean resetEmail(int userId, String email);

    boolean resetUsername(int userId, String username);

    int checkPassword(int userId, String password);
}
