package com.emp.nomina.app.config.httpclient;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

import com.emp.nomina.app.repository.client.EmpleadoCargoRestClient;

// Registra las interfaces @HttpExchange del grupo "empleados" como beans.
// La URL base se toma de spring.http.serviceclient.empleados.base-url
@Configuration
@ImportHttpServices(group = "empleados", types = EmpleadoCargoRestClient.class)
public class HttpClientConfig {
}
