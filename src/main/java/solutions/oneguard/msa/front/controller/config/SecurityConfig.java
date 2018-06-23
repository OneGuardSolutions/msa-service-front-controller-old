package solutions.oneguard.msa.front.controller.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import solutions.oneguard.msa.front.controller.security.JWTAuthenticationFilter;
import solutions.oneguard.msa.front.controller.security.JWTLoginFilter;
import solutions.oneguard.msa.front.controller.security.TokenAuthenticationService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenAuthenticationService authenticationService;

    @Value("${security.signing-key}")
    private String signingKey;

    @Value("${security.encoding-strength}")
    private Integer encodingStrength;

    @Autowired
    public SecurityConfig(TokenAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/", "/favicon.ico").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/token").permitAll()
                .anyRequest().authenticated()
            .and()
            // We filter the /auth/token requests
            .addFilterBefore(
                new JWTLoginFilter("/auth/token", authenticationManager(), authenticationService, null),
                UsernamePasswordAuthenticationFilter.class
            )
            // And filter other requests to check the presence of JWT in header
            .addFilterBefore(
                new JWTAuthenticationFilter(authenticationService),
                UsernamePasswordAuthenticationFilter.class
            );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
            .inMemoryAuthentication()
            .passwordEncoder(passwordEncoder())
            .withUser("user")
                .password("{noop}user")
                .roles("USER")
            .and()
            .withUser("admin")
                .password("{noop}admin")
                .roles("USER", "ADMIN");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
