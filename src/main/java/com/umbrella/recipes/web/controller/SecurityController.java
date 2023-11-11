package com.umbrella.recipes.web.controller;

import com.umbrella.recipes.model.UserModel;
import com.umbrella.recipes.persistence.UserRepository;
import com.umbrella.recipes.web.dto.RegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user registration and authentication.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
public class SecurityController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the provided email and password.
     *
     * @param request The registration request containing email and password.
     * @return ResponseEntity with a success message if registration is successful.
     */
    @PostMapping(path = "/api/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest request) {
        if (repository.existsByUsername(request.email())) {
            log.error("User with email {} already exists", request.email());
            return ResponseEntity.badRequest().body("User already exists");
        }

        log.info("Registering new user: {}", request.email());
        UserModel user = new UserModel();
        user.setUsername(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAuthority("ROLE_USER");
        repository.save(user);

        return ResponseEntity.ok("New user successfully registered");
    }
}
