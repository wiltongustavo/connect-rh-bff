package com.connectrh.bff.dto.response;

import lombok.Data;

@Data
public class UserCreateResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

}
