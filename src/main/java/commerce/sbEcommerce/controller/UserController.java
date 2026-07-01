package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.CategoryResponse;
import commerce.sbEcommerce.payload.UserDTO;
import commerce.sbEcommerce.security.jwt.JwtUtils;
import commerce.sbEcommerce.security.request.UpdateUserRequest;
import commerce.sbEcommerce.security.response.MessageResponse;
import commerce.sbEcommerce.security.response.UserInfoResponse;
import commerce.sbEcommerce.security.services.UserDetailsImpl;
import commerce.sbEcommerce.service.UserService;
import commerce.sbEcommerce.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    JwtUtils jwtUtils;
    @GetMapping("/public/user")
    public ResponseEntity<List<UserDTO>> getUser(){
        List<UserDTO> allUser= userService.getAllUser();
        return new ResponseEntity<>(allUser, HttpStatus.OK);
    }
    @GetMapping("/public/admin")
    public ResponseEntity<List<UserDTO>> getAdmin(){
        List<UserDTO> allAdmin= userService.getAllAdmin();
        return new ResponseEntity<>(allAdmin, HttpStatus.OK);
    }
    @PutMapping("/auth/user/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        String result = userService.updateCurrentUser(request);

        User updatedUser = authUtil.getCurrentUserEntity();
        UserDetailsImpl userDetails = UserDetailsImpl.build(updatedUser);
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        return ResponseEntity.ok(new UserInfoResponse(
                updatedUser.getUserId(),
                updatedUser.getUserName(),
                updatedUser.getEmail(),
                roles,
                newAccessToken,
                newRefreshToken
        ));
    }

}
