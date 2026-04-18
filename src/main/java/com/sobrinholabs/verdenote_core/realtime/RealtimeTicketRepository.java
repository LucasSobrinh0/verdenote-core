package com.sobrinholabs.verdenote_core.realtime;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealtimeTicketRepository extends JpaRepository<RealtimeTicket, UUID> {
	@EntityGraph(attributePaths = {"document", "user", "user.groups", "user.groups.permissions"})
	Optional<RealtimeTicket> findByTicketHash(String ticketHash);
}
