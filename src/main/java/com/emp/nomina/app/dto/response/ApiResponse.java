package com.emp.nomina.app.dto.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude (JsonInclude.Include.NON_NULL) //omite los campos nulos en la respuesta JSON
public record ApiResponse<T>(
    int status, 
    String message, 
    T data,
    LocalDateTime timestamp
) {
    /**
     * generar una respuesta de API con un código de estado, mensaje y datos generico.
     * @param <T>
     * @param data
     * @param message
     * @param status
     * @return
     */
    public static <T> ResponseEntity<ApiResponse<T>> responseApi(T data,String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse<T>(status.value(), message, data, LocalDateTime.now()));
    }    
}
