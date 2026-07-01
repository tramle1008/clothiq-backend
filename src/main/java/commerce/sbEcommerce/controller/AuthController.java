package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.AppRole;
import commerce.sbEcommerce.model.Role;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.repository.RoleRepository;
import commerce.sbEcommerce.repository.UserRepository;
import commerce.sbEcommerce.security.jwt.JwtUtils;
import commerce.sbEcommerce.security.jwt.LoginRequest;
import commerce.sbEcommerce.security.jwt.RefreshTokenRequest;
import commerce.sbEcommerce.security.request.SignupRequest;
import commerce.sbEcommerce.security.response.MessageResponse;
import commerce.sbEcommerce.security.response.UserInfoResponse;
import commerce.sbEcommerce.security.services.UserDetailsImpl;
import commerce.sbEcommerce.security.services.UserDetailsServiceIml;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final UserDetailsServiceIml userDetailsService;

    public AuthController(JwtUtils jwtUtils,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder encoder,
                          RoleRepository roleRepository,
                          UserDetailsServiceIml userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.roleRepository = roleRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Sai tên đăng nhập hoặc mật khẩu");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                accessToken,
                refreshToken
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is required"));
        }

        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Refresh token is invalid or expired"));
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                newAccessToken,
                newRefreshToken
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest signupRequest) {
        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use"));
        }

        Set<String> requestedRoles = signupRequest.getRole();
        if (requestedRoles != null && !requestedRoles.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: Public signup cannot assign roles"));
        }

        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new IllegalArgumentException("Error: ROLE_USER not found"));

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Signup successful"));
    }

    @GetMapping("/username")
    public String currentUser(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        }
        return "null";
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetail(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        String jwtToken = jwtUtils.getJWTFromHeader(request);

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                jwtToken
        );
        return ResponseEntity.ok().body(response);
    }
}
