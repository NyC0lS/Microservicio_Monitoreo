package com.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoreo.model.EventoMonitoreo;
import com.monitoreo.repository.EventoMonitoreoRepository;
import com.monitoreo.service.MonitoreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.monitoreo.exception.GlobalExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MonitoreoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MonitoreoService monitoreoService;

    @Mock
    private EventoMonitoreoRepository eventoRepository;

    @InjectMocks
    private MonitoreoController monitoreoController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(monitoreoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void health() throws Exception {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        when(monitoreoService.checkHealth()).thenReturn(healthStatus);

        mockMvc.perform(get("/api/monitoreo/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getInfo() throws Exception {
        when(monitoreoService.getSystemInfo()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/monitoreo/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Microservicio de Monitoreo"));
    }

    @Test
    void test() throws Exception {
        mockMvc.perform(get("/api/monitoreo/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Microservicio de monitoreo funcionando correctamente"));
    }

    @Test
    void registrarEvento() throws Exception {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.setId(1L);
        evento.setEventType("TEST");

        when(eventoRepository.save(any(EventoMonitoreo.class))).thenReturn(evento);

        mockMvc.perform(post("/api/monitoreo/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventType").value("TEST"));
    }

    @Test
    void obtenerEventoPorId() throws Exception {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.setId(1L);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        mockMvc.perform(get("/api/monitoreo/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void obtenerEventoPorId_notFound() throws Exception {
        when(eventoRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/monitoreo/eventos/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEvento() throws Exception {
        when(eventoRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/monitoreo/eventos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarEvento_notFound() throws Exception {
        when(eventoRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/monitoreo/eventos/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerEventos() throws Exception {
        mockMvc.perform(get("/api/monitoreo/eventos-basicos"))
                .andExpect(status().isOk());
    }
}
