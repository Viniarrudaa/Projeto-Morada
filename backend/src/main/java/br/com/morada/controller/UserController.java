package br.com.morada.controller;

import br.com.morada.dto.CreateUserRequest;
import br.com.morada.dto.UserResponse;
import br.com.morada.security.AuthenticatedUser;
import br.com.morada.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list(@AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.listAll(actor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        userService.requireAdmin(actor);
        return userService.createByAdmin(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.findVisibleUser(actor.id(), actor);
    }

    @GetMapping("/{userId}")
    public UserResponse find(@PathVariable Long userId, @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.findVisibleUser(userId, actor);
    }
}
