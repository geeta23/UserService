package com.example.UseService.controller;

import com.example.UseService.DTO.LoginRequestDTO;
import com.example.UseService.DTO.UserDTO;
import com.example.UseService.DTO.SignUpRequestDTO;
import com.example.UseService.DTO.ValidateRequestDTO;
import com.example.UseService.Service.AuthService;
import com.example.UseService.model.User;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// sends and gives response in JSON for generic response use @Controller
// @RestController will have response as JSON
@RestController
public class AuthController {
    //sign up
    // login
    // logout
    // forget password
    @Autowired
    private AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<UserDTO> signUp (@RequestBody SignUpRequestDTO signUpRequestDTO){
        User user = authService.signUp(signUpRequestDTO.getEmail(), signUpRequestDTO.getPassword());
        UserDTO userDTO = getUserDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){
        try {
            Pair<User, MultiValueMap<String, String>> bodyWithHeaders = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            UserDTO userDTO = getUserDTO(bodyWithHeaders.a);
            return new ResponseEntity<>(userDTO, bodyWithHeaders.b, HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        }
    }

    @PostMapping("/auth/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody ValidateRequestDTO validateRequestDTO){
        Boolean isValid = authService.validateToken(validateRequestDTO.getToken(), validateRequestDTO.getUserId());
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }

    private UserDTO getUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setRoles(user.getRoles());

        return userDTO;
    }
}
