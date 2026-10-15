package com.emp.nomina.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emp.nomina.app.config.mensajes.Mensajes;
import com.emp.nomina.app.dto.response.ApiResponse;
import com.emp.nomina.app.dto.response.EmpleadoCargoResponse;
import com.emp.nomina.app.service.EmpleadoCargoService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/empleados")
@AllArgsConstructor 
@CrossOrigin("*")
@Slf4j 
public class EmpleadoController {
    private final EmpleadoCargoService service;
    private final Mensajes mensajes;
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EmpleadoCargoResponse>>> listarEmpleados() {
        try{
            log.info(mensajes.get("empleados.listar.inicio"));
            List<EmpleadoCargoResponse> empleados = service.listarEmpleados();
            log.info(mensajes.get("empleados.listar.ok.log"));
            return ApiResponse.responseApi(empleados, mensajes.get("empleados.listar.ok"), HttpStatus.OK);
        }
        catch (Exception e) {
            log.warn(mensajes.get("empleados.listar.fallo.log", e.getMessage()));
            throw e;   // relanzar para que @Retryable haga el reintento
        }
    }

    @GetMapping("/salario-mayor-8000")
    public ResponseEntity<ApiResponse<List<EmpleadoCargoResponse>>> listarEmpleadosMayores8000() {
        List<EmpleadoCargoResponse> empleados = service.listarEmpleadosMayores8000();
        log.info(mensajes.get("empleados.mayores-8000.log", empleados.size()));
        return ApiResponse.responseApi(empleados, mensajes.get("empleados.mayores-8000"), HttpStatus.OK);
    }

}
