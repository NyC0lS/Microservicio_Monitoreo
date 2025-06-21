package com.monitoreo.repository;

import com.monitoreo.model.EventoMonitoreo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para eventos de monitoreo
 */
@Repository
public interface EventoMonitoreoRepository extends JpaRepository<EventoMonitoreo, Long> {
    
    /**
     * Busca eventos por tipo
     */
    List<EventoMonitoreo> findByEventType(String eventType);
    
    /**
     * Busca eventos por nivel
     */
    List<EventoMonitoreo> findByLevel(String level);
    
    /**
     * Busca eventos por servicio
     */
    List<EventoMonitoreo> findByServiceName(String serviceName);
    
    /**
     * Busca eventos por usuario
     */
    List<EventoMonitoreo> findByUserId(String userId);
    
    /**
     * Busca eventos por rango de fechas
     */
    List<EventoMonitoreo> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Busca eventos por sesión
     */
    List<EventoMonitoreo> findBySessionId(String sessionId);
    
    /**
     * Cuenta eventos por tipo
     */
    long countByEventType(String eventType);
    
    /**
     * Cuenta eventos por nivel
     */
    long countByLevel(String level);
    
    /**
     * Cuenta eventos por servicio
     */
    long countByServiceName(String serviceName);
    
    /**
     * Busca eventos paginados ordenados por timestamp descendente
     */
    Page<EventoMonitoreo> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Busca eventos por tipo paginados
     */
    Page<EventoMonitoreo> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);
    
    /**
     * Busca eventos por servicio paginados
     */
    Page<EventoMonitoreo> findByServiceNameOrderByTimestampDesc(String serviceName, Pageable pageable);
    
    /**
     * Query personalizada para estadísticas
     */
    @Query("SELECT e.eventType, COUNT(e) FROM EventoMonitoreo e GROUP BY e.eventType")
    List<Object[]> getEventTypeStatistics();
    
    /**
     * Query personalizada para eventos recientes
     */
    @Query("SELECT e FROM EventoMonitoreo e WHERE e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<EventoMonitoreo> findRecentEvents(@Param("since") LocalDateTime since);
    
    /**
     * Query personalizada para eventos críticos
     */
    @Query("SELECT e FROM EventoMonitoreo e WHERE e.level IN ('ERROR', 'CRITICAL') ORDER BY e.timestamp DESC")
    List<EventoMonitoreo> findCriticalEvents();
    
    /**
     * Query personalizada para eventos por usuario y rango de fechas
     */
    @Query("SELECT e FROM EventoMonitoreo e WHERE e.userId = :userId AND e.timestamp BETWEEN :start AND :end ORDER BY e.timestamp DESC")
    List<EventoMonitoreo> findByUserIdAndTimestampBetween(
        @Param("userId") String userId, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
} 