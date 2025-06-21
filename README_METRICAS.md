# 📊 **Instrumentación de Métricas - Microservicio de Monitoreo**

## 🎯 **Descripción General**

Este documento describe la implementación completa de métricas en el microservicio de monitoreo utilizando **Spring Boot Actuator** y **Micrometer** con **Prometheus** para la recolección y **Grafana** para la visualización.

## 🏗️ **Arquitectura de Métricas**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Microservicio │    │    Prometheus   │    │     Grafana     │
│   de Monitoreo  │───▶│   (Recolección) │───▶│ (Visualización) │
│                 │    │                 │    │                 │
│ • Actuator      │    │ • Scraping      │    │ • Dashboards    │
│ • Micrometer    │    │ • Storage       │    │ • Alertas       │
│ • Métricas      │    │ • Queries       │    │ • Reports       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📦 **Dependencias Implementadas**

### **Spring Boot Actuator**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### **Micrometer Prometheus Registry**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>
```

## ⚙️ **Configuración**

### **application.yml**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,configprops,beans,mappings,loggers,auditevents,httptrace,scheduledtasks,threaddump,heapdump
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: development
```

## 🔧 **Servicio de Métricas Personalizadas**

### **Métricas Implementadas**

#### **1. Contadores (Counters)**
- `monitoreo.eventos.creados` - Eventos de monitoreo creados
- `monitoreo.eventos.eliminados` - Eventos de monitoreo eliminados
- `monitoreo.eventos.actualizados` - Eventos de monitoreo actualizados
- `monitoreo.eventos.consultados` - Eventos de monitoreo consultados
- `monitoreo.errores.validacion` - Errores de validación
- `monitoreo.errores.basedatos` - Errores de base de datos
- `monitoreo.errores.sistema` - Errores del sistema
- `monitoreo.datos.sensibles.detectados` - Datos sensibles detectados
- `monitoreo.datos.sensibles.enmascarados` - Datos sensibles enmascarados
- `monitoreo.logs.nivel` - Logs por nivel (INFO, WARN, ERROR, DEBUG)

#### **2. Timers (Medición de Latencia)**
- `monitoreo.eventos.creacion.tiempo` - Tiempo de creación de eventos
- `monitoreo.eventos.consulta.tiempo` - Tiempo de consulta de eventos
- `monitoreo.logs.procesamiento.tiempo` - Tiempo de procesamiento de logs

#### **3. Gauges (Métricas de Estado)**
- `monitoreo.eventos.activos` - Eventos activos en el sistema
- `monitoreo.sesiones.activas` - Sesiones activas
- `monitoreo.eventos.errores.acumulados` - Errores acumulados

## 🚀 **Endpoints de Métricas**

### **Actuator Endpoints**
- `GET /actuator/metrics` - Lista todas las métricas disponibles
- `GET /actuator/metrics/{metric.name}` - Obtiene una métrica específica
- `GET /actuator/prometheus` - Formato Prometheus para scraping
- `GET /actuator/health` - Estado de salud de la aplicación

### **Endpoints Personalizados**
- `GET /api/metrics/custom` - Métricas personalizadas del sistema
- `GET /api/metrics/summary` - Estadísticas resumidas
- `POST /api/metrics/simulate` - Simular carga para generar métricas
- `GET /api/metrics/info` - Información de métricas disponibles

## 📊 **Integración con Prometheus**

### **Configuración de Prometheus**
```yaml
scrape_configs:
  - job_name: 'monitoreo'
    static_configs:
      - targets: ['monitoreo:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    scrape_timeout: 5s
```

### **Métricas Recolectadas**
Prometheus recolecta automáticamente todas las métricas expuestas en `/actuator/prometheus`:

```prometheus
# Eventos de monitoreo
monitoreo_eventos_creados_total{tipo="creacion"} 42
monitoreo_eventos_consultados_total{tipo="consulta"} 156
monitoreo_eventos_eliminados_total{tipo="eliminacion"} 8

# Errores
monitoreo_errores_validacion_total{tipo="validacion"} 3
monitoreo_errores_basedatos_total{tipo="basedatos"} 1
monitoreo_errores_sistema_total{tipo="sistema"} 2

# Latencia
monitoreo_eventos_creacion_tiempo_seconds_sum{operacion="creacion"} 1.234
monitoreo_eventos_creacion_tiempo_seconds_count{operacion="creacion"} 42

# Estado
monitoreo_eventos_activos 34
monitoreo_sesiones_activas 5
```

## 📈 **Dashboards de Grafana**

