package com.example.UseService.Repositories;

import com.example.UseService.DTO.LoginRequestDTO;
import com.example.UseService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Optional because we can't get the user sometimes
    Optional<User> findByEmail(String email);
}
