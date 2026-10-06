package com.taskify.project.api;

import com.taskify.project.domain.User;
import com.taskify.project.domain.UserRole;
import java.util.UUID;

/**
 * Public view of a user.
 *
 * @param id   user id
 * @param name display name
 * @param role role label
 */
public record UserResponse(UUID id, String name, UserRole role) {

    /**
     * Maps a user entity to its response.
     *
     * @param user the entity
     * @return the response
     */
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getRole());
    }
}
