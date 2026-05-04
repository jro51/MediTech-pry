# Imagen de Java ligera
FROM eclipse-temurin:17-jdk-alpine
# Copia el archivo JAR generado por Maven (asegúrate de que esté en la carpeta target)
COPY target/*.jar app.jar
# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]