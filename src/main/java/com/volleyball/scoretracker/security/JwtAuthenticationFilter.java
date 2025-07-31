package com.volleyball.scoretracker.security;

import com.volleyball.scoretracker.service.UserService;
import com.volleyball.scoretracker.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String guestSessionId = request.getHeader("Guest-Session-Id");
        
        // Skip authentication for auth and guest endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/api/guest/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Handle JWT authentication
                String jwt = authHeader.substring(7);
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    Optional<User> userOpt = userService.findByUsername(username);
                    
                    if (userOpt.isPresent()) {
                        UserDetails userDetails = UserDetailsImpl.build(userOpt.get());
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } else if (guestSessionId != null && !guestSessionId.trim().isEmpty()) {
                // For guest sessions, we don't set authentication but allow the request to proceed
                // The controller will validate the guest session
                System.out.println("Processing guest request with session: " + guestSessionId);
            }
        } catch (Exception e) {
            System.err.println("Cannot set user authentication: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}