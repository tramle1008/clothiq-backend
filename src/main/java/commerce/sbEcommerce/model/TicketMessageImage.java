package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_message_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private TicketMessage message;

    @Column(nullable = false, length = 2048)
    private String imageUrl;
}
