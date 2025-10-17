package com.vive.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();

        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object exception = request.getAttribute("javax.servlet.error.exception");

        errorDetails.put("status", status != null ? status : "unknown");
        errorDetails.put("message", message != null ? message.toString() : "An error occurred");
        errorDetails.put("path", request.getRequestURI());

        if (exception != null) {
            log.error("Error occurred: ", (Throwable) exception);
            errorDetails.put("exception", exception.getClass().getName());
        }

        log.error("Error page accessed: status={}, path={}, message={}", status, request.getRequestURI(), message);

        Integer statusCode = status != null ? Integer.valueOf(status.toString()) : 500;
        return ResponseEntity.status(HttpStatus.valueOf(statusCode)).body(errorDetails);
    }
}
