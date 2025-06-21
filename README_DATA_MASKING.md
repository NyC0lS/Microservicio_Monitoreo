# Sistema de Enmascaramiento de Datos Sensibles

## 📋 Resumen

El microservicio de monitoreo implementa un sistema completo de enmascaramiento de datos sensibles para proteger la privacidad de los usuarios y cumplir con regulaciones de protección de datos (GDPR, CCPA, etc.).

## 🎯 Objetivos

- **Protección de Privacidad**: Enmascarar automáticamente datos personales y financieros
- **Cumplimiento Normativo**: Cumplir con regulaciones de protección de datos
- **Auditoría Completa**: Registrar todas las operaciones de enmascaramiento
- **Trazabilidad**: Mantener logs detallados de acceso a datos sensibles

## 🏗️ Arquitectura del Sistema

### Componentes Principales

1. **SensitiveDataFilter** - Filtro principal de enmascaramiento
2. **SensitiveDataHttpFilter** - Filtro HTTP para solicitudes/respuestas
3. **DataMaskingService** - Servicio de enmascaramiento de eventos de monitoreo
4. **AuditService** - Servicio de auditoría y trazabilidad

### Flujo de Enmascaramiento

```
Solicitud HTTP → SensitiveDataHttpFilter → Controlador → DataMaskingService → Respuesta Enmascarada
                                    ↓
                              AuditService (Logging)
```

## 🔒 Tipos de Datos Sensibles Protegidos

### 1. Información Personal
- **Emails**: `usuario@dominio.com` → `u***@d***.com`
- **Teléfonos**: `+1234567890` → `***-***-7890`
- **IDs de Usuario**: `user12345` → `us***45`

### 2. Información Financiera
- **Tarjetas de Crédito**: `1234-5678-9012-3456` → `****-****-****-3456`
- **Montos**: `$1,234.56` → `$1K - $9K`
- **SSN**: `123-45-6789` → `***-**-6789`

### 3. Información de Red
- **IPs**: `192.168.1.100` → `192.***.***.***`
- **URLs**: `https://api.example.com/user/123` → `https://***.example.com/user/***`

### 4. Headers HTTP Sensibles
- `Authorization`
- `X-API-Key`
- `Cookie`
- `Set-Cookie`

## 🛠️ Implementación

### 1. SensitiveDataFilter

```java
@Component
public class SensitiveDataFilter {
    
    // Patrones regex para detectar datos sensibles
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\+?[1-9]\\d{1,14}\\b");
    // ... más patrones
    
    public String maskSensitiveData(String text) {
        // Enmascarar automáticamente todos los tipos de datos sensibles
    }
    
    public String maskEmail(String email) {
        // Enmascarar específicamente emails
    }
    
    // ... más métodos específicos
}
```

### 2. Filtro HTTP

```java
@Component
@Order(1)
public class SensitiveDataHttpFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // Enmascarar headers y parámetros sensibles
        // Log de solicitudes y respuestas enmascaradas
    }
}
```

### 3. Servicio de Enmascaramiento

```java
@Service
public class DataMaskingService {
    
    public EventoMonitoreo maskEventoMonitoreo(EventoMonitoreo evento) {
        // Enmascarar datos sensibles en eventos de monitoreo
    }
    
    public String maskLogMessage(String message) {
        // Enmascarar mensajes de log
    }
}
```

### 4. Servicio de Auditoría

```java
@Service
public class AuditService {
    
    public void logSensitiveDataDetected(String dataType, String source, String context) {
        // Registrar detección de datos sensibles
    }
    
    public void logDataMasked(String dataType, String originalValue, String maskedValue, String source) {
        // Registrar enmascaramiento de datos
    }
}
```

## 📊 Configuración de Logging

### Logback Configuration

```xml
<!-- Appender para logs de seguridad -->
<appender name="SECURITY" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/security.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/security.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>90</maxHistory>
        <totalSizeCap>500MB</totalSizeCap>
    </rollingPolicy>
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
        <!-- Configuración JSON estructurada -->
    </encoder>
</appender>
```

### Loggers Específicos

```xml
<!-- Logger para datos sensibles -->
<logger name="com.monitoreo.config.SensitiveDataFilter" level="DEBUG" additivity="false">
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="SECURITY"/>
</logger>

<!-- Logger para auditoría -->
<logger name="com.monitoreo.audit" level="INFO" additivity="false">
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="SECURITY"/>
</logger>
```

## 🔍 Eventos de Auditoría

### Tipos de Eventos Registrados

1. **SENSITIVE_DATA_DETECTED**
   - Detecta cuando se encuentran datos sensibles
   - Incluye tipo de dato, fuente y contexto

2. **DATA_MASKED**
   - Registra cada operación de enmascaramiento
   - Incluye tipo, longitud original y enmascarada

3. **SENSITIVE_DATA_ACCESS**
   - Registra acceso autorizado a datos sensibles
   - Incluye usuario, operación y recurso

4. **UNAUTHORIZED_ACCESS_ATTEMPT**
   - Registra intentos de acceso no autorizado
   - Incluye razón del rechazo

5. **MASKING_CONFIGURATION**
   - Registra cambios en configuración de enmascaramiento

6. **COMPLIANCE_EVENT**
   - Registra eventos de cumplimiento normativo

### Ejemplo de Log de Auditoría

