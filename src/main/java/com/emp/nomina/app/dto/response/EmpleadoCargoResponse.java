package com.emp.nomina.app.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmpleadoCargoResponse( @JsonProperty ("emp_id") Long empId,
    String nombre,
    String apellido,
    String email,
    String telefono,
    @JsonProperty ("fecha_ingreso") LocalDate fechaIngreso,
    double salario,
    @JsonProperty ("cargo_id") Long cargoId,
    @JsonProperty ("nom_cargo") String nomCargo,
    String descripcion
) {}
