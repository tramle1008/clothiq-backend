package commerce.sbEcommerce.model;



import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gateway;
    private Timestamp transactionDate;
    private String accountNumber;
    private String subAccount;

    private BigDecimal amountIn;
    private BigDecimal amountOut;
    private BigDecimal accumulated;

    private String code; // mã đơn hàng
    private String transactionContent;
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String body;
    @OneToOne(mappedBy = "transaction")
    private Payment payment;
}
