package com.sobrinholabs.verdenote_core.audit;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginAuditEventRepository extends JpaRepository<LoginAuditEvent, UUID> {
	@Override
	@EntityGraph(attributePaths = "user")
	Page<LoginAuditEvent> findAll(Pageable pageable);

	@EntityGraph(attributePaths = "user")
	@Query("""
			select event from LoginAuditEvent event
			left join event.user user
			where (lower(coalesce(event.identifier, '')) like lower(concat('%', :search, '%'))
				or lower(event.ipAddress) like lower(concat('%', :search, '%'))
				or lower(coalesce(user.username, '')) like lower(concat('%', :search, '%'))
				or lower(coalesce(user.email, '')) like lower(concat('%', :search, '%')))
			""")
	Page<LoginAuditEvent> search(@Param("search") String search, Pageable pageable);
}
