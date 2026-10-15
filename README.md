# ms-calculo-nomina

Microservicio de cálculo de nómina. Consume el servicio de empleados (`https://appempleado.onrender.com/empleados`) mediante un cliente HTTP declarativo (`@HttpExchange`) y expone los datos por una API REST, con configuración de resiliencia basada en Resilience4j.

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (Web MVC, RestClient, Actuator, DevTools)
- Resilience4j (`resilience4j-spring-boot4`): Retry, Circuit Breaker, Bulkhead, Rate Limiter
- springdoc-openapi 3.1.1 (Swagger UI)
- Lombok
- Maven (wrapper incluido)

## Requisitos

- JDK 21
- Acceso al servicio de empleados (por defecto `https://appempleado.onrender.com`)
- Docker (opcional)

## Ejecución

```powershell
.\mvnw clean spring-boot:run
```

En Linux/macOS: `./mvnw clean spring-boot:run`

La aplicación arranca en **http://localhost:8081**.

> Si se agrega una dependencia al `pom.xml`, detener la app y volver a arrancar con `clean`. En VS Code, recargar el proyecto Maven (*Java: Clean Java Language Server Workspace*) para que el IDE tome el nuevo classpath.

## Docker

El `Dockerfile` compila con Maven en una primera etapa y deja solo el JRE 21 (Alpine) en la imagen final, ejecutando como usuario sin privilegios.

```bash
docker build -t ms-calculo-nomina .
docker run -p 8081:8081 ms-calculo-nomina
```

Cualquier propiedad se puede sobrescribir con variables de entorno (mayúsculas, `.` → `_` y sin guiones; `base-url` → `BASEURL`):

```bash
docker run -p 8081:8081 \
  -e SPRING_HTTP_SERVICECLIENT_EMPLEADOS_BASEURL=http://host.docker.internal:8091 \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75" \
  ms-calculo-nomina
```

> Dentro del contenedor, `localhost` es el propio contenedor. Para llegar a un servicio que corre en tu máquina usar `host.docker.internal`.

## CI (GitHub Actions)

`.github/workflows/ci.yml` se ejecuta en cada push y pull request a `main`:

| Job | Qué hace |
|-----|----------|
| `build` | Java 21 (Temurin) → `./mvnw -B verify` (compila y ejecuta los tests) → guarda el JAR como artefacto |
| `docker` | Construye la imagen. En push a `main` la publica en `ghcr.io/<usuario>/<repo>` con las etiquetas `latest` y el SHA del commit |

La publicación usa el `GITHUB_TOKEN` del propio workflow; no hace falta configurar secretos.

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/empleados/all` | Lista los empleados con su cargo, obtenidos del servicio de empleados |
| GET | `/swagger-ui.html` | Documentación interactiva (Swagger UI) |
| GET | `/actuator/health` | Estado de la aplicación y de los circuit breakers |
| GET | `/actuator/circuitbreakers` | Estado de cada circuit breaker |

Formato de respuesta (`ApiResponse`):

```json
{
  "status": 200,
  "message": "Empleados obtenidos correctamente",
  "data": [
    {
      "emp_id": 3,
      "nombre": "Carlos",
      "apellido": "Mendoza",
      "email": "carlos.mendoza@empresa.com",
      "telefono": "987654321",
      "fecha_ingreso": "2015-01-12",
      "salario": 13500.0,
      "cargo_id": 3,
      "nom_cargo": "Gerente General",
      "descripcion": "Dirección estratégica de la empresa"
    }
  ],
  "timestamp": "2026-10-14T12:00:00"
}
```

## Estructura

```
src/main/java/com/emp/nomina/app
├── config
│   ├── circuitbraker   # Logs de eventos del circuit breaker
│   ├── client          # Registro de clientes @HttpExchange (@ImportHttpServices)
│   ├── exception       # GlobalExceptionHandler
│   └── swagger         # Configuración OpenAPI
├── controller          # EmpleadoController
├── dto/response        # ApiResponse, EmpleadoCargoResponse
├── exception           # Excepciones de negocio / servicio externo
├── repository/client   # Cliente HTTP al servicio de empleados
└── service             # EmpleadoCargoService
```

## Configuración

Todo está en `src/main/resources/application.properties`.

### Cliente HTTP

```properties
spring.http.serviceclient.empleados.base-url=https://appempleado.onrender.com
spring.http.clients.connect-timeout=2s
spring.http.clients.read-timeout=5s
```

El grupo `empleados` debe coincidir con el declarado en `HttpClientConfig`:

```java
@ImportHttpServices(group = "empleados", types = EmpleadoCargoRestClient.class)
```

### Resiliencia (instancia `empleados`)

| Patrón | Configuración principal |
|--------|-------------------------|
| Retry | 3 intentos, espera inicial 5 s con backoff exponencial (x2) y aleatorio (±50 %) |
| Circuit Breaker | Ventana por tiempo de 60 s, mínimo 10 llamadas, abre con 50 % de fallos o llamadas lentas (> 2 s), espera 10 s en abierto (backoff hasta 120 s) |
| Bulkhead | Máximo 1000 llamadas concurrentes, espera máxima 500 ms |
| Rate Limiter | 100 llamadas por segundo, sin espera |

Solo estas excepciones cuentan como fallo para Retry y Circuit Breaker:

- `com.emp.nomina.app.exception.ServicioNoDisponibleException`
- `java.io.IOException`
- `java.util.concurrent.TimeoutException`

### Manejo de errores del servicio externo

| Situación | Excepción lanzada |
|-----------|-------------------|
| Sin conexión / timeout | `ServicioNoDisponibleException` |
| Respuesta 5xx | `ServicioNoDisponibleException` |
| Respuesta 404 | `RecursoNoEncontradoException` |
| Otros 4xx | `ServicioExternoException` |
| JSON que no coincide con el DTO | `ServicioExternoException` |
