package com.catchiz.service;

import com.catchiz.domain.User;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService {
    List<User> getAllUser();

    int register(User user) throws SQLIntegrityConstraintViolationException;

    User login(User user);

    boolean delUser(int userId);

    User managerLogin(User user);
}
