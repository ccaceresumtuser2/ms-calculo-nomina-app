package com.emp.nomina.app.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.emp.nomina.app.config.mensajes.Mensajes;
import com.emp.nomina.app.dto.response.ApiResponse;
import com.emp.nomina.app.exception.RecursoNoEncontradoException;
import com.emp.nomina.app.exception.ServicioExternoException;
import com.emp.nomina.app.exception.ServicioNoDisponibleException;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {
    private final Mensajes mensajes;
    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    */  
   private static ResponseEntity<ApiResponse<Void>> error(String message, HttpStatus status) {
        return ApiResponse.responseApi(null, message, status);
    }

    /** 404: el recurso no existe. */
	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<ApiResponse<Void>> recursoNoEncontrado(RecursoNoEncontradoException e) {
		log.warn("{}", e.getMessage());
		return error(e.getMessage(), HttpStatus.NOT_FOUND);
	}

    /** 503: el :xxxx sigue caído después de los reintentos. */
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ApiResponse<Void>> handleServicioNoDisponibleException(ServicioNoDisponibleException ex) {
        //log.error("Servicio externo no disponible trans los reintentos: {}", ex.getMessage(), ex);
        log.error(mensajes.get("error.servicio-no-disponible.log", ex.getMessage()));
        return error(mensajes.get("error.servicio-no-disponible"), HttpStatus.SERVICE_UNAVAILABLE);
    }

     /** 502: el :xxxx no responde algo inesperado  . */
    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ApiResponse<Void>> handleServicioExternoException(ServicioExternoException ex) {
        //log.error("Error en el Servicio externo: {}", ex.getMessage(), ex);
        log.error(mensajes.get("error.servicio-externo.log", ex.getMessage()));
        return error(mensajes.get("error.servicio-externo", ex.getMessage()), HttpStatus.BAD_GATEWAY);
    }

    /**503 demasiadas llamadas simultaneas al puerto XXXX */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ApiResponse<Void>> handleBullHeadException(BulkheadFullException ex) {
        log.error(mensajes.get("error.bulkhead.log", ex.getMessage()), ex);
        return error(mensajes.get("error.bulkhead"), HttpStatus.SERVICE_UNAVAILABLE);
    }


    	/** 429: se superó el límite de llamadas por segundo. */
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<ApiResponse<Void>> limiteSuperado(RequestNotPermitted e) {
		log.warn(mensajes.get("error.rate-limiter.log", e.getMessage()));
		return error(mensajes.get("error.rate-limiter"), HttpStatus.TOO_MANY_REQUESTS);
	}

    /*cualquier otra excepcion */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        if(ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
           if(status!=null) {
               // log.error("Solicitud invalida ({}): {}", status.value(), ex.getMessage());
                log.error(mensajes.get("error.solicitud-invalida.log", status.value(), ex.getMessage()));
                return error(status.getReasonPhrase(), status);
            }
        }
        log.error(mensajes.get("error.interno.log", ex.getMessage()), ex);
        return error(mensajes.get("error.interno"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
