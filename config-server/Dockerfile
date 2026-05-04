# Imagen de Java ligera
FROM eclipse-temurin:17-jdk-alpine

# INSTALA CURL: Necesario para que el comando del healthcheck funcione
RUN apk add --no-cache curl

# Copia el archivo JAR generado por Maven
COPY target/*.jar app.jar

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]