package com.emp.nomina.app.exception;

public class ServicioNoDisponibleException extends ServicioExternoException {
    private static final long serialVersionUID = 1L;

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
}
