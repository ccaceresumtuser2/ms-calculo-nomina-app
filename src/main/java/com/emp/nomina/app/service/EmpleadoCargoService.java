package com.emp.nomina.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emp.nomina.app.dto.response.EmpleadoCargoResponse;
import com.emp.nomina.app.repository.client.EmpleadoConsumoClient;

import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class EmpleadoCargoService {
    private final EmpleadoConsumoClient client;
    public List<EmpleadoCargoResponse> listarEmpleados() {
        return listarEmpleadosMayores8000();
    }
    /**
     * Filtra los empleados con salario mayor a 8000
     * logica de negocios
     * @return
     */
    public List<EmpleadoCargoResponse> listarEmpleadosMayores8000() {
        return client.listarEmpleados().stream()       // List -> Stream
                .filter(e -> e.salario() > 8000)       // en un record el getter es salario(), no getSalario()
                .toList();                             // Stream -> List
    }
    
}
