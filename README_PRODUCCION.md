# 🚀 **Monitoreo en Producción y Staging**

## 📋 **Descripción General**

Este documento describe la configuración y despliegue del sistema de monitoreo en entornos de **producción** y **staging**, asegurando que funcione de manera eficiente sin afectar el rendimiento ni exponer información sensible.

## 🏗️ **Arquitectura de Entornos**

```
┌─────────────────┐    ┌─────────────────┐
│   PRODUCCIÓN    │    │     STAGING     │
│                 │    │                 │
│ • Puerto 8080   │    │ • Puerto 8081   │
│ • Prometheus    │    │ • Prometheus    │
│   9090          │    │   9091          │
│ • Grafana 3000  │    │ • Grafana 3001  │
│ • DB: 5432      │    │ • DB: 5433      │
└─────────────────┘    └─────────────────┘
```

## 🔧 **Configuraciones por Entorno**

### **Producción (`application-production.yml`)**
- **Seguridad:** Máxima protección de información sensible
- **Logging:** Solo WARN y ERROR
- **Métricas:** Detalles limitados en health checks
- **Rendimiento:** Optimizado para alta carga
- **Retención:** 200h en Prometheus, 60 días en logs

### **Staging (`application-staging.yml`)**
- **Seguridad:** Moderada, con autenticación básica
- **Logging:** INFO, WARN, ERROR
- **Métricas:** Detalles completos para debugging
- **Rendimiento:** Balanceado entre funcionalidad y rendimiento
- **Retención:** 100h en Prometheus, 30 días en logs

## 🚀 **Despliegue**

### **1. Entorno de Producción**
```bash
# Usar variables de entorno para configuración
export DB_HOST=prod-db-host
export DB_PASSWORD=prod-secure-password
export PROD_USER=admin
export PROD_PASSWORD=secure-password

# Levantar stack de producción
docker-compose up -d
```

### **2. Entorno de Staging**
```bash
# Configurar variables de staging
export STAGING_USER=admin
export STAGING_PASSWORD=staging123

# Levantar stack de staging
docker-compose -f docker-compose.staging.yml up -d
```

### **3. Ambos Entornos Simultáneamente**
```bash
# Levantar ambos entornos
docker-compose up -d
docker-compose -f docker-compose.staging.yml up -d
```

## 📊 **Validación de Métricas y Logs**

### **Script de Validación Automática**
```bash
# Dar permisos de ejecución
chmod +x scripts/validate-metrics.sh

# Validar ambos entornos
./scripts/validate-metrics.sh both

# Validar solo producción
./scripts/validate-metrics.sh production

# Validar solo staging
./scripts/validate-metrics.sh staging
```

### **Validación Manual**

#### **Producción**
```bash
# Health check
curl -u admin:secure-password http://localhost:8080/actuator/health

# Métricas
curl -u admin:secure-password http://localhost:8080/actuator/metrics

# Prometheus
curl http://localhost:9090/api/v1/status/config

# Grafana
curl http://localhost:3000/api/health
```

#### **Staging**
```bash
# Health check
curl -u admin:staging123 http://localhost:8081/actuator/health

# Métricas
curl -u admin:staging123 http://localhost:8081/actuator/metrics

# Prometheus
curl http://localhost:9091/api/v1/status/config

# Grafana
curl http://localhost:3001/api/health
```

## 🔍 **Monitoreo de Recursos**

### **Consumo de Recursos por Agente**

| Componente | CPU (Promedio) | Memoria (Promedio) | Disco |
|------------|----------------|-------------------|-------|
| Microservicio | 2-5% | 200-500MB | 100MB |
| Prometheus | 5-10% | 1-2GB | 5-10GB |
| Grafana | 1-3% | 200-400MB | 500MB |
| Loki | 3-8% | 500MB-1GB | 2-5GB |
| Alertmanager | 1-2% | 100-200MB | 100MB |

### **Optimizaciones de Rendimiento**

#### **Producción**
- **Prometheus:** Retención de 200h, scrape cada 15s
- **Grafana:** Caché habilitado, compresión activada
- **Loki:** Compresión de logs, retención de 60 días
- **Microservicio:** Pool de conexiones optimizado, caché habilitado

