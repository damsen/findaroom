package com.findaroom.findaroomnotifications.integration;

import com.findaroom.findaroomnotifications.notification.Notification;
import com.findaroom.findaroomnotifications.notification.NotificationRepo;
import com.findaroom.findaroomnotifications.utils.TestPojos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static com.findaroom.findaroomnotifications.notification.NotificationController.NOTIFICATION_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
public class NotificationsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private NotificationRepo notificationRepo;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    public void setup() {
        notificationRepo.deleteAll().block();
    }

    @Test
    public void getUserNotifications() {

        notificationRepo.saveAll(Flux.just(TestPojos.notification(), TestPojos.notification())).blockLast();

        var jwtMutator = mockJwt()
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/notifications")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_STREAM_JSON)
                .expectBodyList(Notification.class).hasSize(2);
    }

    @Test
    public void getUserNotificationById() {

        var n = TestPojos.notification();
        n.setNotificationId("123");
        notificationRepo.saveAll(Flux.just(n, TestPojos.notification())).blockLast();

        var jwtMutator = mockJwt()
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/notifications/{notificationId}", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.notificationId", "123").exists()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists()
                .jsonPath("@.seen", "true").exists();
    }

    @Test
    public void getUserNotificationById_whenNotFound_shouldReturnNotFound() {

        var jwtMutator = mockJwt()
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/notifications/{notificationId}", "123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("@.message", NOTIFICATION_NOT_FOUND).exists();
    }

    @Test
    public void notifyUser() {

        webTestClient
                .mutateWith(mockJwt())
                .post()
                .uri("/api/v1/notifications")
                .contentType(APPLICATION_JSON)
                .bodyValue(TestPojos.notifyUser())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists();
    }

    @Test
    public void deleteNotification() {

        var n = TestPojos.notification();
        n.setNotificationId("123");
        notificationRepo.save(n);

        var jwtMutator = mockJwt()
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .delete()
                .uri("/api/v1/notifications/{notificationId}", "123")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/notifications/{notificationId}", "123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("@.message", NOTIFICATION_NOT_FOUND).exists();
    }
}
