package com.sobrinholabs.verdenote_core.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
	@EntityGraph(attributePaths = {"groups", "groups.permissions"})
	Optional<User> findByUsernameIgnoreCase(String username);

	@EntityGraph(attributePaths = {"groups", "groups.permissions"})
	Optional<User> findByEmailIgnoreCase(String email);

	@Override
	@EntityGraph(attributePaths = {"groups", "groups.permissions"})
	Optional<User> findById(UUID id);

	@Override
	@EntityGraph(attributePaths = {"groups", "groups.permissions"})
	Page<User> findAll(Pageable pageable);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	@EntityGraph(attributePaths = {"groups", "groups.permissions"})
	@Query("""
			select distinct user from User user
			where (lower(user.firstName) like lower(concat('%', :search, '%'))
				or lower(user.lastName) like lower(concat('%', :search, '%'))
				or lower(user.username) like lower(concat('%', :search, '%'))
				or lower(user.email) like lower(concat('%', :search, '%')))
			""")
	Page<User> search(@Param("search") String search, Pageable pageable);
}
