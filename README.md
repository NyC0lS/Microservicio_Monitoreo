# 🔍 **Microservicio de Monitoreo - OfertaYa**

## 📋 **Descripción**

Microservicio de monitoreo y observabilidad para la plataforma de subastas **OfertaYa**. Este servicio proporciona capacidades completas de logging, métricas, trazabilidad y auditoría para todos los microservicios de la plataforma.

## 🏗️ **Arquitectura**

```
┌─────────────────────────────────────────────────────────────┐
│                    Microservicio de Monitoreo               │
├─────────────────────────────────────────────────────────────┤
│  📊 Métricas (Prometheus)  │  📝 Logs (Loki)               │
│  🔗 Correlation ID         │  📈 Dashboards (Grafana)      │
│  🔒 Auditoría              │  ⚠️  Alertas (Alertmanager)   │
│  🛡️  Enmascaramiento       │  🚀 Observabilidad Completa   │
└─────────────────────────────────────────────────────────────┘
```

## ✨ **Características Principales**

### 🔍 **Observabilidad Completa**
- **Logging estructurado** en formato JSON
- **Métricas personalizadas** con Micrometer y Prometheus
- **Trazabilidad** con Correlation ID entre microservicios
- **Dashboards** en Grafana para visualización

### 🛡️ **Seguridad y Auditoría**
- **Enmascaramiento automático** de datos sensibles
- **Logs de auditoría** separados con retención extendida
- **Logs de seguridad** para eventos críticos
- **Validación** y manejo de errores robusto

### 📊 **Monitoreo y Alertas**
- **Stack PLG** (Promtail + Loki + Grafana)
- **Alertas configurables** con notificaciones por email
- **Métricas de rendimiento** y latencia
- **Monitoreo de recursos** del sistema

### 🔧 **Configuración Multi-Entorno**
- **Desarrollo:** Configuración optimizada para debugging
- **Staging:** Configuración para pruebas de integración
- **Producción:** Configuración optimizada para rendimiento

## 🚀 **Inicio Rápido**

### **Prerrequisitos**
- Java 21+
- Maven 3.8+
- Docker y Docker Compose
- Git

### **1. Clonar el repositorio**
```bash
git clone <repository-url>
cd Microservicio-Monitoreo
```

### **2. Compilar el proyecto**
```bash
cd monitoreo
mvn clean compile
```

### **3. Ejecutar con Docker Compose**
```bash
# Desarrollo
docker-compose up -d

# Staging
docker-compose -f docker-compose.staging.yml up -d
```

### **4. Verificar servicios**
```bash
# Microservicio
curl http://localhost:8080/actuator/health

# Grafana
curl http://localhost:3000

# Prometheus
curl http://localhost:9090
```

## 📁 **Estructura del Proyecto**

```
Microservicio-Monitoreo/
├── 📁 monitoreo/                    # Código fuente principal
│   ├── 📁 src/main/java/
│   │   └── 📁 com/monitoreo/
│   │       ├── 📁 config/           # Configuraciones
│   │       ├── 📁 controller/       # Controladores REST
│   │       ├── 📁 service/          # Lógica de negocio
│   │       ├── 📁 model/            # Entidades
│   │       ├── 📁 repository/       # Repositorios JPA
│   │       ├── 📁 dto/              # DTOs
│   │       ├── 📁 exception/        # Excepciones personalizadas
│   │       └── 📁 validation/       # Validadores
│   └── 📁 src/main/resources/
│       ├── application.yml          # Configuración principal
│       ├── application-staging.yml  # Configuración staging
│       ├── application-production.yml # Configuración producción
│       └── logback-spring.xml       # Configuración de logging
├── 📁 config/                       # Configuraciones de infraestructura
│   ├── 📁 grafana/                  # Dashboards y provisioning
│   ├── 📁 prometheus/               # Configuración de métricas
│   └── 📁 loki-config.yaml          # Configuración de logs
├── 📁 scripts/                      # Scripts de utilidad
├── 📁 database/                     # Scripts de base de datos
├── docker-compose.yml               # Stack completo para desarrollo
├── docker-compose.staging.yml       # Stack para staging
└── 📚 README_*.md                   # Documentación específica
```

## 🔧 **Configuración**

