package com.hisense.gateway.library.stud.model;

import lombok.Data;

import java.util.List;

@Data
public class AccountSyn {
    private Long id;
    private String accessToken;
    private String orgName;
    private String username;
    private String email;
    private String password;
    private List<UserSyn> users;
}
