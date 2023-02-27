package org.identifiers.cloud.ws.linkchecker.api.controllers;

import org.identifiers.cloud.ws.linkchecker.api.ApiCentral;
import org.identifiers.cloud.ws.linkchecker.api.responses.ServiceResponse;
import org.identifiers.cloud.ws.linkchecker.daemons.PeriodicCheckRequesterResolutionBaseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;


@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String now() {
        // Date for easier finding related entries in log files
        Calendar cal = Calendar.getInstance();
        return sdf.format(cal.getTime());
    }

    @ExceptionHandler(value = { RuntimeException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        // Last hope for logging of unforeseen errors
        // Also a way to make all responses to be of type ServiceResponse
        logger.error("Unforeseen exception", ex);
        ServiceResponse responseBody = new ServiceResponse()
                .setApiVersion(ApiCentral.apiVersion)
                .setErrorMessage(String.format("Unforeseen exception at %s: %s", now(), ex.getMessage()));
        return handleExceptionInternal(ex, responseBody, new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
