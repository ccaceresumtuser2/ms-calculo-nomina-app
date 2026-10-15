package com.emp.nomina.app.config.circuitbraker;

import org.springframework.context.annotation.Configuration;

import com.emp.nomina.app.config.mensajes.Mensajes;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class RetryLogConfig {
	private final RetryRegistry registry;
	private final Mensajes mensajes;

	@PostConstruct
	public void registrarLogs() {
		Retry retry = registry.retry("empleados");
		int maxIntentos = retry.getRetryConfig().getMaxAttempts();

		retry.getEventPublisher()
				// Antes de cada reintento: número de reintento y cuánto esperará
				.onRetry(evento -> log.warn(mensajes.get("retry.reintento",
						evento.getNumberOfRetryAttempts(),
						maxIntentos - 1,
						evento.getWaitInterval().toMillis(),
						evento.getLastThrowable().getMessage())))
				// Respondió bien después de haber reintentado
				.onSuccess(evento -> log.info(mensajes.get("retry.exito",
						evento.getNumberOfRetryAttempts())))
				// Se agotaron todos los intentos
				.onError(evento -> log.error(mensajes.get("retry.agotados",
						evento.getNumberOfRetryAttempts())));
	}
}
