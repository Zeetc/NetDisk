package com.catchiz.service;

import com.catchiz.domain.User;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService {
    List<User> getAllUser();

    int register(User user) throws SQLIntegrityConstraintViolationException, IOException;

    boolean delUser(int userId);

    User managerLogin(User user);

    boolean resetPassword(Integer userId,String password);

    boolean checkEmailExist(String email);

    User getUserById(int id);

    boolean resetEmail(int userId, String email);

    boolean resetUsername(int userId, String username);
}
