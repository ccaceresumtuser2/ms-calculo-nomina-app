package com.emp.nomina.app.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import io.swagger.v3.oas.models.Components;
import com.emp.nomina.app.config.mensajes.Mensajes;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI apiInfo(Mensajes mensajes) {
		return new OpenAPI()
				.info(new Info()
						.title(mensajes.get("swagger.titulo"))
						.description(mensajes.get("swagger.descripcion"))
						.version("1.0")
						.contact(new Contact().name(mensajes.get("swagger.contacto"))))
				.addServersItem(new Server().url("http://localhost:8080").description(mensajes.get("swagger.servidor.local")));
				// Botón "Authorize" en Swagger UI para enviar "Authorization: Bearer <token>"
                /*
				.components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Token obtenido en POST http://localhost:8091/token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                 */
	}
}
