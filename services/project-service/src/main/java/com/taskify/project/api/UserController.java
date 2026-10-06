package com.taskify.project.api;

import com.taskify.project.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for the predefined users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService users;

    /**
     * Creates the controller.
     *
     * @param users user service
     */
    public UserController(UserService users) {
        this.users = users;
    }

    /**
     * Lists the five predefined users.
     *
     * @return all users
     */
    @GetMapping
    public List<UserResponse> list() {
        return users.list().stream().map(UserResponse::from).toList();
    }
}
