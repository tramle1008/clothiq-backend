package commerce.sbEcommerce.security;

import commerce.sbEcommerce.security.jwt.AuthEntryPointJwt;
import commerce.sbEcommerce.security.jwt.AuthTokenFilter;
import commerce.sbEcommerce.security.services.UserDetailsServiceIml;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // Uncomment if you want to use method-level security annotations like @PreAuthorize
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceIml userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(UserDetailsServiceIml userDetailsService,
                             AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthTokenFilter authTokenFilter(){
        return new AuthTokenFilter();
    }

    @Bean
   public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                                           OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                                           DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/api/public/**"),
                                new AntPathRequestMatcher("/api/products/**"),
                                new AntPathRequestMatcher("/api/stores"),
                                new AntPathRequestMatcher("/api/stores/nearest"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/api/payments/sepapy-callback"),
                                new AntPathRequestMatcher("/api/orders/status/**"),
                                new AntPathRequestMatcher("/calljson"),
                                new AntPathRequestMatcher("/oauth2/**"),
                                new AntPathRequestMatcher("/login/oauth2/code/google"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/webjars/swagger-ui/**"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/v3/api-docs/**")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/payments/qr")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/deliver/**")).hasAnyAuthority("ROLE_ADMIN", "ROLE_DELIVER")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2 // Enable OAuth2 Login
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize") // Custom authorization base URI
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*") // Custom redirection base URI
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // Use our custom success handler
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
