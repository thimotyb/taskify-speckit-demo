package com.taskify.project.service;

import com.taskify.project.domain.User;
import com.taskify.project.domain.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to the predefined users.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param users user repository
     */
    public UserService(UserRepository users) {
        this.users = users;
    }

    /**
     * Lists all predefined users.
     *
     * @return users in a stable order
     */
    public List<User> list() {
        return users.findAllByOrderByIdAsc();
    }

    /**
     * Finds one user.
     *
     * @param id user id
     * @return the user
     * @throws NotFoundException if the user does not exist
     */
    public User get(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
