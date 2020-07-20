package com.findaroom.findaroomimages.unit;

import com.findaroom.findaroomimages.config.SecurityConfig;
import com.findaroom.findaroomimages.image.Image;
import com.findaroom.findaroomimages.image.ReactiveImgurClient;
import com.findaroom.findaroomimages.image.SaveImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest
@Import(SecurityConfig.class)
public class ImageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    public ReactiveImgurClient imgurClient;

    @Test
    public void save() {

        Image image = new Image();
        image.setImageId("123");
        image.setWidth(400);
        image.setHeight(600);
        image.setDeleteHash("hash");

        when(imgurClient.save(any())).thenReturn(Mono.just(image));

        SaveImage save = new SaveImage();
        save.setImage(new byte[]{});

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/images")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(save)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void delete() {

        when(imgurClient.delete(anyString())).thenReturn(Mono.empty());

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .delete()
                .uri("/api/v1/images/123")
                .exchange()
                .expectStatus().isNoContent();
    }
}
