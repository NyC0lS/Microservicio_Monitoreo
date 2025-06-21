# 🔗 **Correlation ID - Trazabilidad entre Microservicios**

## 📋 **Descripción General**

Este documento describe la implementación completa del sistema de **Correlation ID** en el microservicio de monitoreo, que permite seguir las peticiones a través de toda la cadena de servicios y correlacionar logs entre diferentes componentes.

## 🏗️ **Arquitectura de Correlation ID**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Cliente       │    │   Microservicio │    │   Base de       │
│                 │    │   de Monitoreo  │    │   Datos         │
│                 │    │                 │    │                 │
│ X-Correlation-ID│───▶│ Filtro HTTP     │───▶│ Logs con        │
│ X-Request-ID    │    │ MDC Context     │    │ Correlation ID  │
│ X-User-ID       │    │ Propagación     │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Otros         │
                       │   Microservicios│
                       │                 │
                       │ Headers         │
                       │ propagados      │
                       └─────────────────┘
```

## 🔧 **Componentes Implementados**

### **1. CorrelationIdFilter**
- **Ubicación:** `com.monitoreo.config.CorrelationIdFilter`
- **Función:** Filtro HTTP que extrae o genera correlation-id
- **Headers manejados:**
  - `X-Correlation-ID`: Identificador principal de correlación
  - `X-Request-ID`: Identificador único de la petición
  - `X-User-ID`: Identificador del usuario
  - `X-Session-ID`: Identificador de sesión

### **2. CorrelationIdInterceptor**
- **Ubicación:** `com.monitoreo.config.CorrelationIdInterceptor`
- **Función:** Interceptor para propagar correlation-id en llamadas salientes
- **Integración:** RestTemplate configurado automáticamente

### **3. RestTemplateConfig**
- **Ubicación:** `com.monitoreo.config.RestTemplateConfig`
- **Función:** Configuración de RestTemplate con interceptores
- **Características:** Timeouts configurados y buffering habilitado

## 📊 **Headers de Trazabilidad**

### **Headers de Entrada**
| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-Correlation-ID` | ID principal de correlación | `corr-a1b2c3d4e5f6g7h8` |
| `X-Request-ID` | ID único de la petición | `req-123456789abc` |
| `X-User-ID` | ID del usuario | `user-123` |
| `X-Session-ID` | ID de sesión | `session-abc123` |

### **Headers de Salida**
| Header | Descripción | Origen |
|--------|-------------|--------|
| `X-Correlation-ID` | Mismo ID propagado | Header de entrada o generado |
| `X-Request-ID` | Mismo ID propagado | Header de entrada o generado |
| `X-User-ID` | ID del usuario | Header de entrada o sesión |
| `X-Session-ID` | ID de sesión | Sesión HTTP |
| `X-Request-Timestamp` | Timestamp de la petición | Generado automáticamente |

## 🔍 **Contexto MDC (Mapped Diagnostic Context)**

### **Claves MDC**
```java
// Identificadores principales
"correlationId"  // ID de correlación
"requestId"      // ID de petición
"userId"         // ID de usuario
"sessionId"      // ID de sesión

// Información de la petición
"method"         // Método HTTP (GET, POST, etc.)
"uri"            // URI de la petición
"remoteAddr"     // IP del cliente
"userAgent"      // User-Agent del cliente
```

### **Acceso a MDC**
```java
// Obtener correlation-id actual
String correlationId = CorrelationIdFilter.getCurrentCorrelationId();

// Obtener request-id actual
String requestId = CorrelationIdFilter.getCurrentRequestId();

// Obtener user-id actual
String userId = CorrelationIdFilter.getCurrentUserId();

// Obtener session-id actual
String sessionId = CorrelationIdFilter.getCurrentSessionId();
```

## 📝 **Configuración de Logging**

### **Formato JSON con Correlation ID**
```json
{
    "timestamp": "2024-01-15T10:30:45.123Z",
    "level": "INFO",
    "logger": "com.monitoreo.controller.EventoMonitoreoController",
    "thread": "http-nio-8080-exec-1",
    "message": "Evento de monitoreo creado exitosamente",
    "correlationId": "corr-a1b2c3d4e5f6g7h8",
    "requestId": "req-123456789abc",
    "userId": "user-123",
    "sessionId": "session-abc123",
    "method": "POST",
    "uri": "/api/eventos",
    "remoteAddr": "192.168.1.100",
    "userAgent": "Mozilla/5.0..."
}
```

### **Appenders Configurados**
- **CONSOLE:** Salida a consola con formato JSON
- **FILE:** Archivo de logs general con rotación
- **SECURITY_FILE:** Logs de seguridad separados
- **AUDIT_FILE:** Logs de auditoría con retención extendida

## 🧪 **Endpoints de Prueba**

### **1. Probar Correlation ID en Logs**
```bash
GET /api/test/correlation/logs
```
Genera logs con diferentes niveles incluyendo correlation-id.

### **2. Probar Propagación**
```bash
POST /api/test/correlation/propagate?targetUrl=http://localhost:8080
```
Prueba la propagación de correlation-id a otro servicio.

