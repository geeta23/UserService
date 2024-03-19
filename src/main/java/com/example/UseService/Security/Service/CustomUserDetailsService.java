package com.example.UseService.Security.Service;

import com.example.UseService.Repositories.UserRepository;
import com.example.UseService.Security.Models.CustomUserDetails;
import com.example.UseService.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // instead of going to auth service it will first hit this service
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()){
            throw new UsernameNotFoundException("Not Found");
        }

        User user = optionalUser.get();
        System.out.println("I came here");

        return new CustomUserDetails(user);
    }
}
