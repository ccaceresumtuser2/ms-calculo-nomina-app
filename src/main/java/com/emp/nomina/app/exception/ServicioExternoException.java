package com.emp.nomina.app.exception;

public class ServicioExternoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServicioExternoException(String mensaje,Throwable causa) {
        super(mensaje);
    }    
}
