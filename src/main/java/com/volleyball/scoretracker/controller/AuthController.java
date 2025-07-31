package com.volleyball.scoretracker.controller;

import com.volleyball.scoretracker.dto.*;
import com.volleyball.scoretracker.model.User;
import com.volleyball.scoretracker.service.UserService;
import com.volleyball.scoretracker.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request);
            
            String jwt = jwtUtils.generateJwtToken(user.getUsername());
            
            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            userService.updateLastLogin(user);
            
            String jwt = jwtUtils.generateJwtToken(user.getUsername());
            
            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setMessage("Invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    AuthResponse response = new AuthResponse();
                    response.setToken(token);
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setMessage("Invalid token");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            MessageResponse errorResponse = new MessageResponse();
            errorResponse.setMessage("Token validation failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}