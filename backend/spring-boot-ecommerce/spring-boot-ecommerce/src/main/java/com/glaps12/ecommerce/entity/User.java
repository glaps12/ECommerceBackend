package com.glaps12.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "enabled")
    private boolean enabled = false;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "date_created")
    @CreationTimestamp
    private Date dateCreated;

    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK, INSTAGRAM
    }
}