### **3. Obtener Información de Correlation**
```bash
GET /api/test/correlation/info
```
Devuelve información completa del correlation-id actual.

### **4. Probar Correlation Personalizado**
```bash
POST /api/test/correlation/custom?correlationId=test-123&requestId=req-456
```
Prueba con correlation-id personalizado en headers.

### **5. Probar Diferentes Niveles de Log**
```bash
GET /api/test/correlation/levels
```
Genera logs de todos los niveles con correlation-id.

## 🚀 **Uso en Código**

### **En Controladores**
```java
@PostMapping
public ResponseEntity<EventoMonitoreo> crearEvento(@Valid @RequestBody EventoMonitoreoRequest request) {
    String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
    String requestId = CorrelationIdFilter.getCurrentRequestId();
    
    logger.info("Creando evento - CorrelationId: {}, RequestId: {}, EventType: {}", 
               correlationId, requestId, request.getEventType());
    
    // ... lógica del controlador
}
```

### **En Servicios**
```java
@Service
public class MonitoreoService {
    
    public void logEvent(String eventType, String message, Map<String, Object> metadata) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        
        // Agregar correlation-id al metadata
        metadata.put("correlationId", correlationId);
        
        logger.info("Evento registrado - CorrelationId: {}, Type: {}, Message: {}", 
                   correlationId, eventType, message);
    }
}
```

### **En Llamadas HTTP Salientes**
```java
@Autowired
private RestTemplate restTemplate;

public void callOtherService() {
    // El correlation-id se propaga automáticamente
    ResponseEntity<String> response = restTemplate.getForEntity(
        "http://other-service/api/data", String.class);
}
```

## 🔍 **Verificación de Trazabilidad**

### **1. Verificar Headers en Petición**
```bash
curl -H "X-Correlation-ID: test-123" \
     -H "X-Request-ID: req-456" \
     -H "X-User-ID: user-789" \
     http://localhost:8080/api/test/correlation/info
```

### **2. Verificar Propagación en Logs**
```bash
# Buscar logs por correlation-id
grep "corr-a1b2c3d4e5f6g7h8" logs/monitoreo.log

# Buscar logs por request-id
grep "req-123456789abc" logs/monitoreo.log
```

### **3. Verificar Headers de Respuesta**
```bash
curl -v -H "X-Correlation-ID: test-123" \
     http://localhost:8080/api/test/correlation/logs
```

## 📊 **Monitoreo y Métricas**

### **Métricas de Correlation ID**
- **Tasa de propagación:** Porcentaje de peticiones con correlation-id propagado
- **Tiempo de procesamiento:** Latencia por correlation-id
- **Errores por correlation:** Errores agrupados por correlation-id

### **Dashboards de Trazabilidad**
- **Flujo de peticiones:** Visualización del flujo entre servicios
- **Tiempo de respuesta:** Latencia por correlation-id
- **Errores correlacionados:** Errores agrupados por correlation-id

## 🔧 **Configuración por Entorno**

### **Desarrollo**
- **Logging:** DEBUG para correlation-id
- **Propagación:** Automática en todas las peticiones
- **Retención:** 7 días

### **Staging**
- **Logging:** INFO para correlation-id
- **Propagación:** Automática en todas las peticiones
- **Retención:** 30 días

### **Producción**
- **Logging:** WARN para correlation-id
- **Propagación:** Automática en todas las peticiones
- **Retención:** 90 días

## 🛠️ **Troubleshooting**

### **Problemas Comunes**

#### **1. Correlation ID no se propaga**
```bash
# Verificar configuración de RestTemplate
curl -H "X-Correlation-ID: test-123" \
     http://localhost:8080/api/test/correlation/propagate
```

#### **2. Logs sin correlation-id**
```bash
# Verificar configuración de MDC
curl http://localhost:8080/api/test/correlation/logs
```

#### **3. Headers no se incluyen en respuesta**
```bash
# Verificar filtro HTTP
curl -v http://localhost:8080/api/test/correlation/info
```

### **Logs de Diagnóstico**
```bash
# Logs del filtro de correlation
grep "CorrelationIdFilter" logs/monitoreo.log

# Logs del interceptor
grep "CorrelationIdInterceptor" logs/monitoreo.log
```

## 📋 **Checklist de Implementación**

- [x] Filtro HTTP para extraer/generar correlation-id
- [x] Interceptor para propagar correlation-id
- [x] Configuración de RestTemplate
- [x] Configuración de logging con MDC
- [x] Endpoints de prueba
- [x] Documentación completa
- [x] Verificación de propagación
- [x] Monitoreo y métricas

## 🎯 **Próximos Pasos**

1. **Integración con otros microservicios:** Asegurar que todos los servicios implementen correlation-id
2. **Distributed Tracing:** Integrar con Jaeger o Zipkin para trazabilidad completa
3. **Alertas por correlation:** Alertas cuando un correlation-id tenga muchos errores
4. **Análisis de flujo:** Dashboard para analizar el flujo de peticiones entre servicios
5. **Performance tracking:** Métricas de rendimiento por correlation-id

---

**¡El sistema de Correlation ID está completamente implementado y listo para producción!** 🔗 