### **Dashboard Principal: Métricas del Microservicio**
- **Eventos de Monitoreo**: Contadores de eventos creados, consultados, eliminados
- **Errores del Sistema**: Errores por tipo (validación, BD, sistema)
- **Latencia de Operaciones**: Tiempo promedio de operaciones
- **Logs por Nivel**: Distribución de logs por nivel (INFO, WARN, ERROR, DEBUG)
- **Datos Sensibles**: Contadores de detección y enmascaramiento
- **Estado del Sistema**: Métricas de estado en tiempo real

### **Consultas PromQL Útiles**

#### **Tasa de Eventos por Minuto**
```promql
rate(monitoreo_eventos_creados_total[1m])
```

#### **Latencia Promedio**
```promql
rate(monitoreo_eventos_creacion_tiempo_seconds_sum[5m]) / rate(monitoreo_eventos_creacion_tiempo_seconds_count[5m])
```

#### **Porcentaje de Errores**
```promql
(rate(monitoreo_errores_sistema_total[5m]) / rate(monitoreo_eventos_creados_total[5m])) * 100
```

#### **Uso de Memoria**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

## 🧪 **Pruebas de Métricas**

### **1. Verificar Endpoints de Actuator**
```bash
# Listar métricas disponibles
curl http://localhost:8080/actuator/metrics

# Obtener métricas específicas
curl http://localhost:8080/actuator/metrics/monitoreo.eventos.creados

# Formato Prometheus
curl http://localhost:8080/actuator/prometheus
```

### **2. Simular Carga**
```bash
# Generar métricas de prueba
curl -X POST http://localhost:8080/api/metrics/simulate

# Obtener métricas personalizadas
curl http://localhost:8080/api/metrics/custom
```

### **3. Verificar Prometheus**
```bash
# Acceder a Prometheus
http://localhost:9090

# Consultar métricas
monitoreo_eventos_creados_total
monitoreo_errores_sistema_total
```

### **4. Verificar Grafana**
```bash
# Acceder a Grafana
http://localhost:3000
# Usuario: admin
# Contraseña: admin
```

## 🔍 **Monitoreo y Alertas**

### **Métricas Críticas a Monitorear**

#### **1. Rendimiento**
- Latencia de operaciones > 1 segundo
- Tasa de errores > 5%
- Uso de memoria > 80%

#### **2. Negocio**
- Eventos no procesados
- Datos sensibles no enmascarados
- Sesiones huérfanas

#### **3. Infraestructura**
- CPU > 80%
- Memoria > 90%
- Espacio en disco > 85%

### **Alertas Recomendadas**
```yaml
# Ejemplo de reglas de alerta para Prometheus
groups:
  - name: monitoreo_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(monitoreo_errores_sistema_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Alta tasa de errores en monitoreo"
          
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(monitoreo_eventos_creacion_tiempo_seconds_bucket[5m])) > 1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Latencia alta en creación de eventos"
```

## 🛠️ **Comandos Útiles**

### **Docker Compose**
```bash
# Levantar stack completo
docker-compose up -d

# Ver logs
docker-compose logs -f monitoreo

# Detener servicios
docker-compose down

# Reconstruir imagen
docker-compose build monitoreo
```

### **Prometheus**
```bash
# Verificar configuración
curl http://localhost:9090/api/v1/status/config

# Consultar métricas
curl "http://localhost:9090/api/v1/query?query=monitoreo_eventos_creados_total"
```

### **Grafana**
```bash
# Crear dashboard programáticamente
curl -X POST http://admin:admin@localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @config/grafana/dashboards/metrics-dashboard.json
```

## 📋 **Checklist de Implementación**

- [x] Dependencias de Actuator y Micrometer agregadas
- [x] Configuración de application.yml completada
- [x] Servicio de métricas personalizadas implementado
- [x] Integración en controladores y servicios
- [x] Endpoints de métricas expuestos
- [x] Prometheus configurado y funcionando
- [x] Grafana configurado con dashboards
- [x] Métricas de sistema (CPU, memoria) implementadas
- [x] Métricas de negocio implementadas
- [x] Timers para latencia implementados
- [x] Contadores para eventos implementados
- [x] Gauges para estado implementados
- [x] Documentación completa

## 🎯 **Próximos Pasos**

1. **Implementar Alertas**: Configurar reglas de alerta en Prometheus
2. **Métricas de Negocio**: Agregar métricas específicas del dominio
3. **Distributed Tracing**: Integrar con Jaeger o Zipkin
4. **APM**: Implementar Application Performance Monitoring
5. **Auto-scaling**: Basado en métricas de carga
6. **SLA Monitoring**: Monitoreo de acuerdos de nivel de servicio

## 📚 **Recursos Adicionales**

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [PromQL Query Language](https://prometheus.io/docs/prometheus/latest/querying/basics/)

---

**¡El microservicio de monitoreo está completamente instrumentado con métricas y listo para producción!** 🚀 