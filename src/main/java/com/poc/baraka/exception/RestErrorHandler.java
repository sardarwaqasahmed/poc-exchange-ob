package com.poc.baraka.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Waqas Ahmed
 */
@RestControllerAdvice
@SuppressWarnings("all")
public class RestErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RestErrorHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RestResponse> handleStatusException(ResponseStatusException ex, WebRequest request) {
        log.error(ex.getReason(), ex);
        return RestResponse.builder()
                .exception(ex)
                .path(getPath(request))
                .entity();
    }

    @ExceptionHandler(MatchingEngineException.class)
    public ResponseEntity<RestResponse> handleMatchingEngineException(MatchingEngineException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return RestResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getErrorList().toString())
                .path(getPath(request))
                .entity();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InvalidArgumentErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<Param> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            Param param = new Param();
            String fieldName = ((FieldError) error).getField();
            String reason = error.getDefaultMessage();
            param.setName(fieldName);
            param.setReason(reason);
            errors.add(param);
        });

        InvalidArgumentErrorResponse body = InvalidArgumentErrorResponse.builder()
                .type("validation-error")
                .title("Your request parameters didn't validate.")
                .detail("Request has invalid parameters")
                .paramList(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<RestResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        log.error(ex.getLocalizedMessage(), ex);
        return RestResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(ex.getMessage())
                .path(getPath(request))
                .entity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return RestResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Server encountered an error")
                .path(getPath(request))
                .entity();
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
