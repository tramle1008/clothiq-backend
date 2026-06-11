package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.AppRole;
import commerce.sbEcommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserName(String username);

    Optional<User> findByUserName(String userName);

    Optional<User> findByUserNameOrEmail(String userName, String email);

    boolean existsByEmail(String email);

    List<User> findByRoles_RoleName(AppRole roleName);

    Optional<User> findByEmail(String email);
}
