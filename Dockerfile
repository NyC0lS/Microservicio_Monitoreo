# Etapa 1: compilar el jar
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Compilar la aplicación
RUN mvn clean package -DskipTests
RUN ls -l /app/target/

# Etapa 2: empaquetar la app
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para logs
RUN mkdir -p /var/log/monitoreo

# Exponer puerto
EXPOSE 8080

# Configurar variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 