#### **Staging**
- **Prometheus:** Retención de 100h, scrape cada 30s
- **Grafana:** Configuración estándar
- **Loki:** Retención de 30 días
- **Microservicio:** Configuración balanceada

## 🔐 **Controles de Acceso**

### **Grafana - Roles y Permisos**

| Rol | Permisos | Usuarios |
|-----|----------|----------|
| **Admin** | Acceso completo | admin |
| **Editor** | Crear/editar dashboards | developer |
| **Viewer** | Solo visualización | viewer |

### **Autenticación por Entorno**

#### **Producción**
- **Grafana:** Usuario/contraseña + 2FA recomendado
- **Microservicio:** Autenticación básica obligatoria
- **Prometheus:** Sin autenticación (interno)
- **Alertmanager:** Sin autenticación (interno)

#### **Staging**
- **Grafana:** Usuario/contraseña
- **Microservicio:** Autenticación básica
- **Prometheus:** Sin autenticación
- **Alertmanager:** Sin autenticación

## 🚨 **Alertas por Entorno**

### **Producción**
- **Servicio caído:** 1 minuto
- **Errores 5xx:** >5/min durante 2 minutos
- **Latencia:** >300ms (95 percentil) durante 2 minutos

### **Staging**
- **Servicio caído:** 2 minutos
- **Errores 5xx:** >10/min durante 3 minutos
- **Latencia:** >500ms (95 percentil) durante 3 minutos

## 📈 **Dashboards por Entorno**

### **Producción**
- Dashboard principal con métricas críticas
- Alertas de negocio
- Métricas de rendimiento
- Estado de servicios

### **Staging**
- Dashboard completo con métricas detalladas
- Métricas de desarrollo
- Logs de debugging
- Estado de servicios

## 🔧 **Mantenimiento**

### **Backup de Configuración**
```bash
# Backup de configuraciones
tar -czf backup-config-$(date +%Y%m%d).tar.gz config/

# Backup de datos
docker-compose exec postgres pg_dump -U postgres monitoreo_db > backup-db-$(date +%Y%m%d).sql
```

### **Actualización de Versiones**
```bash
# Actualizar imágenes
docker-compose pull
docker-compose up -d

# Verificar funcionamiento
./scripts/validate-metrics.sh both
```

### **Limpieza de Datos**
```bash
# Limpiar logs antiguos
find logs/ -name "*.log" -mtime +30 -delete

# Limpiar volúmenes de Docker
docker volume prune -f
```

## 🛠️ **Troubleshooting**

### **Problemas Comunes**

#### **1. Microservicio no responde**
```bash
# Verificar logs
docker-compose logs -f monitoreo

# Verificar base de datos
docker-compose exec postgres psql -U postgres -d monitoreo_db -c "SELECT 1;"
```

#### **2. Prometheus no recolecta métricas**
```bash
# Verificar configuración
curl http://localhost:9090/api/v1/status/config

# Verificar targets
curl http://localhost:9090/api/v1/targets
```

#### **3. Grafana no muestra datos**
```bash
# Verificar fuentes de datos
curl -u admin:admin http://localhost:3000/api/datasources

# Verificar dashboards
curl -u admin:admin http://localhost:3000/api/dashboards
```

### **Logs de Diagnóstico**
```bash
# Logs de todos los servicios
docker-compose logs

# Logs específicos
docker-compose logs prometheus
docker-compose logs grafana
docker-compose logs monitoreo
```

## 📋 **Checklist de Despliegue**

### **Pre-despliegue**
- [ ] Variables de entorno configuradas
- [ ] Base de datos inicializada
- [ ] Puertos disponibles
- [ ] Recursos del sistema verificados

### **Despliegue**
- [ ] Stack levantado correctamente
- [ ] Health checks pasando
- [ ] Métricas recolectándose
- [ ] Dashboards funcionando

### **Post-despliegue**
- [ ] Alertas configuradas
- [ ] Usuarios creados
- [ ] Accesos verificados
- [ ] Documentación actualizada

## 🎯 **Próximos Pasos**

1. **Implementar 2FA** en Grafana para producción
2. **Configurar backup automático** de configuraciones
3. **Implementar auto-scaling** basado en métricas
4. **Configurar integración** con sistemas de tickets
5. **Implementar métricas de negocio** específicas

---

**¡El sistema de monitoreo está listo para producción y staging!** 🚀 