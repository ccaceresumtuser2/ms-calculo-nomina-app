package com.emp.nomina.app.repository.client;

import java.util.List;

//import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.emp.nomina.app.config.mensajes.Mensajes;
import com.emp.nomina.app.dto.response.EmpleadoCargoResponse;
import com.emp.nomina.app.exception.RecursoNoEncontradoException;
import com.emp.nomina.app.exception.ServicioExternoException;
import com.emp.nomina.app.exception.ServicioNoDisponibleException;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
//import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component 
@AllArgsConstructor 
@Slf4j 
public class EmpleadoConsumoClientImp implements EmpleadoConsumoClient {
    private final EmpleadoCargoRestClient client;
    private final Mensajes mensajes;
   	/** Última lista correcta recibida, para usarla en el fallback. */
    //private final AtomicReference<List<EmpleadoCargoResponse>> ultimaRespuesta = new AtomicReference<>();
		@Retry(name = "empleados", fallbackMethod = "fallbackListarEmpleados")
	@CircuitBreaker(name = "empleados")
	@RateLimiter(name = "empleados")
	@Bulkhead(name = "empleados")
	@Override 
    public List<EmpleadoCargoResponse> listarEmpleados() {
        try{
            log.info(mensajes.get("cliente.empleados.consumiendo"));
            List<EmpleadoCargoResponse> empleados = client.listarEmpleadoCargo();
            //ultimaRespuesta.set(empleados);
            return empleados;
        }
        catch (ResourceAccessException e) {
			// Sin respuesta: servicio caído, conexión rechazada o timeout
			log.warn(mensajes.get("cliente.empleados.sin-conexion.log", e.getMessage()));
			throw new ServicioNoDisponibleException(mensajes.get("cliente.empleados.sin-conexion"), e);

		}
        catch (HttpServerErrorException e) {
			// 5xx: error del lado del servicio, puede ser temporal
			log.warn(mensajes.get("cliente.empleados.error-servidor.log", e.getStatusCode().value()));
			throw new ServicioNoDisponibleException(
					mensajes.get("cliente.empleados.error-servidor", e.getStatusCode().value()), e);

		} catch (HttpClientErrorException.NotFound e) {
			// 404: no existe, reintentar no sirve
			log.warn(mensajes.get("cliente.empleados.no-encontrado.log"));
			throw new RecursoNoEncontradoException(mensajes.get("cliente.empleados.no-encontrado"));

		} catch (RestClientResponseException e) {
			// Otros 4xx (400, 401, 403...): nuestra petición es incorrecta
			log.error(mensajes.get("cliente.empleados.rechazo.log",
					e.getStatusCode().value(), e.getResponseBodyAsString()));
			throw new ServicioExternoException(
					mensajes.get("cliente.empleados.rechazo", e.getStatusCode().value()), e);

		} catch (RestClientException e) {
			// Hubo respuesta, pero no se pudo leer (JSON distinto, etc.)
			log.error(mensajes.get("cliente.empleados.respuesta-invalida.log"), e);
			throw new ServicioExternoException(mensajes.get("cliente.empleados.respuesta-invalida"), e);
		}        
    }

    //====================== FALLBACK ==========================
    //Deben tener el misomo tipo de retorno y parámetros que el método original listarEmpleados()
    private List<EmpleadoCargoResponse>fallbackListarEmpleados(ServicioNoDisponibleException ex) {
        return usarUltimaRespuestaOFallar(mensajes.get("fallback.motivo.reintentos"), ex);
    }
    /** El circuito está abierto: no se llamó al :8091. */
	private List<EmpleadoCargoResponse> fallbackListarEmpleados(CallNotPermittedException e) {
			return usarUltimaRespuestaOFallar(mensajes.get("fallback.motivo.circuito"), e);
	}

    /**
     * Usar la última respuesta conocida o lanzar una excepción de servicio no disponible.
     * @param motivo
     * @param ex
     * @return
     */
    private List<EmpleadoCargoResponse> usarUltimaRespuestaOFallar(String motivo, Exception ex) {
			// Con el circuito abierto no se ejecuta listarEmpleados(): se vuelve a exigir el token aquí
     		/*
			 * List<EmpleadoCargoResponse> cache = ultimaRespuesta.get(); if (cache != null)
			 * { log.
			 * warn("⚠️ Fallback ({}): devolviendo ultima respuesta conocida ({} empleados)"
			 * , motivo, cache.size()); return cache; }
			 */
			log.error(mensajes.get("fallback.sin-datos.log", motivo));
			throw new ServicioNoDisponibleException(mensajes.get("fallback.no-disponible", motivo), ex);
    }

}
