package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.TicketMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    @EntityGraph(attributePaths = {"sender", "images"})
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    @Query("select count(m) from TicketMessage m where m.ticket.id = :ticketId")
    long countByTicketId(@Param("ticketId") Long ticketId);
}
