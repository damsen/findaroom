package com.findaroom.findaroomusers.user;

import com.okta.sdk.resource.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public Mono<PublicUser> getById(@PathVariable String userId) {
        return userService.getById(userId);
    }

    @GetMapping("/my-account")
    public Mono<User> getMe(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(jwt.getSubject());
    }

    @PatchMapping("/my-account")
    public Mono<User> update(@RequestBody @Valid UserInfo userInfo,
                             @AuthenticationPrincipal Jwt jwt) {
        return userService.update(jwt.getSubject(), userInfo);
    }

    @PostMapping("/my-account/favorites")
    public Mono<User> addFavorite(@RequestParam String accommodationId,
                                  @AuthenticationPrincipal Jwt jwt) {
        return userService.addFavorite(jwt.getSubject(), accommodationId);
    }

    @DeleteMapping("/my-account/favorites/{accommodationId}")
    public Mono<User> removeFavorite(@PathVariable String accommodationId,
                                     @AuthenticationPrincipal Jwt jwt) {
        return userService.removeFavorite(jwt.getSubject(), accommodationId);
    }

}
