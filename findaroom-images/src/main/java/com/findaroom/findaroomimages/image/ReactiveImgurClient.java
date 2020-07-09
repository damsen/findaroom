package com.findaroom.findaroomimages.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class ReactiveImgurClient {

    public static final String IMGUR_ERROR = "Imgur responded with an error status of %d.";

    @Value("${imgur.api.upload-image-url}")
    public String imgurUrl;

    @Value("${imgur.api.client-id}")
    private String imgurClientId;

    private final WebClient webClient;

    public ReactiveImgurClient() {
        this.webClient = WebClient
                .builder()
                .filter(ExchangeFilterFunctions.statusError(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> new ResponseStatusException(clientResponse.statusCode(), String.format(IMGUR_ERROR, clientResponse.rawStatusCode()))
                ))
                .build();
    }

    public Mono<Image> save(SaveImage image) {
        return webClient
                .post()
                .uri(imgurUrl)
                .headers(h -> h.add(HttpHeaders.AUTHORIZATION, imgurClientId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(image)
                .retrieve()
                .bodyToMono(ImgurResponse.class)
                .map(ImgurResponse::getImage);
    }

    public Mono<Void> delete(String deleteHash) {
        return webClient
                .delete()
                .uri(imgurUrl + "/" + deleteHash)
                .headers(h -> h.add(HttpHeaders.AUTHORIZATION, imgurClientId))
                .retrieve()
                .bodyToMono(Void.class);
    }
}
