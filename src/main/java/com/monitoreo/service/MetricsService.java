package com.monitoreo.service;

import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio para gestionar métricas personalizadas del microservicio de monitoreo
 */
@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    @Autowired
    private MeterRegistry meterRegistry;

    // Contadores para eventos de monitoreo
    private final Counter eventosCreadosCounter;
    private final Counter eventosEliminadosCounter;
    private final Counter eventosActualizadosCounter;
    private final Counter eventosConsultadosCounter;

    // Contadores para errores
    private final Counter erroresValidacionCounter;
    private final Counter erroresBaseDatosCounter;
    private final Counter erroresSistemaCounter;

    // Contadores para datos sensibles
    private final Counter datosSensiblesDetectadosCounter;
    private final Counter datosEnmascaradosCounter;

    // Timers para medir latencia
    private final Timer creacionEventoTimer;
    private final Timer consultaEventosTimer;
    private final Timer procesamientoLogsTimer;

    // Gauges para métricas de estado
    private final AtomicLong eventosActivosGauge;
    private final AtomicLong sesionesActivasGauge;
    private final AtomicLong erroresAcumuladosGauge;

    // Distribución de eventos por nivel
    private final Counter infoLogsCounter;
    private final Counter warnLogsCounter;
    private final Counter errorLogsCounter;
    private final Counter debugLogsCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Inicializar contadores
        this.eventosCreadosCounter = Counter.builder("monitoreo.eventos.creados")
                .description("Número total de eventos de monitoreo creados")
                .tag("tipo", "creacion")
                .register(meterRegistry);

        this.eventosEliminadosCounter = Counter.builder("monitoreo.eventos.eliminados")
                .description("Número total de eventos de monitoreo eliminados")
                .tag("tipo", "eliminacion")
                .register(meterRegistry);

        this.eventosActualizadosCounter = Counter.builder("monitoreo.eventos.actualizados")
                .description("Número total de eventos de monitoreo actualizados")
                .tag("tipo", "actualizacion")
                .register(meterRegistry);

        this.eventosConsultadosCounter = Counter.builder("monitoreo.eventos.consultados")
                .description("Número total de eventos de monitoreo consultados")
                .tag("tipo", "consulta")
                .register(meterRegistry);

        // Contadores de errores
        this.erroresValidacionCounter = Counter.builder("monitoreo.errores.validacion")
                .description("Número total de errores de validación")
                .tag("tipo", "validacion")
                .register(meterRegistry);

        this.erroresBaseDatosCounter = Counter.builder("monitoreo.errores.basedatos")
                .description("Número total de errores de base de datos")
                .tag("tipo", "basedatos")
                .register(meterRegistry);

        this.erroresSistemaCounter = Counter.builder("monitoreo.errores.sistema")
                .description("Número total de errores del sistema")
                .tag("tipo", "sistema")
                .register(meterRegistry);

        // Contadores de datos sensibles
        this.datosSensiblesDetectadosCounter = Counter.builder("monitoreo.datos.sensibles.detectados")
                .description("Número total de datos sensibles detectados")
                .tag("tipo", "deteccion")
                .register(meterRegistry);

        this.datosEnmascaradosCounter = Counter.builder("monitoreo.datos.sensibles.enmascarados")
                .description("Número total de datos sensibles enmascarados")
                .tag("tipo", "enmascaramiento")
                .register(meterRegistry);

        // Timers para latencia
        this.creacionEventoTimer = Timer.builder("monitoreo.eventos.creacion.tiempo")
                .description("Tiempo de creación de eventos de monitoreo")
                .tag("operacion", "creacion")
                .register(meterRegistry);

        this.consultaEventosTimer = Timer.builder("monitoreo.eventos.consulta.tiempo")
                .description("Tiempo de consulta de eventos de monitoreo")
                .tag("operacion", "consulta")
                .register(meterRegistry);

        this.procesamientoLogsTimer = Timer.builder("monitoreo.logs.procesamiento.tiempo")
                .description("Tiempo de procesamiento de logs")
                .tag("operacion", "procesamiento")
                .register(meterRegistry);

        // Gauges para métricas de estado
        this.eventosActivosGauge = new AtomicLong(0);
        Gauge.builder("monitoreo.eventos.activos", eventosActivosGauge, AtomicLong::doubleValue)
                .description("Número de eventos activos en el sistema")
                .register(meterRegistry);

        this.sesionesActivasGauge = new AtomicLong(0);
        Gauge.builder("monitoreo.sesiones.activas", sesionesActivasGauge, AtomicLong::doubleValue)
                .description("Número de sesiones activas")
                .register(meterRegistry);

        this.erroresAcumuladosGauge = new AtomicLong(0);
        Gauge.builder("monitoreo.eventos.errores.acumulados", erroresAcumuladosGauge, AtomicLong::doubleValue)
                .description("Número acumulado de errores")
                .register(meterRegistry);

        // Contadores por nivel de log
        this.infoLogsCounter = Counter.builder("monitoreo.logs.nivel")
                .description("Logs por nivel")
                .tag("nivel", "INFO")
                .register(meterRegistry);

        this.warnLogsCounter = Counter.builder("monitoreo.logs.nivel")
                .description("Logs por nivel")
                .tag("nivel", "WARN")
                .register(meterRegistry);

        this.errorLogsCounter = Counter.builder("monitoreo.logs.nivel")
                .description("Logs por nivel")
                .tag("nivel", "ERROR")
                .register(meterRegistry);

        this.debugLogsCounter = Counter.builder("monitoreo.logs.nivel")
                .description("Logs por nivel")
                .tag("nivel", "DEBUG")
                .register(meterRegistry);

        logger.info("Servicio de métricas inicializado correctamente");
    }

    // Métodos para eventos de monitoreo
    public void incrementarEventosCreados() {
        eventosCreadosCounter.increment();
        eventosActivosGauge.incrementAndGet();
        logger.debug("Métrica: Evento creado incrementado");
    }

    public void incrementarEventosEliminados() {
        eventosEliminadosCounter.increment();
        eventosActivosGauge.decrementAndGet();
        logger.debug("Métrica: Evento eliminado incrementado");
    }

    public void incrementarEventosActualizados() {
        eventosActualizadosCounter.increment();
        logger.debug("Métrica: Evento actualizado incrementado");
    }

    public void incrementarEventosConsultados() {
        eventosConsultadosCounter.increment();
        logger.debug("Métrica: Evento consultado incrementado");
    }

    // Métodos para errores
    public void incrementarErroresValidacion() {
        erroresValidacionCounter.increment();
        erroresAcumuladosGauge.incrementAndGet();
        logger.warn("Métrica: Error de validación incrementado");
    }

    public void incrementarErroresBaseDatos() {
        erroresBaseDatosCounter.increment();
        erroresAcumuladosGauge.incrementAndGet();
        logger.error("Métrica: Error de base de datos incrementado");
    }

    public void incrementarErroresSistema() {
        erroresSistemaCounter.increment();
        erroresAcumuladosGauge.incrementAndGet();
        logger.error("Métrica: Error del sistema incrementado");
    }

    // Métodos para datos sensibles
    public void incrementarDatosSensiblesDetectados() {
        datosSensiblesDetectadosCounter.increment();
        logger.debug("Métrica: Datos sensibles detectados incrementado");
    }

    public void incrementarDatosEnmascarados() {
        datosEnmascaradosCounter.increment();
        logger.debug("Métrica: Datos enmascarados incrementado");
    }

    // Métodos para timers
    public Timer.Sample iniciarTimerCreacionEvento() {
        return Timer.start(meterRegistry);
    }

    public void detenerTimerCreacionEvento(Timer.Sample sample) {
        sample.stop(creacionEventoTimer);
        logger.debug("Métrica: Timer de creación de evento detenido");
    }

    public Timer.Sample iniciarTimerConsultaEventos() {
        return Timer.start(meterRegistry);
    }

    public void detenerTimerConsultaEventos(Timer.Sample sample) {
        sample.stop(consultaEventosTimer);
        logger.debug("Métrica: Timer de consulta de eventos detenido");
    }

    public Timer.Sample iniciarTimerProcesamientoLogs() {
        return Timer.start(meterRegistry);
    }

    public void detenerTimerProcesamientoLogs(Timer.Sample sample) {
        sample.stop(procesamientoLogsTimer);
        logger.debug("Métrica: Timer de procesamiento de logs detenido");
    }

    // Métodos para logs por nivel
    public void incrementarLogsInfo() {
        infoLogsCounter.increment();
    }

    public void incrementarLogsWarn() {
        warnLogsCounter.increment();
    }

    public void incrementarLogsError() {
        errorLogsCounter.increment();
    }

    public void incrementarLogsDebug() {
        debugLogsCounter.increment();
    }

    // Métodos para gestionar sesiones
    public void incrementarSesionesActivas() {
        sesionesActivasGauge.incrementAndGet();
        logger.debug("Métrica: Sesión activa incrementada");
    }

    public void decrementarSesionesActivas() {
        sesionesActivasGauge.decrementAndGet();
        logger.debug("Métrica: Sesión activa decrementada");
    }

    // Método para registrar métricas de rendimiento del sistema
    public void registrarMetricaRendimiento(String nombre, double valor, String unidad) {
        Gauge.builder("monitoreo.rendimiento." + nombre, () -> valor)
                .description("Métrica de rendimiento: " + nombre)
                .tag("unidad", unidad)
                .register(meterRegistry);
        logger.debug("Métrica de rendimiento registrada: {} = {} {}", nombre, valor, unidad);
    }

    // Método para registrar métricas de negocio
    public void registrarMetricaNegocio(String nombre, double valor, String categoria) {
        Gauge.builder("monitoreo.negocio." + nombre, () -> valor)
                .description("Métrica de negocio: " + nombre)
                .tag("categoria", categoria)
                .register(meterRegistry);
        logger.debug("Métrica de negocio registrada: {} = {} ({})", nombre, valor, categoria);
    }

    // Método para obtener estadísticas resumidas
    public void registrarEstadisticasResumidas() {
        long eventosActivos = eventosActivosGauge.get();
        long sesionesActivas = sesionesActivasGauge.get();
        long erroresAcumulados = erroresAcumuladosGauge.get();

        logger.info("Estadísticas del sistema - Eventos activos: {}, Sesiones activas: {}, Errores acumulados: {}", 
                   eventosActivos, sesionesActivas, erroresAcumulados);
    }
} 