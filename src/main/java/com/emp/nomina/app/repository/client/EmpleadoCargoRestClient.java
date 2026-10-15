package com.emp.nomina.app.repository.client;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.emp.nomina.app.dto.response.EmpleadoCargoResponse;

@HttpExchange("/empleados")
public interface EmpleadoCargoRestClient {
    @GetExchange 
    List<EmpleadoCargoResponse> listarEmpleadoCargo();
}
