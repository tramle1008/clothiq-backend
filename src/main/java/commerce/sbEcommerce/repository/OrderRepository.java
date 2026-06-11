package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.DeliveryStatus;
import commerce.sbEcommerce.model.Order;
import commerce.sbEcommerce.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.paymentStatus = 'PAID'
            AND o.dateOrder BETWEEN :fromDate AND :toDate
            """)
    double findTotalRevenue(LocalDate fromDate, LocalDate toDate);

    default double findTotalRevenue() {
        LocalDate now = LocalDate.now();
        return findTotalRevenue(
                LocalDate.of(now.getYear(), 1, 1),
                LocalDate.of(now.getYear(), 12, 31)
        );
    }

    default double findCurrentMonthRevenue() {
        LocalDate now = LocalDate.now();
        return findTotalRevenue(
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth())
        );
    }

    @Query(value = """
    SELECT DATE_FORMAT(date_order, '%Y-%m-%d') AS period, COALESCE(SUM(total_amount), 0) AS revenue
    FROM orders
    WHERE payment_status = 'PAID'
    AND date_order BETWEEN :fromDate AND :toDate
    GROUP BY DATE_FORMAT(date_order, '%Y-%m-%d')
    ORDER BY period
    """, nativeQuery = true)
    List<RevenueChartPointProjection> findRevenueChartByDay(LocalDate fromDate, LocalDate toDate);

    @Query(value = """
    SELECT DATE_FORMAT(date_order, '%Y-%m') AS period, COALESCE(SUM(total_amount), 0) AS revenue
    FROM orders
    WHERE payment_status = 'PAID'
    AND date_order BETWEEN :fromDate AND :toDate
    GROUP BY DATE_FORMAT(date_order, '%Y-%m')
    ORDER BY period
    """, nativeQuery = true)
    List<RevenueChartPointProjection> findRevenueChartByMonth(LocalDate fromDate, LocalDate toDate);

    @Query(value = """
    SELECT DATE_FORMAT(date_order, '%Y') AS period, COALESCE(SUM(total_amount), 0) AS revenue
    FROM orders
    WHERE payment_status = 'PAID'
    AND date_order BETWEEN :fromDate AND :toDate
    GROUP BY DATE_FORMAT(date_order, '%Y')
    ORDER BY period
    """, nativeQuery = true)
    List<RevenueChartPointProjection> findRevenueChartByYear(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliveryStatus = 'PENDING'")
    long countPendingDeliveries();

    @Query(value = """
    SELECT COUNT(*) FROM orders
    WHERE delivery_status = 'PENDING'
    AND (
        payment_method <> 'QR'
        OR (payment_method = 'QR' AND payment_status <> 'UNPAID')
    )
    """,nativeQuery = true
    )
    long countPendingDeliveriesWithPayment();

    Optional<Order> findByCode(String code);


    Page<Order> findByEmail(String email, Pageable pageable);

    Page<Order> findByDeliveryStatus(DeliveryStatus status, Pageable pageable);


    List<Order> findByEmail(String userEmail);

    @Query("SELECT o.paymentStatus FROM Order o WHERE o.code = :code AND o.email = :email")
    PaymentStatus findPaymentStatusByCodeAndEmail(String code, String email);

}
