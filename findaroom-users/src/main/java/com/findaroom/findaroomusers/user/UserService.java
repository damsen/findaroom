package com.findaroom.findaroomusers.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.resource.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.findaroom.findaroomusers.utils.ErrorUtils.unprocessableEntity;
import static com.findaroom.findaroomusers.utils.MessageUtils.ALREADY_FAVORITE;
import static com.findaroom.findaroomusers.utils.MessageUtils.NOT_FAVORITE;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ReactiveOktaClient oktaClient;
    private final ObjectMapper objectMapper;

    public Mono<PublicUser> getById(String userId) {
        return oktaClient.getUser(userId).map(PublicUser::from);
    }

    public Mono<User> getMe(String userId) {
        return oktaClient.getUser(userId);
    }

    public Mono<User> update(String userId, UserInfo userInfo) {
        return oktaClient
                .getUser(userId)
                .doOnNext(user -> user.getProfile()
                        .putAll(objectMapper.convertValue(userInfo, new TypeReference<Map<String, Object>>() {
                        })))
                .flatMap(oktaClient::update);
    }

    public Mono<User> addAccommodationToFavourites(String userId, String accommodationId) {
        return oktaClient
                .getUser(userId)
                .flatMap(user -> {
                    List<String> favorites = user.getProfile().getStringList("favoriteAccommodations");
                    if (!CollectionUtils.isEmpty(favorites) && favorites.contains(accommodationId)) {
                        return Mono.error(unprocessableEntity(ALREADY_FAVORITE));
                    }
                    favorites.add(accommodationId);
                    return Mono.just(user);
                })
                .flatMap(oktaClient::update);
    }

    public Mono<User> removeAccommodationFromFavourites(String userId, String accommodationId) {
        return oktaClient
                .getUser(userId)
                .flatMap(user -> {
                    List<String> favorites = user.getProfile().getStringList("favoriteAccommodations");
                    if (!CollectionUtils.isEmpty(favorites) && favorites.contains(accommodationId)) {
                        favorites.remove(accommodationId);
                        return Mono.just(user);
                    }
                    return Mono.error(unprocessableEntity(NOT_FAVORITE));
                })
                .flatMap(oktaClient::update);
    }

}
