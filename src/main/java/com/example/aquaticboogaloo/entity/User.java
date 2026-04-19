package com.example.aquaticboogaloo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // username + password

    // TODO: can be null but unique?
    @Column(unique = true)
    private String discordUserId;

    private String username;
    private String avatarUrl;

    public User(String discordUserId) {
        this.discordUserId = discordUserId;
    }
}
