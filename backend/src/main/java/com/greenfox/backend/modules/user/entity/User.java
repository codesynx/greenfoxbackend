package com.greenfox.backend.modules.user.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User entity representing registered users in the system.
 * Authentication is phone-number based (OTP).
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_phone", columnList = "phone_number", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * User roles in the system.
     */
    public enum UserRole {
        ADMIN,
        USER
    }
}

