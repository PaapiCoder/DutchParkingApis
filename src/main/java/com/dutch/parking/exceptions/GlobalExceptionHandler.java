package com.dutch.parking.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
	                                                           HttpStatusCode status, WebRequest request) {

    Map<String, List<String>> body = new HashMap<>();

    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();

    body.put("errors", errors);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RegistrationNotFoundException.class)
  public ResponseEntity<Map<String,String>> resourceNotFoundException(RegistrationNotFoundException ex, WebRequest request) {
    Map<String, String> body = new HashMap<>();

    body.put("message", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<Map<String,String>> resourceNotFoundException(AlreadyRegisteredException ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, List<String>>> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();

    ex.getConstraintViolations().forEach(cv -> errors.add(cv.getMessage()));

    Map<String, List<String>> result = new HashMap<>();
    result.put("errors", errors);

    return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
  }
}
