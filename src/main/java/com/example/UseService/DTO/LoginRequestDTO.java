package com.example.UseService.DTO;

import lombok.Getter;
import lombok.Setter;

//{
//        "email" : "madhu@gmail.com",
//        "password" : "geeta23"
//        }
@Getter
@Setter
public class LoginRequestDTO {
    private String email;
    private String password;
}
