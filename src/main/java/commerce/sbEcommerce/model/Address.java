package commerce.sbEcommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "address")
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "detail")
    private String detail;

    @NotBlank
    @ToString.Exclude
    @Size(min = 10, message = "So dien thoai phai co it nhat 10 chu so")
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(Ward ward, String detail, String phoneNumber) {
        this.ward = ward;
        this.detail = detail;
        this.phoneNumber = phoneNumber;
    }
}
