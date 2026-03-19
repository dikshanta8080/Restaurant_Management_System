package com.dikshanta.restaurant.management.system.group_project.security;

import com.dikshanta.restaurant.management.system.group_project.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = authHeader.split(" ")[1];
            System.out.println("Processing token in filter: " + token);
            String username = jwtService.extractUsername(token);
            System.out.println("Extracted username: " + username);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                System.out.println("Loaded UserDetails: " + userDetails.getUsername());
                if (jwtService.isValid(token, userDetails)) {
                    System.out.println("Token is valid!");
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    System.out.println("Token is NOT valid!");
                }
            } else {
                System.out.println("Skipped auth check: username=" + username + ", current context auth=" + SecurityContextHolder.getContext().getAuthentication());
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationServiceException e) {
            System.out.println("AuthenticationServiceException caught: " + e.getMessage());
            authenticationEntryPoint.commence(request, response, e);
        } catch (Exception e) {
            System.out.println("General Exception caught in filter: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
