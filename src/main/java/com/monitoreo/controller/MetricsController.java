package com.monitoreo.controller;

import com.monitoreo.service.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para exponer métricas personalizadas y estadísticas del sistema
 */
@RestController
@RequestMapping("/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * GET - Obtener métricas personalizadas del sistema
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Métricas del sistema
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // CPU y memoria
        double cpuUsage = osBean.getSystemLoadAverage();
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        // Registrar métricas de rendimiento
        metricsService.registrarMetricaRendimiento("cpu_usage", cpuUsage, "percentage");
        metricsService.registrarMetricaRendimiento("memory_usage", memoryUsagePercent, "percentage");
        metricsService.registrarMetricaRendimiento("total_memory", totalMemory, "bytes");
        metricsService.registrarMetricaRendimiento("used_memory", usedMemory, "bytes");
        
        // Agregar métricas al response
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("system", Map.of(
            "cpu_usage", cpuUsage,
            "memory_usage_percent", memoryUsagePercent,
            "total_memory_mb", totalMemory / (1024 * 1024),
            "used_memory_mb", usedMemory / (1024 * 1024),
            "available_processors", osBean.getAvailableProcessors(),
            "system_uptime", System.currentTimeMillis()
        ));
        
        // Métricas de negocio
        metrics.put("business", Map.of(
            "application_name", "monitoreo",
            "version", "1.0.0",
            "environment", "development"
        ));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET - Obtener estadísticas resumidas
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Registrar estadísticas
        metricsService.registrarEstadisticasResumidas();
        
        summary.put("timestamp", LocalDateTime.now());
        summary.put("message", "Estadísticas registradas correctamente");
        summary.put("endpoints", Map.of(
            "metrics", "/actuator/metrics",
            "prometheus", "/actuator/prometheus",
            "health", "/actuator/health",
            "custom", "/api/metrics/custom"
        ));
        
        return ResponseEntity.ok(summary);
    }

    /**
     * POST - Simular carga para generar métricas
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateLoad() {
        Map<String, Object> response = new HashMap<>();
        
        // Simular diferentes tipos de eventos
        for (int i = 0; i < 10; i++) {
            metricsService.incrementarEventosCreados();
            metricsService.incrementarLogsInfo();
            
            if (i % 3 == 0) {
                metricsService.incrementarLogsWarn();
            }
            
            if (i % 5 == 0) {
                metricsService.incrementarLogsError();
                metricsService.incrementarErroresSistema();
            }
            
            if (i % 2 == 0) {
                metricsService.incrementarDatosSensiblesDetectados();
                metricsService.incrementarDatosEnmascarados();
            }
        }
        
        // Simular sesiones
        metricsService.incrementarSesionesActivas();
        
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Carga simulada generada correctamente");
        response.put("events_generated", 10);
        response.put("sessions_created", 1);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET - Obtener información de métricas disponibles
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getMetricsInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("timestamp", LocalDateTime.now());
        info.put("available_metrics", Map.of(
            "counters", Map.of(
                "monitoreo.eventos.creados", "Eventos de monitoreo creados",
                "monitoreo.eventos.eliminados", "Eventos de monitoreo eliminados",
                "monitoreo.eventos.actualizados", "Eventos de monitoreo actualizados",
                "monitoreo.eventos.consultados", "Eventos de monitoreo consultados",
                "monitoreo.errores.validacion", "Errores de validación",
                "monitoreo.errores.basedatos", "Errores de base de datos",
                "monitoreo.errores.sistema", "Errores del sistema",
                "monitoreo.datos.sensibles.detectados", "Datos sensibles detectados",
                "monitoreo.datos.sensibles.enmascarados", "Datos sensibles enmascarados",
                "monitoreo.logs.nivel", "Logs por nivel (INFO, WARN, ERROR, DEBUG)"
            ),
            "timers", Map.of(
                "monitoreo.eventos.creacion.tiempo", "Tiempo de creación de eventos",
                "monitoreo.eventos.consulta.tiempo", "Tiempo de consulta de eventos",
                "monitoreo.logs.procesamiento.tiempo", "Tiempo de procesamiento de logs"
            ),
            "gauges", Map.of(
                "monitoreo.eventos.activos", "Eventos activos en el sistema",
                "monitoreo.sesiones.activas", "Sesiones activas",
                "monitoreo.eventos.errores.acumulados", "Errores acumulados"
            )
        ));
        
        info.put("endpoints", Map.of(
            "actuator_metrics", "/actuator/metrics",
            "actuator_prometheus", "/actuator/prometheus",
            "custom_metrics", "/api/metrics/custom",
            "metrics_summary", "/api/metrics/summary",
            "simulate_load", "/api/metrics/simulate"
        ));
        
        return ResponseEntity.ok(info);
    }
} 