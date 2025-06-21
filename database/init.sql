-- Script de inicialización de la base de datos para el microservicio de monitoreo
-- Base de datos: monitoreo_db

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS monitoreo_db;

-- Conectar a la base de datos
\c monitoreo_db;

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Tabla para eventos de monitoreo general
CREATE TABLE IF NOT EXISTS eventos_monitoreo (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    service_name VARCHAR(100),
    level VARCHAR(20),
    metadata JSONB,
    user_id VARCHAR(100),
    session_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_timestamp ON eventos_monitoreo(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_event_type ON eventos_monitoreo(event_type);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_service_name ON eventos_monitoreo(service_name);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_user_id ON eventos_monitoreo(user_id);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_level ON eventos_monitoreo(level);

-- Índices compuestos para consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_service_timestamp ON eventos_monitoreo(service_name, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_type_timestamp ON eventos_monitoreo(event_type, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_user_timestamp ON eventos_monitoreo(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_level_timestamp ON eventos_monitoreo(level, timestamp DESC);

-- Índices para búsquedas de texto completo
CREATE INDEX IF NOT EXISTS idx_eventos_monitoreo_message_gin ON eventos_monitoreo USING gin(to_tsvector('spanish', message));

-- Crear usuario para la aplicación (opcional)
-- CREATE USER monitoreo_user WITH PASSWORD 'monitoreo_password';
-- GRANT ALL PRIVILEGES ON DATABASE monitoreo_db TO monitoreo_user;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO monitoreo_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO monitoreo_user;

-- Insertar datos de ejemplo (opcional)
INSERT INTO eventos_monitoreo (event_type, message, service_name, level, user_id) VALUES
('SISTEMA_INICIADO', 'Microservicio de monitoreo iniciado correctamente', 'monitoreo_loggin', 'INFO', 'sistema'),
('HEALTH_CHECK', 'Health check ejecutado exitosamente', 'monitoreo_loggin', 'INFO', 'sistema'),
('DATABASE_CONNECTION', 'Conexión a base de datos establecida', 'monitoreo_loggin', 'INFO', 'sistema'),
('SERVICE_READY', 'Servicio listo para recibir eventos', 'monitoreo_loggin', 'INFO', 'sistema')
ON CONFLICT DO NOTHING;

-- Crear vistas para consultas comunes
CREATE OR REPLACE VIEW vista_eventos_criticos AS
SELECT * FROM eventos_monitoreo 
WHERE level IN ('ERROR', 'CRITICAL') 
ORDER BY timestamp DESC;

CREATE OR REPLACE VIEW vista_eventos_recientes AS
SELECT * FROM eventos_monitoreo 
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
ORDER BY timestamp DESC;

CREATE OR REPLACE VIEW vista_eventos_por_servicio AS
SELECT service_name, COUNT(*) as total_eventos, 
       COUNT(CASE WHEN level = 'ERROR' THEN 1 END) as errores,
       COUNT(CASE WHEN level = 'WARN' THEN 1 END) as warnings,
       COUNT(CASE WHEN level = 'INFO' THEN 1 END) as info
FROM eventos_monitoreo 
GROUP BY service_name;

-- Crear función para limpiar eventos antiguos (retention policy)
CREATE OR REPLACE FUNCTION limpiar_eventos_antiguos(dias_antiguedad INTEGER DEFAULT 90)
RETURNS INTEGER AS $$
DECLARE
    eventos_eliminados INTEGER;
BEGIN
    DELETE FROM eventos_monitoreo 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * dias_antiguedad;
    
    GET DIAGNOSTICS eventos_eliminados = ROW_COUNT;
    
    RETURN eventos_eliminados;
END;
$$ LANGUAGE plpgsql;

-- Crear función para obtener estadísticas de eventos
CREATE OR REPLACE FUNCTION obtener_estadisticas_eventos()
RETURNS TABLE(
    total_eventos BIGINT,
    eventos_error BIGINT,
    eventos_warn BIGINT,
    eventos_info BIGINT,
    eventos_ultima_hora BIGINT,
    eventos_ultimas_24h BIGINT,
    servicios_activos BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_eventos,
        COUNT(CASE WHEN level = 'ERROR' THEN 1 END) as eventos_error,
        COUNT(CASE WHEN level = 'WARN' THEN 1 END) as eventos_warn,
        COUNT(CASE WHEN level = 'INFO' THEN 1 END) as eventos_info,
        COUNT(CASE WHEN timestamp >= CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 1 END) as eventos_ultima_hora,
        COUNT(CASE WHEN timestamp >= CURRENT_TIMESTAMP - INTERVAL '24 hours' THEN 1 END) as eventos_ultimas_24h,
        COUNT(DISTINCT service_name) as servicios_activos
    FROM eventos_monitoreo;
END;
$$ LANGUAGE plpgsql;

-- Comentarios sobre las tablas y funciones
COMMENT ON TABLE eventos_monitoreo IS 'Tabla para almacenar eventos generales de monitoreo del sistema';
COMMENT ON FUNCTION limpiar_eventos_antiguos IS 'Función para limpiar eventos antiguos según política de retención';
COMMENT ON FUNCTION obtener_estadisticas_eventos IS 'Función para obtener estadísticas generales de eventos';

-- Verificar que las tablas se crearon correctamente
SELECT 'Tablas creadas exitosamente' as status;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE 'eventos_%'; 