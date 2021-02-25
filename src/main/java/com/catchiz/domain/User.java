package com.catchiz.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class User {
    @ApiModelProperty(required = true,hidden = true)
    private Integer id;

    private String username;

    private String password;

    private String email;

    @ApiModelProperty(hidden = true)
    private Timestamp registerDate;

    @ApiModelProperty(hidden = true)
    private int isManager;

}
