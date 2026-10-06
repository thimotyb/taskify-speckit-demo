package com.taskify.project.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link User}.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Lists all users in a stable order.
     *
     * @return users ordered by id
     */
    List<User> findAllByOrderByIdAsc();
}
