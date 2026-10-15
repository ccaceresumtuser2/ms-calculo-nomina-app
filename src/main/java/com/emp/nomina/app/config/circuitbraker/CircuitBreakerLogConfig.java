package com.emp.nomina.app.config.circuitbraker;

import org.springframework.context.annotation.Configuration;

import com.emp.nomina.app.config.mensajes.Mensajes;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j 
public class CircuitBreakerLogConfig {
	private final CircuitBreakerRegistry registry;
	private final Mensajes mensajes;

	@PostConstruct
	public void registrarLogs() {
		registry.circuitBreaker("empleados").getEventPublisher()
				.onStateTransition(evento -> log.warn(mensajes.get("circuitbreaker.transicion",
						evento.getCircuitBreakerName(),
						evento.getStateTransition().getFromState(),
						evento.getStateTransition().getToState())));
	}
}