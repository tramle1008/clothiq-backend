package commerce.sbEcommerce.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String jwtToken;
    private String accessToken;
    private String refreshToken;
    private String userName;
    private String email;
    private String password;
    private List<String> role;



    public UserInfoResponse(Long id, String jwtToken, List<String> role, String userName) {
      this.id=id;
        this.jwtToken = jwtToken;
        this.role = role;
        this.userName = userName;
    }
    public UserInfoResponse(Long id, String userName,List<String> role) {
        this.id=id;
        this.role = role;
        this.userName = userName;
    }

    public UserInfoResponse(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;

    }
    public UserInfoResponse(Long id, String userName, String email, List<String> role, String jwtToken) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
        this.accessToken = jwtToken;
    }

    public UserInfoResponse(Long id, String userName, String email, List<String> role, String accessToken, String refreshToken) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.jwtToken = accessToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }


}
