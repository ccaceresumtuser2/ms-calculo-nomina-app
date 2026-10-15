package com.emp.nomina.app.repository.client;

import java.util.List;

import com.emp.nomina.app.dto.response.EmpleadoCargoResponse;

public interface EmpleadoConsumoClient {
    List<EmpleadoCargoResponse> listarEmpleados();
}
