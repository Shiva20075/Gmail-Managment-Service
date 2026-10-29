package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@Service
public class GmailActionService {

    @Autowired
    private RestClient restClient;

    public void markAsRead(String accessToken, List<String> ids) {

        validateIds(ids);

        restClient.post().uri("/messages/batchModify")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .body(Map.of("ids", ids, "removeLabelIds", List.of("UNREAD")))
                .retrieve()
                .toBodilessEntity();
    }

    public void markAsImportant(String accessToken, List<String> ids) {

        validateIds(ids);

        restClient.post().uri("/messages/batchModify")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .body(Map.of("ids", ids, "addLabelIds", List.of("IMPORTANT")))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteAll(String accessToken, List<String> ids) {

        validateIds(ids);

        for (String id : ids) {
            restClient.post().uri("/messages/{id}/trash", id)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private void validateIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one email ID is required");
        }

        if (ids.size() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 100 emails per action");
        }

        if (ids.stream().anyMatch(id -> id == null || id.isBlank() || id.length() > 200)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email ID");
        }

        if (ids.stream().distinct().count() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate email IDs are not allowed");
        }
    }
}
