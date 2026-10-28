package ru.maxim.gamestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/games", "/games/*/cover", "/games/*",
                                "/css/**",
                                "/js/**", "/images/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin/games/{id}/edit"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/admin/games/{id}/edit"
                        ).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/games/{id}/screenshots/{number}"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
