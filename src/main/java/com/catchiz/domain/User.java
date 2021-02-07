package com.catchiz.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class User {
    private int id;

    private String username;

    private String password;

    private String email;

    private Timestamp registerDate;

    private int isManager;

}
