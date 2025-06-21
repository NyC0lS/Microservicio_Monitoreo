# Endpoints CRUD - Microservicio de Monitoreo

## 📋 Resumen

El microservicio de monitoreo proporciona una API REST completa con endpoints CRUD para gestionar eventos de monitoreo y logging. Todos los endpoints están organizados por funcionalidad y siguen las mejores prácticas REST.

## 🏗️ Arquitectura de Controladores

### Controladores Principales

1. **EventoMonitoreoController** - `/api/monitoreo/eventos`
   - CRUD completo para eventos generales de monitoreo

2. **MonitoreoController** - `/api/monitoreo`
   - Endpoints de logging y monitoreo general
   - Health checks básicos
   - Información del servicio

## 🔗 Endpoints por Categoría

### 📊 Sistema General

#### Información del Servicio
```
GET /api/monitoreo/info
```
Obtiene información general del microservicio de monitoreo.

**Respuesta:**
```json
{
  "name": "Microservicio de Monitoreo",
  "description": "Servicio para monitoreo y logging de aplicaciones",
  "version": "1.0.0",
  "timestamp": "2024-01-01T12:00:00",
  "systemInfo": {
    "javaVersion": "17.0.1",
    "springVersion": "3.0.0",
    "uptime": "2h 30m"
  }
}
```

#### Health Check
```
GET /api/monitoreo/health
```
Verifica el estado de salud del microservicio.

**Respuesta:**
```json
{
  "status": "UP",
  "service": "monitoreo_service",
  "timestamp": "2024-01-01T12:00:00",
  "version": "1.0.0",
  "components": {
    "database": "UP",
    "logging": "UP",
    "events": "UP"
  }
}
```

#### Test Endpoint
```
GET /api/monitoreo/test
```
Endpoint de prueba para verificar que el servicio esté funcionando.

**Respuesta:**
```
Microservicio de monitoreo funcionando correctamente
```

### 📝 Eventos de Monitoreo

#### CREATE
```
POST /api/monitoreo/eventos
POST /api/monitoreo/eventos/batch
```

#### READ
```
GET /api/monitoreo/eventos?page=0&size=20&sortBy=timestamp&sortDir=desc
GET /api/monitoreo/eventos/{id}
GET /api/monitoreo/eventos/tipo/{eventType}
GET /api/monitoreo/eventos/servicio/{serviceName}
GET /api/monitoreo/eventos/nivel/{level}
GET /api/monitoreo/eventos/usuario/{userId}
GET /api/monitoreo/eventos/fecha?inicio=2024-01-01T00:00:00&fin=2024-01-31T23:59:59
GET /api/monitoreo/eventos/criticos
GET /api/monitoreo/eventos/recientes?horas=24
```

#### UPDATE
```
PUT /api/monitoreo/eventos/{id}
PATCH /api/monitoreo/eventos/{id}
```

#### DELETE
```
DELETE /api/monitoreo/eventos/{id}
DELETE /api/monitoreo/eventos/tipo/{eventType}
DELETE /api/monitoreo/eventos/fecha?inicio=2024-01-01T00:00:00&fin=2024-01-31T23:59:59
DELETE /api/monitoreo/eventos/todos
```

#### Estadísticas
```
GET /api/monitoreo/eventos/estadisticas
GET /api/monitoreo/eventos/health
```

### 📊 Logging y Monitoreo

#### Logging de Eventos
```
POST /api/monitoreo/eventos
```

#### Consultas de Logs
```
GET /api/monitoreo/eventos?nivel=ERROR&servicio=auth&horas=24
GET /api/monitoreo/eventos/criticos
GET /api/monitoreo/eventos/usuario/{userId}
GET /api/monitoreo/eventos/servicio/{serviceName}
```

#### Health Checks Específicos
```
GET /api/monitoreo/health
GET /api/monitoreo/eventos/estadisticas
```

## 📋 Ejemplos de Uso

### Crear un Evento de Monitoreo

