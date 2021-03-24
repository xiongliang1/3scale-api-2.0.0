package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class UserSynDto {
    private Long id;
    private String accessToken;
    private String orgName;
    private String username;
    private String email;
    private String password;
}
