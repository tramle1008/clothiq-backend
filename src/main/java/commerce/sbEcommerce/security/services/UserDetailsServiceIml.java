package commerce.sbEcommerce.security.services;

import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//tìm và trả về thông tin người dùng (UserDetails) dựa trên tên đăng nhập (username
@Service
public class UserDetailsServiceIml implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceIml(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional //toàn bộ hoạt động đã hoàn tất nếu có bất kỳ lỗi naò xảy ra thì toàn bộ hoạt động sẽ khôi phục
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserNameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + username));
        return UserDetailsImpl.build(user);
    }


}