```bash
POST /api/monitoreo/eventos
Content-Type: application/json

{
  "eventType": "USER_LOGIN",
  "message": "Usuario inició sesión exitosamente",
  "level": "INFO",
  "serviceName": "auth-service",
  "userId": "user123",
  "metadata": {
    "ip": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "sessionId": "sess_abc123"
  }
}
```

### Obtener Eventos Críticos

```bash
GET /api/monitoreo/eventos/criticos?horas=24
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": 1,
      "eventType": "DATABASE_ERROR",
      "message": "Error de conexión a base de datos",
      "level": "ERROR",
      "timestamp": "2024-01-01T12:00:00",
      "serviceName": "db-service",
      "metadata": {
        "errorCode": "DB_CONN_001",
        "retryCount": 3
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Obtener Estadísticas

```bash
GET /api/monitoreo/eventos/estadisticas
```

**Respuesta:**
```json
{
  "totalEventos": 1250,
  "eventosPorNivel": {
    "INFO": 800,
    "WARN": 300,
    "ERROR": 150
  },
  "eventosPorServicio": {
    "auth-service": 400,
    "db-service": 300,
    "api-gateway": 550
  },
  "eventosRecientes": {
    "ultimaHora": 25,
    "ultimas24Horas": 180,
    "ultimaSemana": 850
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### Crear Múltiples Eventos

```bash
POST /api/monitoreo/eventos/batch
Content-Type: application/json

[
  {
    "eventType": "API_REQUEST",
    "message": "Solicitud API procesada",
    "level": "INFO",
    "serviceName": "api-gateway",
    "metadata": {
      "endpoint": "/api/users",
      "method": "GET",
      "responseTime": 150
    }
  },
  {
    "eventType": "CACHE_MISS",
    "message": "Cache miss en Redis",
    "level": "WARN",
    "serviceName": "cache-service",
    "metadata": {
      "key": "user:123",
      "cacheType": "redis"
    }
  }
]
```

## 🔍 Filtros y Búsquedas

### Filtros Disponibles

- **Por tipo de evento**: `/api/monitoreo/eventos/tipo/{eventType}`
- **Por servicio**: `/api/monitoreo/eventos/servicio/{serviceName}`
- **Por nivel**: `/api/monitoreo/eventos/nivel/{level}`
- **Por usuario**: `/api/monitoreo/eventos/usuario/{userId}`
- **Por fecha**: `/api/monitoreo/eventos/fecha?inicio=2024-01-01T00:00:00&fin=2024-01-31T23:59:59`
- **Eventos críticos**: `/api/monitoreo/eventos/criticos`
- **Eventos recientes**: `/api/monitoreo/eventos/recientes?horas=24`

### Paginación

Todos los endpoints de listado soportan paginación:

```
GET /api/monitoreo/eventos?page=0&size=20&sortBy=timestamp&sortDir=desc
```

**Parámetros:**
- `page`: Número de página (0-based)
- `size`: Tamaño de la página (máximo 100)
- `sortBy`: Campo para ordenar (timestamp, eventType, serviceName, level)
- `sortDir`: Dirección del ordenamiento (asc, desc)

## 📊 Códigos de Respuesta

### Éxito
- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `204 No Content`: Operación exitosa sin contenido

### Error
- `400 Bad Request`: Datos de entrada inválidos
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error interno del servidor

## 🔒 Seguridad

- Todos los endpoints soportan CORS con origen `*`
- Los datos sensibles se enmascaran automáticamente
- Se registran todos los accesos para auditoría

## 📈 Monitoreo y Logging

- Todos los endpoints generan logs estructurados
- Se registran métricas de rendimiento
- Los errores se capturan y registran automáticamente
- Se implementa enmascaramiento de datos sensibles

## 🚀 Despliegue

### Variables de Entorno Recomendadas

```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=monitoreo_db
DB_USER=postgres
DB_PASSWORD=postgres

# Logging
LOG_LEVEL=INFO
LOG_FILE=monitoreo.log

# Aplicación
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### Health Check para Load Balancer

```bash
curl -f http://localhost:8080/api/monitoreo/health
```

## 📚 Recursos Adicionales

- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL](https://www.postgresql.org/docs/) 