```json
{
  "event_type": "DATA_MASKED",
  "timestamp": "2024-01-01T12:00:00",
  "data_type": "EMAIL",
  "source": "HTTP_REQUEST",
  "original_length": 25,
  "masked_length": 12,
  "masking_applied": true,
  "application": "monitoreo-service",
  "version": "1.0.0",
  "environment": "development",
  "data_masked": true,
  "log_type": "security"
}
```

## 🚀 Uso en Controladores

### Ejemplo de Implementación

```java
@RestController
public class EventoMonitoreoController {
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping("/eventos/{id}")
    public ResponseEntity<EventoMonitoreo> getEvento(@PathVariable Long id) {
        EventoMonitoreo evento = eventoService.findById(id);
        
        // Enmascarar datos sensibles antes de la respuesta
        EventoMonitoreo maskedEvento = dataMaskingService.maskEventoMonitoreo(evento);
        
        // Registrar acceso a datos sensibles
        auditService.logSensitiveDataAccess(
            getCurrentUserId(), 
            "EVENTO_MONITOREO", 
            "READ", 
            "/api/eventos/" + id
        );
        
        return ResponseEntity.ok(maskedEvento);
    }
}
```

## 🔧 Configuración

### Propiedades de Configuración

```yaml
# application.yml
monitoreo:
  data-masking:
    enabled: true
    audit-enabled: true
    log-sensitive-detection: true
    
  security:
    log-retention-days: 90
    max-log-size: 500MB
    
  compliance:
    gdpr-enabled: true
    ccpa-enabled: true
    data-retention-days: 30
```

### Variables de Entorno

```bash
# Habilitar/deshabilitar enmascaramiento
DATA_MASKING_ENABLED=true

# Configurar retención de logs
SECURITY_LOG_RETENTION_DAYS=90

# Configurar tamaño máximo de logs
SECURITY_LOG_MAX_SIZE=500MB
```

## 📈 Monitoreo y Métricas

### Métricas Disponibles

- **Datos Sensibles Detectados**: Contador de detecciones
- **Datos Enmascarados**: Contador de enmascaramientos
- **Intentos de Acceso No Autorizado**: Contador de intentos fallidos
- **Tiempo de Procesamiento**: Latencia del enmascaramiento

### Endpoints de Monitoreo

```
GET /actuator/metrics/data.masking.detected
GET /actuator/metrics/data.masking.masked
GET /actuator/metrics/data.masking.unauthorized
GET /actuator/metrics/data.masking.processing.time
```

## 🔒 Cumplimiento Normativo

### GDPR (General Data Protection Regulation)

- ✅ **Derecho al Olvido**: Datos enmascarados automáticamente
- ✅ **Minimización de Datos**: Solo datos necesarios son visibles
- ✅ **Transparencia**: Logs completos de procesamiento de datos

### CCPA (California Consumer Privacy Act)

- ✅ **Derecho a Saber**: Información sobre datos recolectados
- ✅ **Derecho a Eliminar**: Datos enmascarados en logs
- ✅ **No Discriminación**: Acceso igualitario a servicios

### HIPAA (Health Insurance Portability and Accountability Act)

- ✅ **PHI Protection**: Datos de salud enmascarados
- ✅ **Audit Trails**: Trazabilidad completa de acceso
- ✅ **Access Controls**: Control de acceso a datos sensibles

## 🧪 Testing

### Tests Unitarios

```java
@Test
public void testEmailMasking() {
    String email = "usuario@ejemplo.com";
    String masked = sensitiveDataFilter.maskEmail(email);
    assertEquals("u***@e***.com", masked);
}

@Test
public void testPhoneMasking() {
    String phone = "+1234567890";
    String masked = sensitiveDataFilter.maskPhone(phone);
    assertEquals("***-***-7890", masked);
}
```

### Tests de Integración

```java
@Test
public void testHttpFilterMasking() {
    // Test de enmascaramiento en solicitudes HTTP
}

@Test
public void testAuditLogging() {
    // Test de logging de auditoría
}
```

## 🚨 Alertas y Notificaciones

### Alertas Automáticas

- **Datos Sensibles No Enmascarados**: Cuando se detectan datos sensibles sin enmascarar
- **Acceso No Autorizado**: Cuando se detectan intentos de acceso no autorizado
- **Errores de Enmascaramiento**: Cuando falla el proceso de enmascaramiento

### Configuración de Alertas

```yaml
monitoreo:
  alerts:
    sensitive-data-exposed:
      enabled: true
      threshold: 1
      notification: email
    
    unauthorized-access:
      enabled: true
      threshold: 5
      notification: slack
    
    masking-errors:
      enabled: true
      threshold: 1
      notification: pagerduty
```

## 📚 Referencias

- [GDPR Compliance Guidelines](https://gdpr.eu/)
- [CCPA Compliance Guide](https://oag.ca.gov/privacy/ccpa)
- [OWASP Data Protection](https://owasp.org/www-project-data-protection/)
- [NIST Privacy Framework](https://www.nist.gov/privacy-framework)

## 🤝 Contribución

Para contribuir al sistema de enmascaramiento:

1. Revisar los patrones de detección de datos sensibles
2. Agregar nuevos tipos de datos sensibles según sea necesario
3. Mejorar los algoritmos de enmascaramiento
4. Actualizar la documentación de cumplimiento normativo

---

**Nota**: Este sistema está diseñado para cumplir con las regulaciones de protección de datos más estrictas. Siempre revise las regulaciones específicas aplicables a su jurisdicción y caso de uso. 