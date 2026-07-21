package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer"})
    Optional<Ticket> findById(Long id);

    @EntityGraph(attributePaths = {"customer"})
    Page<Ticket> findByCustomerUserId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Ticket> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    List<Ticket> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);
}
