package br.edu.ifsp.mef.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        return http
        		.authorizeHttpRequests((auth) -> auth.anyRequest()
        			.authenticated())
        			.formLogin(withDefaults())
        		.httpBasic(withDefaults())
        		.build();

    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin").password("{noop}1234").roles("ADMIN").build();

        return new InMemoryUserDetailsManager(admin);
    }
    
}