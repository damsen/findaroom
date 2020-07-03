package com.findaroom.findaroomusers.user;

import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.user.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactiveOktaClient implements ReactiveClient {

    private final Client client;

    public ReactiveOktaClient() {
        this.client = Clients.builder().build();
    }

    public Mono<User> getUser(String userId) {
        return onBoundedElastic(() -> client.getUser(userId));
    }

    public Mono<User> update(User user) {
        return onBoundedElastic(user::update);
    }
}
