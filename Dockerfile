# ===== Etapa 1: compilar con Maven =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo el wrapper y el pom: si no cambian, Docker reutiliza la capa con las dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# sed quita los fin de linea de Windows (CRLF) por si el archivo se edito en Windows
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ===== Etapa 2: imagen final solo con el JRE =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios para no ejecutar la app como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

# Ajusta la memoria de la JVM al limite del contenedor
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
