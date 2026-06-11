package commerce.sbEcommerce.config;

import commerce.sbEcommerce.model.AppRole;
import commerce.sbEcommerce.model.Role;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.repository.RoleRepository;
import commerce.sbEcommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${system.user.name}")
    private String systemName;

    @Value("${system.user.email}")
    private String systemEmail;

    @Value("${system.user.password}")
    private String systemPassword;

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, 
                                   UserRepository userRepository, 
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Khởi tạo Roles
            initializeRole(roleRepository, AppRole.ROLE_USER);
            initializeRole(roleRepository, AppRole.ROLE_ADMIN);
            initializeRole(roleRepository, AppRole.ROLE_DELIVER);

            try {
                // 2. Khởi tạo System User - Kiểm tra cả Username và Email
                if (!userRepository.existsByUserName(systemName) && !userRepository.existsByEmail(systemEmail)) {
                    User systemUser = new User(systemName, systemEmail, passwordEncoder.encode(systemPassword));
                    
                    // Đảm bảo các trường bắt buộc khác (nếu có) được set giá trị mặc định
                    // Ví dụ: systemUser.setMobileNumber("0000000000");

                    Set<Role> roles = new HashSet<>();
                    roleRepository.findByRoleName(AppRole.ROLE_ADMIN).ifPresent(roles::add);
                    roleRepository.findByRoleName(AppRole.ROLE_USER).ifPresent(roles::add);
                    roleRepository.findByRoleName(AppRole.ROLE_DELIVER).ifPresent(roles::add);
                    systemUser.setRoles(roles);
                    userRepository.save(systemUser);
                    logger.info("System user created successfully with username: {}", systemName);
                } else {
                    logger.info("System user already exists, skipping initialization.");
                }
            } catch (Exception e) {
                logger.error("Failed to initialize system user: {}", e.getMessage());
                // Không throw lại exception để tránh làm sập ứng dụng nếu chỉ lỗi khởi tạo user
            }
        };
    }

    private void initializeRole(RoleRepository roleRepository, AppRole roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }
}