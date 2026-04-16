package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDiscordUserId(String discordUserId);
}
