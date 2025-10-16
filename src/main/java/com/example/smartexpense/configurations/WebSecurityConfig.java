package com.example.smartexpense.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disable CSRF for APIs
            .authorizeHttpRequests(auth -> auth
                // public pages
                .requestMatchers(
                    "/", "/index.html", "/about",
                    "/register", "/userregister",
                    "/user/login", "/userlogin",
                    "/verify-otp", "/otpverify",
                    "/forgot/password", "/forgot/sendOtp", "/reset-password",
                    "/css/**", "/js/**", "/images/**"
                ).permitAll()

                // everything else must be authenticated
                .anyRequest().authenticated()
            )
            
            
            .formLogin(form -> form
                .loginPage("/user/login")        // your login page
                .loginProcessingUrl("/userlogin") // POST URL of your form
                .usernameParameter("email")   // ✅ map your HTML input name
                .defaultSuccessUrl("/userhome", true) // redirect after login
                .failureUrl("/user/login?error=true") // back to login on failure
                .permitAll()
            )

         
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
