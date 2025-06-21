# 📊 Stack de Observabilidad - PLG (Promtail, Loki, Grafana)

Este documento describe la configuración y uso del stack de observabilidad implementado para el microservicio de monitoreo.

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Microservicio │    │    Promtail     │    │      Loki       │
│   de Monitoreo  │───▶│   (Colector)    │───▶│  (Almacenamiento)│
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │     Grafana     │
                                              │ (Visualización) │
                                              └─────────────────┘
```

## 🚀 Inicio Rápido

### 1. Levantar el Stack Completo
```bash
# Desde la raíz del proyecto Microservicio-Monitoreo
docker-compose up -d
```

### 2. Verificar Servicios
```bash
# Verificar que todos los servicios estén corriendo
docker-compose ps
```

### 3. Acceder a Grafana
- **URL**: http://localhost:3000
- **Usuario**: `admin`
- **Contraseña**: `admin123`

## 📁 Estructura de Archivos

```
Microservicio-Monitoreo/
├── docker-compose.yml                    # Stack completo
├── config/
│   ├── loki-config.yaml                  # Configuración de Loki
│   ├── promtail-config.yaml              # Configuración de Promtail
│   └── grafana/
│       ├── provisioning/
│       │   ├── datasources/
│       │   │   └── loki-datasource.yaml  # Fuente de datos Loki
│       │   └── dashboards/
│       │       └── dashboards.yaml       # Provisioning de dashboards
│       └── dashboards/
│           └── monitoreo-dashboard.json  # Dashboard principal
└── logs/                                 # Logs del microservicio
```

## 🔧 Configuración Detallada

### Loki (Almacenamiento de Logs)
- **Puerto**: 3100
- **Configuración**: `config/loki-config.yaml`
- **Almacenamiento**: Sistema de archivos local
- **Retención**: 7 días por defecto

### Promtail (Colector de Logs)
- **Puerto**: 9080
- **Configuración**: `config/promtail-config.yaml`
- **Monitorea**: 
  - `/var/log/monitoreo/*.log` (logs del microservicio)
  - `/var/log/*.log` (logs del sistema)

### Grafana (Visualización)
- **Puerto**: 3000
- **Dashboard**: Cargado automáticamente
- **Fuente de datos**: Loki configurado automáticamente

## 📊 Dashboard de Monitoreo

El dashboard incluye los siguientes paneles:

### 1. Gráfico de Torta - Logs por Nivel
- Muestra la distribución de logs por nivel (INFO, WARN, ERROR, DEBUG)
- Actualización cada 5 minutos

### 2. Gráfico de Líneas - Tasa de Logs
- Muestra la tasa de logs por segundo
- Útil para detectar picos de actividad

### 3. Panel de Logs - Logs Recientes
- Muestra los logs más recientes en tiempo real
- Ordenados por timestamp descendente

### 4. Métricas Rápidas
- **Errores (5m)**: Contador de errores en los últimos 5 minutos
- **Warnings (5m)**: Contador de warnings en los últimos 5 minutos
- **Info (5m)**: Contador de logs de información
- **Debug (5m)**: Contador de logs de debug

## 🔍 Consultas Útiles en Grafana

### Consultas Básicas
```logql
# Todos los logs del microservicio
{job="monitoreo"}

# Solo errores
{job="monitoreo", level="ERROR"}

# Logs de un servicio específico
{job="monitoreo"} |= "serviceName"

# Logs con datos sensibles detectados
{job="monitoreo"} |= "sensitive_data_detected"
```

### Consultas Avanzadas
```logql
# Contar logs por nivel en los últimos 5 minutos
sum by (level) (count_over_time({job="monitoreo"} [5m]))

# Tasa de logs por segundo
sum(rate({job="monitoreo"} [5m]))

# Logs con errores de validación
{job="monitoreo"} |= "VALIDATION_ERROR"

# Logs de eventos de monitoreo creados
{job="monitoreo"} |= "EVENTO_CREADO"
```

## 🛠️ Comandos Útiles

### Verificar Estado de los Servicios
```bash
# Ver logs de todos los servicios
docker-compose logs

# Ver logs de un servicio específico
docker-compose logs loki
docker-compose logs promtail
docker-compose logs grafana
docker-compose logs monitoreo

# Verificar estado de los contenedores
docker-compose ps
```

### Reiniciar Servicios
```bash
# Reiniciar todo el stack
docker-compose restart

# Reiniciar un servicio específico
docker-compose restart grafana
```

### Limpiar y Reconstruir
```bash
# Detener y eliminar contenedores
docker-compose down

# Eliminar volúmenes (cuidado: elimina datos)
docker-compose down -v

# Reconstruir imágenes
docker-compose build --no-cache
```

## 🔒 Seguridad

### Credenciales por Defecto
- **Grafana**: admin/admin123
- **Loki**: Sin autenticación (solo desarrollo)
- **Promtail**: Sin autenticación

### Recomendaciones para Producción
1. Cambiar contraseñas por defecto
2. Habilitar autenticación en Loki
3. Configurar HTTPS
4. Implementar control de acceso por IP
5. Usar secretos de Docker para credenciales

## 📈 Monitoreo y Alertas

### Métricas a Monitorear
- **Tasa de logs por segundo**: Detectar picos anormales
- **Porcentaje de errores**: Mantener bajo (< 5%)
- **Latencia de respuesta**: < 100ms
- **Uso de recursos**: CPU, memoria, disco

### Alertas Recomendadas
```yaml
# Ejemplo de alerta para muchos errores
- alert: HighErrorRate
  expr: sum(rate({job="monitoreo", level="ERROR"}[5m])) > 10
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "Alta tasa de errores en microservicio de monitoreo"
```

## 🐛 Troubleshooting

### Problemas Comunes

#### 1. Grafana no puede conectarse a Loki
```bash
# Verificar que Loki esté corriendo
docker-compose ps loki

# Verificar logs de Loki
docker-compose logs loki

# Verificar conectividad
curl http://localhost:3100/ready
```

#### 2. Promtail no recolecta logs
```bash
# Verificar configuración de Promtail
docker-compose logs promtail

# Verificar que los archivos de log existan
ls -la logs/

# Verificar permisos de archivos
docker-compose exec promtail ls -la /var/log/monitoreo/
```

#### 3. Dashboard no muestra datos
```bash
# Verificar que Loki tenga datos
curl "http://localhost:3100/loki/api/v1/labels"

# Verificar consultas en Grafana
# Ir a Explore y probar: {job="monitoreo"}
```

### Logs de Debug
```bash
# Habilitar logs detallados de Promtail
docker-compose exec promtail promtail -config.file=/etc/promtail/config.yml -log.level=debug

# Verificar métricas de Promtail
curl http://localhost:9080/metrics
```

## 📚 Recursos Adicionales

### Documentación Oficial
- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Promtail Documentation](https://grafana.com/docs/loki/latest/clients/promtail/)
- [Grafana Documentation](https://grafana.com/docs/)

### LogQL (Loki Query Language)
- [LogQL Reference](https://grafana.com/docs/loki/latest/logql/)
- [Query Examples](https://grafana.com/docs/loki/latest/logql/log_queries/)

### Dashboards y Alertas
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)
- [Loki Alerting](https://grafana.com/docs/loki/latest/alerting/)

## 🔄 Actualizaciones

### Actualizar Versiones
```bash
# Editar docker-compose.yml y cambiar versiones
# Luego reconstruir
docker-compose down
docker-compose pull
docker-compose up -d
```

### Backup de Configuración
```bash
# Crear backup de configuración
tar -czf observability-config-$(date +%Y%m%d).tar.gz config/

# Restaurar configuración
tar -xzf observability-config-YYYYMMDD.tar.gz
```

---

## ✅ Checklist de Verificación

- [ ] Docker y Docker Compose instalados
- [ ] Stack levantado con `docker-compose up -d`
- [ ] Grafana accesible en http://localhost:3000
- [ ] Loki configurado como fuente de datos
- [ ] Dashboard cargado automáticamente
- [ ] Logs del microservicio visibles en Grafana
- [ ] Consultas LogQL funcionando
- [ ] Métricas actualizándose en tiempo real

¡Tu stack de observabilidad está listo! 🎉 