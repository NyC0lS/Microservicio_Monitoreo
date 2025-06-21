# Configuración de Base de Datos - Microservicio de Monitoreo

## 📋 Resumen

El microservicio de monitoreo utiliza **PostgreSQL** como base de datos para almacenar eventos de monitoreo y logging del sistema.

## 🗄️ Base de Datos

- **Nombre**: `monitoreo_db`
- **Motor**: PostgreSQL 12+
- **Puerto**: 5432 (por defecto)
- **Usuario**: postgres
- **Contraseña**: postgres

## ⚙️ Configuración Inicial

### 1. Instalar PostgreSQL

#### En Windows:
```bash
# Descargar desde: https://www.postgresql.org/download/windows/
# O usar Chocolatey:
choco install postgresql
```

#### En macOS:
```bash
# Usar Homebrew:
brew install postgresql
brew services start postgresql
```

#### En Ubuntu/Debian:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Crear la Base de Datos

```bash
# Conectar como usuario postgres
sudo -u postgres psql

# Crear la base de datos
CREATE DATABASE monitoreo_db;

# Verificar que se creó
\l

# Salir
\q
```

### 3. Ejecutar Script de Inicialización

```bash
# Navegar al directorio del script
cd Microservicio-Monitoreo/database/

# Ejecutar el script
psql -U postgres -d monitoreo_db -f init.sql
```

### 4. Verificar la Configuración

```bash
# Conectar a la base de datos
psql -U postgres -d monitoreo_db

# Verificar tablas
\dt

# Verificar índices
\di

# Verificar vistas
\dv

# Salir
\q
```

## 🏗️ Estructura de la Base de Datos

### Tabla: `eventos_monitoreo`
Almacena eventos generales de monitoreo del sistema.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| event_type | VARCHAR(100) | Tipo de evento |
| message | VARCHAR(1000) | Mensaje del evento |
| timestamp | TIMESTAMP | Fecha y hora del evento |
| service_name | VARCHAR(100) | Nombre del servicio |
| level | VARCHAR(20) | Nivel del evento (INFO, WARN, ERROR) |
| metadata | JSONB | Metadatos adicionales |
| user_id | VARCHAR(100) | ID del usuario |
| session_id | VARCHAR(100) | ID de la sesión |

## 🔧 Configuración de la Aplicación

### Archivo: `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/monitoreo_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Variables de Entorno (Opcional)

```bash
# Configurar variables de entorno
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=monitoreo_db
export DB_USER=postgres
export DB_PASSWORD=postgres
```

## 📊 Índices y Optimización

La base de datos incluye índices optimizados para:

- Búsquedas por timestamp (ordenadas descendente)
- Filtros por tipo de evento
- Filtros por servicio de origen
- Filtros por usuario
- Búsquedas de texto completo en mensajes
- Consultas compuestas frecuentes

## 👁️ Vistas Útiles

### `vista_eventos_criticos`
Muestra todos los eventos críticos del sistema.

## 🛠️ Mantenimiento

### Limpiar Eventos Antiguos

```sql
-- Limpiar eventos de más de 90 días
SELECT limpiar_eventos_antiguos(90);

-- Limpiar eventos de más de 30 días
SELECT limpiar_eventos_antiguos(30);
```

### Estadísticas de la Base de Datos

```sql
-- Contar eventos por tipo
SELECT event_type, COUNT(*) FROM eventos_monitoreo GROUP BY event_type;

-- Eventos de las últimas 24 horas
SELECT COUNT(*) FROM eventos_monitoreo 
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '1 day';

-- Eventos por nivel de severidad
SELECT level, COUNT(*) FROM eventos_monitoreo GROUP BY level;

-- Eventos por servicio
SELECT service_name, COUNT(*) FROM eventos_monitoreo GROUP BY service_name;
```

## 🔍 Troubleshooting

### Error de Conexión
```bash
# Verificar que PostgreSQL esté ejecutándose
sudo systemctl status postgresql

# Verificar que el puerto esté abierto
netstat -an | grep 5432
```

### Error de Autenticación
```bash
# Editar pg_hba.conf para permitir conexiones locales
sudo nano /etc/postgresql/*/main/pg_hba.conf

# Agregar línea:
local   all             postgres                                peer
```

### Error de Permisos
```bash
# Otorgar permisos al usuario
sudo -u postgres psql
GRANT ALL PRIVILEGES ON DATABASE monitoreo_db TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
```

## 📚 Recursos Adicionales

- [Documentación PostgreSQL](https://www.postgresql.org/docs/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hibernate](https://hibernate.org/)

## 🔄 Migraciones

Para futuras actualizaciones de la base de datos, crear scripts de migración en:
```
Microservicio-Monitoreo/database/migrations/
```

Ejemplo:
```sql
-- V1.1__add_new_column.sql
ALTER TABLE eventos_monitoreo ADD COLUMN priority INTEGER DEFAULT 0;
``` 