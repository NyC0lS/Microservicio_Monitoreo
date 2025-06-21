package com.monitoreo.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsServiceTest {

    private MetricsService metricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void incrementarEventosCreados() {
        metricsService.incrementarEventosCreados();
        Counter counter = meterRegistry.find("monitoreo.eventos.creados").counter();
        Gauge gauge = meterRegistry.find("monitoreo.eventos.activos").gauge();
        assertEquals(1.0, counter.count());
        assertEquals(1.0, gauge.value());
    }

    @Test
    void incrementarEventosEliminados() {
        // Increment first to avoid negative gauge
        metricsService.incrementarEventosCreados();
        metricsService.incrementarEventosEliminados();
        
        Counter counter = meterRegistry.find("monitoreo.eventos.eliminados").counter();
        Gauge gauge = meterRegistry.find("monitoreo.eventos.activos").gauge();
        
        assertEquals(1.0, counter.count());
        assertEquals(0.0, gauge.value());
    }

    @Test
    void incrementarErroresValidacion() {
        metricsService.incrementarErroresValidacion();
        Counter counter = meterRegistry.find("monitoreo.errores.validacion").counter();
        Gauge gauge = meterRegistry.find("monitoreo.eventos.errores.acumulados").gauge();
        assertEquals(1.0, counter.count());
        assertEquals(1.0, gauge.value());
    }

    @Test
    void timers() {
        // Test creacionEventoTimer
        Timer.Sample creacionSample = metricsService.iniciarTimerCreacionEvento();
        metricsService.detenerTimerCreacionEvento(creacionSample);
        Timer creacionTimer = meterRegistry.find("monitoreo.eventos.creacion.tiempo").timer();
        assertNotNull(creacionTimer);
        assertEquals(1, creacionTimer.count());

        // Test consultaEventosTimer
        Timer.Sample consultaSample = metricsService.iniciarTimerConsultaEventos();
        metricsService.detenerTimerConsultaEventos(consultaSample);
        Timer consultaTimer = meterRegistry.find("monitoreo.eventos.consulta.tiempo").timer();
        assertNotNull(consultaTimer);
        assertEquals(1, consultaTimer.count());
    }
    
    @Test
    void incrementarLogsPorNivel() {
        metricsService.incrementarLogsInfo();
        metricsService.incrementarLogsWarn();
        metricsService.incrementarLogsError();
        metricsService.incrementarLogsDebug();

        assertEquals(1.0, meterRegistry.find("monitoreo.logs.nivel").tag("nivel", "INFO").counter().count());
        assertEquals(1.0, meterRegistry.find("monitoreo.logs.nivel").tag("nivel", "WARN").counter().count());
        assertEquals(1.0, meterRegistry.find("monitoreo.logs.nivel").tag("nivel", "ERROR").counter().count());
        assertEquals(1.0, meterRegistry.find("monitoreo.logs.nivel").tag("nivel", "DEBUG").counter().count());
    }
    
    @Test
    void registrarMetricaNegocio() {
        metricsService.registrarMetricaNegocio("ventas", 120.5, "libros");
        Gauge gauge = meterRegistry.find("monitoreo.negocio.ventas").tag("categoria", "libros").gauge();
        assertNotNull(gauge);
        assertEquals(120.5, gauge.value());
    }
} 