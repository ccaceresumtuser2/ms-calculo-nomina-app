package com.emp.nomina.app.config.mensajes;

import java.util.Arrays;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

/**
 * Lee los textos de messages.properties.
 * Ejemplo: mensajes.get("retry.exito", 2) -> "✅ Exito despues de 2 reintento(s)"
 */
@Component
@AllArgsConstructor
public class Mensajes {
	private final MessageSource messageSource;

	public String get(String clave, Object... args) {
		// Se pasan como texto para que los números no se formateen con separador de miles (5000 -> "5,000")
		Object[] textos = Arrays.stream(args).map(String::valueOf).toArray();
		return messageSource.getMessage(clave, textos, LocaleContextHolder.getLocale());
	}
}