### **Variables de Entorno**
```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# Logging
LOGGING_LEVEL_COM_MONITOREO=DEBUG
LOGGING_LEVEL_ROOT=INFO

# Métricas
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

### **Puertos por Defecto**
- **Microservicio:** 8080
- **Grafana:** 3000
- **Prometheus:** 9090
- **Loki:** 3100
- **Alertmanager:** 9093

## 📊 **Endpoints Principales**

### **API de Eventos**
- `POST /api/eventos` - Crear evento de monitoreo
- `GET /api/eventos` - Listar eventos con paginación
- `GET /api/eventos/{id}` - Obtener evento por ID
- `DELETE /api/eventos/{id}` - Eliminar evento

### **API de Pruebas**
- `GET /api/test/correlation/logs` - Probar correlation ID
- `GET /api/test/correlation/info` - Información de correlation
- `POST /api/test/correlation/propagate` - Probar propagación

### **Actuator**
- `GET /actuator/health` - Estado del servicio
- `GET /actuator/metrics` - Métricas disponibles
- `GET /actuator/prometheus` - Métricas en formato Prometheus

## 🧪 **Pruebas**

### **Pruebas Unitarias**
```bash
cd monitoreo
mvn test
```

### **Pruebas de Correlation ID**
```bash
# Ejecutar script de pruebas
./scripts/test-correlation.sh
```

### **Pruebas de Integración**
```bash
# Verificar stack completo
docker-compose up -d
./scripts/validate-metrics.sh
```

## 📈 **Monitoreo y Métricas**

### **Métricas Disponibles**
- **Eventos creados/consultados/eliminados**
- **Errores de validación y sistema**
- **Latencia de operaciones**
- **Logs por nivel (INFO, WARN, ERROR, DEBUG)**
- **Uso de memoria y CPU**

### **Dashboards de Grafana**
- **Métricas del Sistema:** CPU, memoria, disco
- **Métricas de Aplicación:** Eventos, errores, latencia
- **Logs de Monitoreo:** Visualización de logs estructurados
- **Trazabilidad:** Flujo de peticiones con Correlation ID

## 🔒 **Seguridad**

### **Enmascaramiento de Datos**
- **Emails:** `usuario@dominio.com` → `u***@d***.com`
- **Teléfonos:** `+1234567890` → `+1***67890`
- **Tarjetas:** `1234-5678-9012-3456` → `****-****-****-3456`
- **IPs:** `192.168.1.100` → `192.168.*.*`

### **Logs de Auditoría**
- **Eventos de seguridad** separados
- **Retención extendida** (365 días)
- **Enmascaramiento automático** de datos sensibles

## 🚀 **Despliegue**

### **Desarrollo Local**
```bash
# Compilar y ejecutar
mvn spring-boot:run

# Con Docker
docker-compose up -d
```

### **Staging**
```bash
# Usar configuración de staging
docker-compose -f docker-compose.staging.yml up -d
```

### **Producción**
```bash
# Configurar variables de producción
export SPRING_PROFILES_ACTIVE=production
export LOGGING_LEVEL_ROOT=WARN

# Ejecutar con configuración de producción
java -jar monitoreo.jar --spring.profiles.active=production
```

## 📚 **Documentación**

### **Documentación Específica**
- **[README_CORRELATION_ID.md](README_CORRELATION_ID.md)** - Sistema de trazabilidad
- **[README_METRICAS.md](README_METRICAS.md)** - Instrumentación de métricas
- **[README_OBSERVABILIDAD.md](README_OBSERVABILIDAD.md)** - Stack PLG
- **[README_PRODUCCION.md](README_PRODUCCION.md)** - Configuración de producción
- **[README_DATA_MASKING.md](README_DATA_MASKING.md)** - Enmascaramiento de datos
- **[README_ENDPOINTS_CRUD.md](README_ENDPOINTS_CRUD.md)** - API REST
- **[README_DATABASE.md](README_DATABASE.md)** - Configuración de base de datos

## 🤝 **Contribución**

### **Flujo de Trabajo**
1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### **Estándares de Código**
- **Java:** Java 21 con Spring Boot 3.x
- **Logging:** SLF4J con Logback
- **Métricas:** Micrometer con Prometheus
- **Documentación:** JavaDoc y README actualizados

## 📞 **Soporte**

### **Contacto**
- **Issues:** Crear issue en GitHub
- **Documentación:** Revisar README específicos
- **Logs:** Verificar logs en `logs/` directory

### **Troubleshooting**
- **Verificar salud del servicio:** `GET /actuator/health`
- **Revisar logs:** `tail -f logs/monitoreo.log`
- **Verificar métricas:** `GET /actuator/metrics`
- **Probar correlation ID:** `GET /api/test/correlation/info`

## 📄 **Licencia**

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

**¡El microservicio de monitoreo está listo para proporcionar observabilidad completa a tu plataforma de subastas!** 🔍✨ 