package commerce.sbEcommerce.security;

import commerce.sbEcommerce.model.AppRole;
import commerce.sbEcommerce.model.Role;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.repository.RoleRepository;
import commerce.sbEcommerce.repository.UserRepository;
import commerce.sbEcommerce.security.jwt.JwtUtils;
import commerce.sbEcommerce.security.services.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,
                                              RoleRepository roleRepository,
                                              PasswordEncoder passwordEncoder,
                                              JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Value("#{'${frontend.urls}'.split(',')}")
    private List<String> frontendUrls;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");

            if (email == null || email.isBlank()) {
                response.sendRedirect(resolveFrontendUrl() + "/login?error=missing_email");
                return;
            }

            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;

            if (userOptional.isEmpty()) {
                user = new User();
                user.setEmail(email);
                user.setUserName(resolveUsername(email));
                user.setPassword(passwordEncoder.encode("GoogleLogin"));
                Set<Role> roles = new HashSet<>();
                Optional<Role> userRole = roleRepository.findByRoleName(AppRole.ROLE_USER);
                userRole.ifPresent(roles::add);
                user.setRoles(roles);

                userRepository.save(user);
            } else {
                user = userOptional.get();
            }

            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            String jwt = jwtUtils.generateJwtToken(userDetails);

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(resolveFrontendUrl())
                    .path("/oauth2/redirect")
                    .queryParam("token", jwt)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect(resolveFrontendUrl() + "/login?error=oauth2_failed");
        }
    }

    private String resolveFrontendUrl() {
        return frontendUrls.stream()
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No frontend URL configured"));
    }

    private String resolveUsername(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        if (!userRepository.existsByUserName(localPart)) {
            return localPart;
        }

        if (!userRepository.existsByUserName(email)) {
            return email;
        }

        int suffix = 1;
        String candidate = localPart + suffix;
        while (userRepository.existsByUserName(candidate)) {
            suffix++;
            candidate = localPart + suffix;
        }
        return candidate;
    }
}

