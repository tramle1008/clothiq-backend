package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "Ticket title is required")
    @Size(max = 255, message = "Ticket title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Message content is required")
    private String content;

    private List<MultipartFile> images;
}
