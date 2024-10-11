package org.identifiers.cloud.ws.linkchecker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.identifiers.cloud.ws.linkchecker.api.models.LinkScoringApiModel;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * This is a temporary feature while we don't have a dedicated curation pipeline
 */
@RestController
@RequiredArgsConstructor
public class CurationController {
    private final LinkScoringApiModel model;

    @GetMapping("/getResourcesWithLowAvailability")
    public ResponseEntity<Map<String, Float>> getResourcesWithLowAvailability(
            @RequestParam(defaultValue="80", required=false) int minAvailability)
    {
        var response = model.getResourcesIdsWithAvailabilityLowerThan(minAvailability);
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(response);
        }
    }
}
