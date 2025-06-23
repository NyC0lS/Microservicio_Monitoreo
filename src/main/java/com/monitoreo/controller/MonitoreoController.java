package com.monitoreo.controller;

import com.monitoreo.model.EventoMonitoreo;
import com.monitoreo.service.MonitoreoService;
import com.monitoreo.repository.EventoMonitoreoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador principal para el microservicio de monitoreo
 */
@RestController
@RequestMapping("/api/monitoreo")
@CrossOrigin(origins = "*")
public class MonitoreoController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoreoController.class);

    @Autowired
    private MonitoreoService monitoreoService;

    @Autowired
    private EventoMonitoreoRepository eventoRepository;

    /**
     * Endpoint de salud básico
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Solicitud de health check recibida");
        return ResponseEntity.ok(monitoreoService.checkHealth());
    }

    /**
     * Endpoint de información del servicio
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        logger.info("Solicitud de información del servicio");
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Microservicio de Monitoreo");
        info.put("description", "Servicio para monitoreo y logging de aplicaciones");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());
        info.put("systemInfo", monitoreoService.getSystemInfo());
        
        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint de prueba
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Endpoint de prueba ejecutado");
        return ResponseEntity.ok("Microservicio de monitoreo funcionando correctamente");
    }

    /**
     * Registra un nuevo evento de monitoreo
     */
    @PostMapping("/eventos-basicos")
    public ResponseEntity<EventoMonitoreo> registrarEvento(@RequestBody EventoMonitoreo evento) {
        logger.info("Registrando nuevo evento: {}", evento.getEventType());
        
        evento.setServiceName("monitoreo_loggin");
        evento.setTimestamp(LocalDateTime.now());
        
        EventoMonitoreo eventoGuardado = eventoRepository.save(evento);
        
        // Registrar en el servicio de logging
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventoId", eventoGuardado.getId());
        metadata.put("eventType", eventoGuardado.getEventType());
        metadata.put("serviceName", eventoGuardado.getServiceName());
        
        monitoreoService.logEvent(evento.getEventType(), evento.getMessage(), metadata);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoGuardado);
    }

    /**
     * Obtiene todos los eventos con paginación
     */
    @GetMapping("/eventos-basicos")
    public ResponseEntity<Page<EventoMonitoreo>> obtenerEventos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Obteniendo eventos - página: {}, tamaño: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoMonitoreo> eventos = eventoRepository.findAll(pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * Obtiene eventos por tipo
     */
    @GetMapping("/eventos-basicos/tipo/{eventType}")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosPorTipo(@PathVariable String eventType) {
        logger.info("Obteniendo eventos por tipo: {}", eventType);
        List<EventoMonitoreo> eventos = eventoRepository.findByEventType(eventType);
        return ResponseEntity.ok(eventos);
    }

    /**
     * Obtiene eventos por nivel
     */
    @GetMapping("/eventos-basicos/nivel/{level}")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosPorNivel(@PathVariable String level) {
        logger.info("Obteniendo eventos por nivel: {}", level);
        List<EventoMonitoreo> eventos = eventoRepository.findByLevel(level);
        return ResponseEntity.ok(eventos);
    }

    /**
     * Obtiene un evento por ID
     */
    @GetMapping("/eventos-basicos/{id}")
    public ResponseEntity<EventoMonitoreo> obtenerEventoPorId(@PathVariable Long id) {
        logger.info("Obteniendo evento por ID: {}", id);
        return eventoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene estadísticas de eventos
     */
    @GetMapping("/eventos-basicos/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        logger.info("Obteniendo estadísticas de eventos");
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalEventos", eventoRepository.count());
        estadisticas.put("eventosError", eventoRepository.countByLevel("ERROR"));
        estadisticas.put("eventosInfo", eventoRepository.countByLevel("INFO"));
        estadisticas.put("eventosWarning", eventoRepository.countByLevel("WARN"));
        estadisticas.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Elimina un evento por ID
     */
    @DeleteMapping("/eventos-basicos/{id}")
    public ResponseEntity<Void> eliminarEvento(@PathVariable Long id) {
        logger.info("Eliminando evento con ID: {}", id);
        
        if (eventoRepository.existsById(id)) {
            eventoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 