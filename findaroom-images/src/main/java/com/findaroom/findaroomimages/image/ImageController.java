package com.findaroom.findaroomimages.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ReactiveImgurClient imgurClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Image> save(@RequestBody SaveImage image) {
        return imgurClient.save(image);
    }

    @DeleteMapping("/{deleteHash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String deleteHash) {
        return imgurClient.delete(deleteHash);
    }
}
