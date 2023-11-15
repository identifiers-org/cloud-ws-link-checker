package org.identifiers.cloud.ws.linkchecker.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.strategies
 * Timestamp: 2018-05-28 8:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This class implements a simple link checking strategy based on HTTP GET request.
 */
@Component
@Scope("prototype")
public class SimpleLinkCheckerStrategy implements LinkCheckerStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLinkCheckerStrategy.class);


    final HttpClient linkCheckerHttpClient;
    SimpleLinkCheckerStrategy(@Autowired HttpClient linkCheckerHttpClient) {
        this.linkCheckerHttpClient = linkCheckerHttpClient;
    }

    @Override
    public LinkCheckerReport check(URL checkingUrl, boolean accept401or403) {
        LinkCheckerReport report = new LinkCheckerReport()
                .setUrl(checkingUrl.toString())
                .setTimestamp(new Timestamp(System.currentTimeMillis()));

        URI uri = URI.create(checkingUrl.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .uri(uri).build();
        HttpResponse<?> response;
        try {
            response = linkCheckerHttpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            report.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            report.setUrlAssessmentOk(false);
            logger.info("[HTTP NaN] Exception when checking {}", report.getUrl());
            logger.info("             message {}", e.getMessage());
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return report;
        }

        report.setHttpStatus(response.statusCode());

        HttpStatus responseStatus = HttpStatus.valueOf(response.statusCode());
        if (responseStatus.is2xxSuccessful()) {
            report.setUrlAssessmentOk(true);
            logger.info("[HTTP {}] ACCEPTED AS OK For URL {}",
                    report.getHttpStatus(), report.getUrl());
        } else if (responseStatus.is3xxRedirection()) {
            report.setUrlAssessmentOk(true);
            logger.info("[HTTP {}] REDIRECT ACCEPTED AS OK For URL {}",
                    report.getHttpStatus(), report.getUrl());
        } else if (
                (responseStatus == HttpStatus.FORBIDDEN || responseStatus == HttpStatus.UNAUTHORIZED)
                        && accept401or403
        ) {
            report.setUrlAssessmentOk(true);
            logger.info("[HTTP {}] AUTH RESPONSE ACCEPTED AS OK For PROTECTED URL {}",
                    report.getHttpStatus(), report.getUrl());
        } else {
            report.setUrlAssessmentOk(false);
            logger.info("[HTTP {}] NOT ACCEPTED AS OK For URL {}",
                    report.getHttpStatus(), report.getUrl());
        }
        return report;
    }
}
