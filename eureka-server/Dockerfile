# Imagen de Java ligera
FROM eclipse-temurin:17-jdk-alpine

# Es obligatorio para que el healthcheck de Docker funcione
RUN apk add --no-cache curl

# Copia el archivo JAR generado por Maven
COPY target/*.jar app.jar

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]