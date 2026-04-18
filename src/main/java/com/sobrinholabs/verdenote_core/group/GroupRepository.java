package com.sobrinholabs.verdenote_core.group;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, UUID> {
	@EntityGraph(attributePaths = "permissions")
	Optional<Group> findByNameIgnoreCase(String name);

	@EntityGraph(attributePaths = "permissions")
	List<Group> findByNameIn(Collection<String> names);

	boolean existsByNameIgnoreCase(String name);
}
