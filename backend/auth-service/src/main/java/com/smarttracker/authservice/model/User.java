package com.smarttracker.authservice.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private ObjectId id;

    private String name;
    @Indexed(unique = true)
    private String email;
    private String password;

    private List<String> roles;

    @Builder.Default
    private boolean isVerified = false;

    private String verificationToken;

    private String resetToken;
    private Long resetTokenExpiry;
}
