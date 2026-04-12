package com.medictech.auth_service.controller;

import com.medictech.auth_service.dto.AuthResponse;
import com.medictech.auth_service.dto.LoginRequest;
import com.medictech.auth_service.dto.RegisterRequest;
import com.medictech.auth_service.entity.User;
import com.medictech.auth_service.repository.UserRepository;
import com.medictech.auth_service.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // Verifica si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("El email ya está registrado");
        }

        // Construye el usuario con el password encriptado
        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Busca el usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Verifica que exista y que el password coincida
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales incorrectas");
        }

        // Genera el token con email, role y userId
        String token = jwtUtils.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId()
        );

        // Devuelve la respuesta con el token y datos del usuario
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}
