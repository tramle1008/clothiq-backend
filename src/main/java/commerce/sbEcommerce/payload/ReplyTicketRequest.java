package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyTicketRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    private List<MultipartFile> images;
}
