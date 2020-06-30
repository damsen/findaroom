package com.findaroom.findaroomusers.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
public class UserService {

    private final Client oktaClient;
    private final ObjectMapper objectMapper;

    public UserService(ObjectMapper objectMapper) {
        this.oktaClient = Clients.builder().build();
        this.objectMapper = objectMapper;
    }

    public PublicUser getById(String userId) {
        return PublicUser.from(oktaClient.getUser(userId));
    }

    public User getMe(String userId) {
        return oktaClient.getUser(userId);
    }

    public User update(String userId, UserInfo userInfo) {
        User user = oktaClient.getUser(userId);
        user.getProfile().putAll(objectMapper.convertValue(userInfo, new TypeReference<Map<String, Object>>() {}));
        return user.update();
    }

    public User addAccommodationToFavourites(String userId, String accommodationId) {
        return updateUserFavorites(userId, accommodationId, this::addToFavoritesInternal);
    }

    public User removeAccommodationFromFavourites(String userId, String accommodationId) {
        return updateUserFavorites(userId, accommodationId, this::removeFromFavoritesInternal);
    }

    private User updateUserFavorites(String userId, String accommodationId, BiConsumer<String, List<String>> operation) {
        User user = oktaClient.getUser(userId);
        operation.accept(accommodationId, user.getProfile().getStringList("favoriteAccommodations"));
        return user.update();
    }

    private void addToFavoritesInternal(String accommodationId, List<String> favorites) {
        boolean isAlreadyInFavorites = favorites
                .stream()
                .anyMatch(id -> Objects.equals(accommodationId, id));

        if (isAlreadyInFavorites) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Accommodation is already a favorite.");
        }
        favorites.add(accommodationId);
    }

    private void removeFromFavoritesInternal(String accommodationId, List<String> favorites) {
        favorites
                .stream()
                .filter(id -> Objects.equals(accommodationId, id))
                .findFirst()
                .map(favorites::remove)
                .orElseThrow(() -> new ResponseStatusException(UNPROCESSABLE_ENTITY, "Accommodation is not a favorite."));
    }

}
