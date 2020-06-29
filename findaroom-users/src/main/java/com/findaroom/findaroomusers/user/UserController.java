package com.findaroom.findaroomusers.user;

import com.okta.sdk.resource.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PatchMapping("/my-account")
    public ResponseEntity<User> update(@RequestBody @Valid UserInfo userInfo,
                                       @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.update(jwt.getSubject(), userInfo));
    }

    @PatchMapping("/my-favorites/add")
    public ResponseEntity<User> addAccommodationToFavorites(@RequestParam String accommodationId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.addAccommodationToFavourites(jwt.getSubject(), accommodationId));
    }

    @PatchMapping("/my-favorites/{accommodationId}/remove")
    public ResponseEntity<User> removeAccommodationFromFavorites(@PathVariable String accommodationId,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.removeAccommodationFromFavourites(jwt.getSubject(), accommodationId));
    }

}
