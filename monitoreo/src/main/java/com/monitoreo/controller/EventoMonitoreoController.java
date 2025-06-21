package com.monitoreo.controller;

import com.monitoreo.dto.EventoMonitoreoRequest;
import com.monitoreo.exception.EventoNotFoundException;
import com.monitoreo.exception.InvalidEventoException;
import com.monitoreo.model.EventoMonitoreo;
import com.monitoreo.repository.EventoMonitoreoRepository;
import com.monitoreo.service.MonitoreoService;
import com.monitoreo.validation.EventoMonitoreoValidator;
import com.monitoreo.service.MetricsService;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoreo.config.CorrelationIdFilter;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador CRUD completo para eventos de monitoreo con validaciones mejoradas
 * Proporciona endpoints REST para gestionar eventos de monitoreo
 */
@RestController
@RequestMapping("/api/monitoreo/eventos")
@CrossOrigin(origins = "*")
public class EventoMonitoreoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoMonitoreoController.class);

    @Autowired
    private EventoMonitoreoRepository eventoMonitoreoRepository;

    @Autowired
    private MonitoreoService monitoreoService;

    @Autowired
    private EventoMonitoreoValidator eventoValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MetricsService metricsService;

    // ==================== CREATE ====================

    /**
     * POST - Crear un nuevo evento de monitoreo con validaciones mejoradas
     */
    @PostMapping
    public ResponseEntity<EventoMonitoreo> crearEvento(@Valid @RequestBody EventoMonitoreoRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        
        logger.info("Creando nuevo evento de monitoreo - CorrelationId: {}, RequestId: {}, EventType: {}", 
                   correlationId, requestId, request.getEventType());
        
        // Iniciar timer para medir latencia
        Timer.Sample timer = metricsService.iniciarTimerCreacionEvento();
        
        try {
            eventoValidator.validateAndThrow(request);
            
            EventoMonitoreo evento = new EventoMonitoreo();
            evento.setEventType(request.getEventType());
            evento.setMessage(request.getMessage());
            evento.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
            evento.setServiceName(request.getServiceName());
            evento.setLevel(request.getLevel() != null ? request.getLevel() : "INFO");
            evento.setUserId(request.getUserId());
            evento.setSessionId(request.getSessionId());
            
            if (request.getMetadata() != null) {
                evento.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } else {
                evento.setMetadata(null);
            }
            
            EventoMonitoreo eventoGuardado = eventoMonitoreoRepository.save(evento);
            
            // Registrar métricas
            metricsService.incrementarEventosCreados();
            
            // Registrar métricas por nivel de log
            if ("INFO".equals(evento.getLevel())) {
                metricsService.incrementarLogsInfo();
            } else if ("WARN".equals(evento.getLevel())) {
                metricsService.incrementarLogsWarn();
            } else if ("ERROR".equals(evento.getLevel())) {
                metricsService.incrementarLogsError();
            } else if ("DEBUG".equals(evento.getLevel())) {
                metricsService.incrementarLogsDebug();
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventoId", eventoGuardado.getId());
            metadata.put("eventType", eventoGuardado.getEventType());
            metadata.put("serviceName", eventoGuardado.getServiceName());
            metadata.put("correlationId", correlationId);
            metadata.put("requestId", requestId);
            
            monitoreoService.logEvent("EVENTO_CREADO", "Nuevo evento de monitoreo creado", metadata);
            
            logger.info("Evento de monitoreo creado exitosamente - CorrelationId: {}, RequestId: {}, EventoId: {}", 
                       correlationId, requestId, eventoGuardado.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(eventoGuardado);
        } catch (InvalidEventoException e) {
            logger.warn("Evento inválido - CorrelationId: {}, RequestId: {}, Error: {}", 
                       correlationId, requestId, e.getMessage());
            metricsService.incrementarErroresValidacion();
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear evento de monitoreo - CorrelationId: {}, RequestId: {}, Error: {}", 
                        correlationId, requestId, e.getMessage(), e);
            metricsService.incrementarErroresSistema();
            throw new RuntimeException("Error interno al crear evento", e);
        } finally {
            // Detener timer
            metricsService.detenerTimerCreacionEvento(timer);
        }
    }

    /**
     * POST - Crear múltiples eventos de monitoreo con validaciones
     */
    @PostMapping("/batch")
    public ResponseEntity<List<EventoMonitoreo>> crearEventosBatch(@Valid @RequestBody List<EventoMonitoreoRequest> requests) {
        logger.info("Creando {} eventos de monitoreo en lote", requests.size());
        
        try {
            for (EventoMonitoreoRequest request : requests) {
                eventoValidator.validateAndThrow(request);
            }
            
            List<EventoMonitoreo> eventos = requests.stream()
                .map(request -> {
                    EventoMonitoreo evento = new EventoMonitoreo();
                    evento.setEventType(request.getEventType());
                    evento.setMessage(request.getMessage());
                    evento.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
                    evento.setServiceName(request.getServiceName());
                    evento.setLevel(request.getLevel() != null ? request.getLevel() : "INFO");
                    evento.setUserId(request.getUserId());
                    evento.setSessionId(request.getSessionId());
                    try {
                        if (request.getMetadata() != null) {
                            evento.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
                        } else {
                            evento.setMetadata(null);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException("Error al serializar metadata", ex);
                    }
                    return evento;
                })
                .toList();
            
            List<EventoMonitoreo> eventosGuardados = eventoMonitoreoRepository.saveAll(eventos);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("cantidadEventos", eventosGuardados.size());
            
            monitoreoService.logEvent("EVENTOS_BATCH_CREADOS", "Múltiples eventos de monitoreo creados", metadata);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(eventosGuardados);
        } catch (InvalidEventoException e) {
            logger.warn("Evento inválido en lote: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear eventos de monitoreo en lote", e);
            throw new RuntimeException("Error interno al crear eventos en lote", e);
        }
    }

    // ==================== READ ====================

    /**
     * GET - Obtener todos los eventos con paginación
     */
    @GetMapping
    public ResponseEntity<Page<EventoMonitoreo>> obtenerEventos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        
        logger.info("Obteniendo eventos - CorrelationId: {}, RequestId: {}, Página: {}, Tamaño: {}, Ordenado por: {}", 
                   correlationId, requestId, page, size, sortBy);
        
        // Iniciar timer para medir latencia
        Timer.Sample timer = metricsService.iniciarTimerConsultaEventos();
        
        try {
            if (page < 0) {
                throw new IllegalArgumentException("El número de página debe ser mayor o igual a 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
            }
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<EventoMonitoreo> eventos = eventoMonitoreoRepository.findAllByOrderByTimestampDesc(pageable);
            
            // Registrar métricas
            metricsService.incrementarEventosConsultados();
            
            logger.info("Eventos obtenidos exitosamente - CorrelationId: {}, RequestId: {}, Total: {}, Página: {}", 
                       correlationId, requestId, eventos.getTotalElements(), eventos.getNumber());
            
            return ResponseEntity.ok(eventos);
        } catch (IllegalArgumentException e) {
            logger.warn("Parámetros de paginación inválidos - CorrelationId: {}, RequestId: {}, Error: {}", 
                       correlationId, requestId, e.getMessage());
            metricsService.incrementarErroresValidacion();
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener eventos - CorrelationId: {}, RequestId: {}, Error: {}", 
                        correlationId, requestId, e.getMessage(), e);
            metricsService.incrementarErroresSistema();
            throw new RuntimeException("Error interno al obtener eventos", e);
        } finally {
            // Detener timer
            metricsService.detenerTimerConsultaEventos(timer);
        }
    }

    /**
     * GET - Obtener un evento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoMonitoreo> obtenerEventoPorId(@PathVariable Long id) {
        logger.info("Obteniendo evento por ID: {}", id);
        
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("El ID debe ser un número positivo");
            }
            
            Optional<EventoMonitoreo> evento = eventoMonitoreoRepository.findById(id);
            
            if (evento.isPresent()) {
                return ResponseEntity.ok(evento.get());
            } else {
                throw new EventoNotFoundException(id);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("ID inválido: {}", e.getMessage());
            throw e;
        } catch (EventoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener evento por ID: {}", id, e);
            throw new RuntimeException("Error interno al obtener evento", e);
        }
    }

    /**
     * GET - Obtener eventos por tipo
     */
    @GetMapping("/tipo/{eventType}")
    public ResponseEntity<Page<EventoMonitoreo>> obtenerEventosPorTipo(
            @PathVariable String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Obteniendo eventos por tipo: {}", eventType);
        
        try {
            if (eventType == null || eventType.trim().isEmpty()) {
                throw new IllegalArgumentException("El tipo de evento no puede estar vacío");
            }
            
            if (page < 0) {
                throw new IllegalArgumentException("El número de página debe ser mayor o igual a 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<EventoMonitoreo> eventos = eventoMonitoreoRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
            
            return ResponseEntity.ok(eventos);
        } catch (IllegalArgumentException e) {
            logger.warn("Parámetros inválidos: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener eventos por tipo: {}", eventType, e);
            throw new RuntimeException("Error interno al obtener eventos por tipo", e);
        }
    }

    /**
     * GET - Obtener eventos por servicio
     */
    @GetMapping("/servicio/{serviceName}")
    public ResponseEntity<Page<EventoMonitoreo>> obtenerEventosPorServicio(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Obteniendo eventos por servicio: {}", serviceName);
        
        try {
            if (serviceName == null || serviceName.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
            }
            
            if (page < 0) {
                throw new IllegalArgumentException("El número de página debe ser mayor o igual a 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<EventoMonitoreo> eventos = eventoMonitoreoRepository.findByServiceNameOrderByTimestampDesc(serviceName, pageable);
            
            return ResponseEntity.ok(eventos);
        } catch (IllegalArgumentException e) {
            logger.warn("Parámetros inválidos: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener eventos por servicio: {}", serviceName, e);
            throw new RuntimeException("Error interno al obtener eventos por servicio", e);
        }
    }

    /**
     * GET - Obtener eventos por nivel
     */
    @GetMapping("/nivel/{level}")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosPorNivel(@PathVariable String level) {
        logger.info("Obteniendo eventos por nivel: {}", level);
        
        try {
            if (level == null || level.trim().isEmpty()) {
                throw new IllegalArgumentException("El nivel no puede estar vacío");
            }
            
            String[] validLevels = {"INFO", "WARN", "ERROR", "DEBUG", "TRACE"};
            boolean isValid = false;
            for (String validLevel : validLevels) {
                if (validLevel.equals(level)) {
                    isValid = true;
                    break;
                }
            }
            
            if (!isValid) {
                throw new IllegalArgumentException("El nivel debe ser INFO, WARN, ERROR, DEBUG o TRACE");
            }
            
            List<EventoMonitoreo> eventos = eventoMonitoreoRepository.findByLevel(level);
            return ResponseEntity.ok(eventos);
        } catch (IllegalArgumentException e) {
            logger.warn("Nivel inválido: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener eventos por nivel: {}", level, e);
            throw new RuntimeException("Error interno al obtener eventos por nivel", e);
        }
    }

    /**
     * GET - Obtener eventos por usuario
     */
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosPorUsuario(@PathVariable String userId) {
        logger.info("Obteniendo eventos por usuario: {}", userId);
        
        try {
            List<EventoMonitoreo> eventos = eventoMonitoreoRepository.findByUserId(userId);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Error al obtener eventos por usuario: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET - Obtener eventos por rango de fechas
     */
    @GetMapping("/fecha")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosPorFecha(
            @RequestParam String inicio,
            @RequestParam String fin) {
        
        logger.info("Obteniendo eventos entre {} y {}", inicio, fin);
        
        try {
            LocalDateTime fechaInicio = LocalDateTime.parse(inicio);
            LocalDateTime fechaFin = LocalDateTime.parse(fin);
            
            List<EventoMonitoreo> eventos = eventoMonitoreoRepository.findByTimestampBetween(fechaInicio, fechaFin);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Error al obtener eventos por fecha", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET - Obtener eventos críticos
     */
    @GetMapping("/criticos")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosCriticos() {
        logger.info("Obteniendo eventos críticos");
        
        try {
            List<EventoMonitoreo> eventos = eventoMonitoreoRepository.findCriticalEvents();
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Error al obtener eventos críticos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET - Obtener eventos recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<EventoMonitoreo>> obtenerEventosRecientes(
            @RequestParam(defaultValue = "24") int horas) {
        
        logger.info("Obteniendo eventos de las últimas {} horas", horas);
        
        try {
            LocalDateTime desde = LocalDateTime.now().minusHours(horas);
            List<EventoMonitoreo> eventos = eventoMonitoreoRepository.findRecentEvents(desde);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Error al obtener eventos recientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== UPDATE ====================

    /**
     * PUT - Actualizar un evento completo
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoMonitoreo> actualizarEvento(
            @PathVariable Long id, 
            @Valid @RequestBody EventoMonitoreo evento) {
        
        logger.info("Actualizando evento con ID: {}", id);
        
        try {
            if (!eventoMonitoreoRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            evento.setId(id);
            EventoMonitoreo eventoActualizado = eventoMonitoreoRepository.save(evento);
            
            // Registrar en el servicio de logging
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventoId", id);
            metadata.put("eventType", evento.getEventType());
            
            monitoreoService.logEvent("EVENTO_ACTUALIZADO", "Evento de monitoreo actualizado", metadata);
            
            return ResponseEntity.ok(eventoActualizado);
        } catch (Exception e) {
            logger.error("Error al actualizar evento con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PATCH - Actualizar parcialmente un evento
     */
    @PatchMapping("/{id}")
    public ResponseEntity<EventoMonitoreo> actualizarEventoParcial(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> actualizaciones) {
        
        logger.info("Actualizando parcialmente evento con ID: {}", id);
        
        try {
            Optional<EventoMonitoreo> eventoOpt = eventoMonitoreoRepository.findById(id);
            
            if (!eventoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            EventoMonitoreo evento = eventoOpt.get();
            
            // Aplicar actualizaciones parciales
            if (actualizaciones.containsKey("message")) {
                evento.setMessage((String) actualizaciones.get("message"));
            }
            if (actualizaciones.containsKey("level")) {
                evento.setLevel((String) actualizaciones.get("level"));
            }
            if (actualizaciones.containsKey("metadata")) {
                evento.setMetadata((String) actualizaciones.get("metadata"));
            }
            
            EventoMonitoreo eventoActualizado = eventoMonitoreoRepository.save(evento);
            
            // Registrar en el servicio de logging
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventoId", id);
            metadata.put("camposActualizados", actualizaciones.keySet());
            
            monitoreoService.logEvent("EVENTO_ACTUALIZADO_PARCIAL", "Evento de monitoreo actualizado parcialmente", metadata);
            
            return ResponseEntity.ok(eventoActualizado);
        } catch (Exception e) {
            logger.error("Error al actualizar parcialmente evento con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== DELETE ====================

    /**
     * DELETE - Eliminar un evento por ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEvento(@PathVariable Long id) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        
        logger.info("Eliminando evento - CorrelationId: {}, RequestId: {}, EventoId: {}", 
                   correlationId, requestId, id);
        
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("El ID debe ser un número positivo");
            }
            
            Optional<EventoMonitoreo> evento = eventoMonitoreoRepository.findById(id);
            
            if (evento.isPresent()) {
                eventoMonitoreoRepository.deleteById(id);
                
                // Registrar métricas
                metricsService.incrementarEventosEliminados();
                
                logger.info("Evento eliminado exitosamente - CorrelationId: {}, RequestId: {}, EventoId: {}", 
                           correlationId, requestId, id);
                return ResponseEntity.noContent().build();
            } else {
                throw new EventoNotFoundException(id);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("ID inválido - CorrelationId: {}, RequestId: {}, Error: {}", 
                       correlationId, requestId, e.getMessage());
            metricsService.incrementarErroresValidacion();
            throw e;
        } catch (EventoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar evento - CorrelationId: {}, RequestId: {}, EventoId: {}, Error: {}", 
                        correlationId, requestId, id, e.getMessage(), e);
            metricsService.incrementarErroresSistema();
            throw new RuntimeException("Error interno al eliminar evento", e);
        }
    }

    /**
     * DELETE - Eliminar eventos por tipo
     */
    @DeleteMapping("/tipo/{eventType}")
    public ResponseEntity<Map<String, Object>> eliminarEventosPorTipo(@PathVariable String eventType) {
        logger.info("Eliminando eventos por tipo: {}", eventType);
        
        try {
            List<EventoMonitoreo> eventosAEliminar = eventoMonitoreoRepository.findByEventType(eventType);
            long cantidadEliminados = eventosAEliminar.size();
            
            eventoMonitoreoRepository.deleteAll(eventosAEliminar);
            
            // Registrar en el servicio de logging
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventType", eventType);
            metadata.put("cantidadEliminados", cantidadEliminados);
            
            monitoreoService.logEvent("EVENTOS_ELIMINADOS_POR_TIPO", "Eventos eliminados por tipo", metadata);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Eventos eliminados exitosamente");
            respuesta.put("tipoEvento", eventType);
            respuesta.put("cantidadEliminados", cantidadEliminados);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            logger.error("Error al eliminar eventos por tipo: {}", eventType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE - Eliminar eventos por rango de fechas
     */
    @DeleteMapping("/fecha")
    public ResponseEntity<Map<String, Object>> eliminarEventosPorFecha(
            @RequestParam String inicio,
            @RequestParam String fin) {
        
        logger.info("Eliminando eventos entre {} y {}", inicio, fin);
        
        try {
            LocalDateTime fechaInicio = LocalDateTime.parse(inicio);
            LocalDateTime fechaFin = LocalDateTime.parse(fin);
            
            List<EventoMonitoreo> eventosAEliminar = eventoMonitoreoRepository.findByTimestampBetween(fechaInicio, fechaFin);
            long cantidadEliminados = eventosAEliminar.size();
            
            eventoMonitoreoRepository.deleteAll(eventosAEliminar);
            
            // Registrar en el servicio de logging
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fechaInicio", inicio);
            metadata.put("fechaFin", fin);
            metadata.put("cantidadEliminados", cantidadEliminados);
            
            monitoreoService.logEvent("EVENTOS_ELIMINADOS_POR_FECHA", "Eventos eliminados por rango de fechas", metadata);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Eventos eliminados exitosamente");
            respuesta.put("fechaInicio", inicio);
            respuesta.put("fechaFin", fin);
            respuesta.put("cantidadEliminados", cantidadEliminados);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            logger.error("Error al eliminar eventos por fecha", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE - Eliminar todos los eventos
     */
    @DeleteMapping("/todos")
    public ResponseEntity<Map<String, Object>> eliminarTodosLosEventos() {
        logger.warn("Eliminando TODOS los eventos de monitoreo");
        
        try {
            long cantidadTotal = eventoMonitoreoRepository.count();
            eventoMonitoreoRepository.deleteAll();
            
            // Registrar en el servicio de logging
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("cantidadEliminados", cantidadTotal);
            
            monitoreoService.logEvent("TODOS_EVENTOS_ELIMINADOS", "Todos los eventos de monitoreo eliminados", metadata);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Todos los eventos eliminados exitosamente");
            respuesta.put("cantidadEliminados", cantidadTotal);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            logger.error("Error al eliminar todos los eventos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * GET - Obtener estadísticas de eventos
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        logger.info("Obteniendo estadísticas de eventos");
        
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalEventos", eventoMonitoreoRepository.count());
            estadisticas.put("eventosError", eventoMonitoreoRepository.countByLevel("ERROR"));
            estadisticas.put("eventosInfo", eventoMonitoreoRepository.countByLevel("INFO"));
            estadisticas.put("eventosWarning", eventoMonitoreoRepository.countByLevel("WARN"));
            estadisticas.put("eventosCritical", eventoMonitoreoRepository.countByLevel("CRITICAL"));
            estadisticas.put("timestamp", LocalDateTime.now());
            
            // Estadísticas por tipo de evento
            List<Object[]> estadisticasPorTipo = eventoMonitoreoRepository.getEventTypeStatistics();
            Map<String, Long> eventosPorTipo = new HashMap<>();
            for (Object[] stat : estadisticasPorTipo) {
                eventosPorTipo.put((String) stat[0], (Long) stat[1]);
            }
            estadisticas.put("eventosPorTipo", eventosPorTipo);
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET - Health check específico para eventos
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Health check para eventos de monitoreo");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "eventos_monitoreo");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("totalEventos", eventoMonitoreoRepository.count());
        
        return ResponseEntity.ok(healthStatus);
    }
} 