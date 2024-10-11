package org.identifiers.cloud.ws.linkchecker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.identifiers.cloud.ws.linkchecker.api.models.LinkScoringApiModel;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoreProvider;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoreResource;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoring;
import org.identifiers.cloud.ws.linkchecker.api.responses.ServiceResponseScoringRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.controllers
 * Timestamp: 2018-05-21 10:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@RestController
@RequiredArgsConstructor
public class LinkScoringApiController {
    private final LinkScoringApiModel model;

    @PostMapping("/getScoreForUrl")
    public ResponseEntity<ServiceResponseScoringRequest> getScoreForUrl(@RequestBody ServiceRequestScoring request) {
        ServiceResponseScoringRequest response = model.getScoreForUrl(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PostMapping("/getScoreForResolvedId")
    public ResponseEntity<ServiceResponseScoringRequest> getScoreForResolvedId(@RequestBody ServiceRequestScoreResource request) {
        ServiceResponseScoringRequest response = model.getScoreForResolvedId(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PostMapping("/getScoreForProvider")
    public ResponseEntity<ServiceResponseScoringRequest> getScoreForProvider(@RequestBody ServiceRequestScoreProvider request) {
        ServiceResponseScoringRequest response = model.getScoreForProvider(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
