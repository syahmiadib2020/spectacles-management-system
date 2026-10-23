package com.meteor.eyewear;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwords() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(Accounts accounts) {
        return email -> {
            Account account = accounts.findByEmailIgnoreCase(email.trim()).orElse(null);
            if (account == null) {
                throw new UsernameNotFoundException("Account not found");
            }
            return User.withUsername(account.getEmail())
                    .password(account.getPasswordHash())
                    .roles(account.getRole())
                    .build();
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/login",
                                                "/signup",
                                                "/about",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/error")
                                        .permitAll()
                                        .requestMatchers("/staff/**")
                                        .hasRole("STAFF")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        form ->
                                form.loginPage("/login")
                                        .usernameParameter("email")
                                        .defaultSuccessUrl("/dashboard", true)
                                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .headers(
                        headers ->
                                headers.contentSecurityPolicy(
                                        policy ->
                                                policy.policyDirectives(
                                                        "default-src 'self'; img-src 'self' data:;"
                                                            + " style-src 'self'; script-src"
                                                            + " 'self'; form-action 'self';"
                                                            + " frame-ancestors 'none'; base-uri"
                                                            + " 'self'")));
        return http.build();
    }
}
