package com.example.auths3.config;

import com.example.auths3.security.JwtAuthenticationFilter;
import com.example.auths3.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
//CADENA DE FILTROS DE SEGURIDAD
@Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http, JwtAuthenticationFilter jwtFilter)throws Exception{
    http.csrf(csrf->csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm-> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth->auth
                    .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/me").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/users/photo").authenticated()
                    .requestMatchers("/api/admin/users").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
}
//Crea y expone el filtro de autenticación basado en JWT
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }
//Encriptar la contraseña
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//Es el que autentica a los usuarios en el login, comprobando credenciales
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
//Para que se comunique con posibles Front-End externos
@Bean
    public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("*"));
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("Authorization"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}
}