package com.dikshanta.restaurant.management.system.group_project.security;

import com.dikshanta.restaurant.management.system.group_project.handlers.CustomAccessDeniedHandler;
import com.dikshanta.restaurant.management.system.group_project.handlers.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.dikshanta.restaurant.management.system.group_project.enums.Role.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(req -> req

                        // ── Public (no auth required) ──────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/v1/health/**"
                        ).permitAll()

                        // Public browse: anyone can see the restaurant list and food menus
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/allRestaurants").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/restaurants/*/food-items").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/food-items").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/allCategoriws").permitAll()
                        // Serve uploaded images without auth
                        .requestMatchers("/uploads/**").permitAll()
                        // Backwards-compatible public routes (older frontends)
                        .requestMatchers(HttpMethod.GET, "/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/foods/**").permitAll()

                        // ── Admin-only ────────────────────────────────────────────────────
                        .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())

                        // ── Restaurant management (owner actions) ─────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurant/restaurants/*/food-items").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,  "/api/v1/restaurant/food-items/*").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurant/food-items/*").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/getRestaurant").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurant/updateRestaurant/**").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurant/deleteRestaurant/**").hasAnyRole(RESTAURANT.name(), ADMIN.name())

                        // ── Restaurant order management ───────────────────────────────────
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurant/orders/*/status").hasAnyRole(RESTAURANT.name(), ADMIN.name())

                        // ── Customer routes (all authenticated users) ─────────────────────
                        // Profile
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/customer/profileUpdate").authenticated()
                        // Register a restaurant (any logged-in user can apply)
                        .requestMatchers(HttpMethod.POST, "/api/v1/customer/createRestaurant").authenticated()

                        // ── Cart (customer + admin) ───────────────────────────────────────
                        .requestMatchers("/api/v1/customer/cart/**").hasAnyRole(CUSTOMER.name(), ADMIN.name())
                        .requestMatchers("/api/v1/customer/cart").hasAnyRole(CUSTOMER.name(), ADMIN.name())

                        // ── Orders ────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/customer/orders").hasAnyRole(CUSTOMER.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/orders/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/orders").authenticated()
                        .requestMatchers("/api/v1/customer/payments/**").hasAnyRole(CUSTOMER.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/payments").hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/payments").hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant/orders").hasAnyRole(RESTAURANT.name(), ADMIN.name())

                        // ── Reviews ───────────────────────────────────────────────────────
                        // GET: anyone can read reviews
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        // POST/PUT: CUSTOMER only — further enforced by @PreAuthorize in ReviewController
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole(CUSTOMER.name())
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole(CUSTOMER.name())
                        // DELETE: review owner or admin (fine-grained check done in service)
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                        // ── Anything else requires authentication ─────────────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow both dev ports and any localhost port for flexibility
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
