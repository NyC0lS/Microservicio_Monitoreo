package com.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoreo.dto.EventoMonitoreoRequest;
import com.monitoreo.exception.InvalidEventoException;
import com.monitoreo.model.EventoMonitoreo;
import com.monitoreo.repository.EventoMonitoreoRepository;
import com.monitoreo.service.MetricsService;
import com.monitoreo.service.MonitoreoService;
import com.monitoreo.validation.EventoMonitoreoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.monitoreo.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class EventoMonitoreoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventoMonitoreoRepository eventoMonitoreoRepository;

    @Mock
    private MonitoreoService monitoreoService;

    @Mock
    private EventoMonitoreoValidator eventoValidator;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private EventoMonitoreoController eventoMonitoreoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventoMonitoreoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void crearEvento_success() throws Exception {
        EventoMonitoreoRequest request = new EventoMonitoreoRequest();
        request.setEventType("TEST_EVENT");
        request.setMessage("Test message");
        request.setTimestamp(LocalDateTime.now());
        request.setServiceName("test-service");

        EventoMonitoreo eventoGuardado = new EventoMonitoreo();
        eventoGuardado.setId(1L);
        eventoGuardado.setEventType(request.getEventType());

        when(eventoMonitoreoRepository.save(any(EventoMonitoreo.class))).thenReturn(eventoGuardado);
        when(metricsService.iniciarTimerCreacionEvento()).thenReturn(null); // Timer.Sample is final

        mockMvc.perform(post("/api/monitoreo/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventType").value("TEST_EVENT"));

        verify(eventoValidator, times(1)).validateAndThrow(any(EventoMonitoreoRequest.class));
        verify(metricsService, times(1)).incrementarEventosCreados();
        verify(monitoreoService, times(1)).logEvent(any(), any(), any());
    }

    @Test
    void obtenerEventoPorId_success() throws Exception {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.setId(1L);
        evento.setEventType("FOUND_EVENT");

        when(eventoMonitoreoRepository.findById(1L)).thenReturn(Optional.of(evento));

        mockMvc.perform(get("/api/monitoreo/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventType").value("FOUND_EVENT"));
    }

    @Test
    void obtenerEventoPorId_notFound() throws Exception {
        when(eventoMonitoreoRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/monitoreo/eventos/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEvento_success() throws Exception {
        when(eventoMonitoreoRepository.findById(1L)).thenReturn(Optional.of(new EventoMonitoreo()));

        mockMvc.perform(delete("/api/monitoreo/eventos/1"))
                .andExpect(status().isNoContent());

        verify(eventoMonitoreoRepository, times(1)).deleteById(1L);
        verify(metricsService, times(1)).incrementarEventosEliminados();
    }

    @Test
    void eliminarEvento_notFound() throws Exception {
        when(eventoMonitoreoRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/monitoreo/eventos/1"))
                .andExpect(status().isNotFound());

        verify(eventoMonitoreoRepository, never()).deleteById(any());
    }

    @Test
    void eliminarEvento_DebeRetornarNoContent() throws Exception {
        when(eventoMonitoreoRepository.findById(1L)).thenReturn(Optional.of(new EventoMonitoreo()));

        mockMvc.perform(delete("/api/monitoreo/eventos/1"))
                .andExpect(status().isNoContent());
    }
} 