#!/bin/bash

# Script para probar la funcionalidad de Correlation ID
# Autor: Sistema de Monitoreo
# Fecha: $(date)

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="http://localhost:8080"
API_BASE="$BASE_URL/api"
CORRELATION_BASE="$API_BASE/test/correlation"
EVENTOS_BASE="$API_BASE/eventos"

# Función para imprimir mensajes
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Función para verificar si el servicio está corriendo
check_service() {
    print_info "Verificando si el servicio está corriendo..."
    
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_success "Servicio está corriendo en $BASE_URL"
        return 0
    else
        print_error "Servicio no está corriendo en $BASE_URL"
        print_info "Asegúrate de que el microservicio esté iniciado"
        return 1
    fi
}

# Función para generar correlation-id de prueba
generate_test_correlation() {
    echo "test-$(date +%s)-$(shuf -i 1000-9999 -n 1)"
}

# Función para probar correlation-id en logs
test_correlation_logs() {
    print_info "Probando correlation-id en logs..."
    
    local correlation_id=$(generate_test_correlation)
    
    response=$(curl -s -H "X-Correlation-ID: $correlation_id" \
                    -H "X-Request-ID: req-$(date +%s)" \
                    -H "X-User-ID: user-test-123" \
                    "$CORRELATION_BASE/logs")
    
    if echo "$response" | grep -q "$correlation_id"; then
        print_success "Correlation ID se incluye correctamente en la respuesta"
        echo "Respuesta: $response" | jq '.' 2>/dev/null || echo "Respuesta: $response"
    else
        print_error "Correlation ID no se incluye en la respuesta"
        echo "Respuesta: $response"
    fi
}

# Función para probar información de correlation
test_correlation_info() {
    print_info "Probando información de correlation..."
    
    local correlation_id=$(generate_test_correlation)
    local request_id="req-$(date +%s)"
    local user_id="user-test-456"
    
    response=$(curl -s -H "X-Correlation-ID: $correlation_id" \
                    -H "X-Request-ID: $request_id" \
                    -H "X-User-ID: $user_id" \
                    "$CORRELATION_BASE/info")
    
    if echo "$response" | grep -q "$correlation_id"; then
        print_success "Información de correlation obtenida correctamente"
        echo "Respuesta: $response" | jq '.' 2>/dev/null || echo "Respuesta: $response"
    else
        print_error "No se pudo obtener información de correlation"
        echo "Respuesta: $response"
    fi
}

# Función para probar diferentes niveles de log
test_log_levels() {
    print_info "Probando diferentes niveles de log..."
    
    local correlation_id=$(generate_test_correlation)
    
    response=$(curl -s -H "X-Correlation-ID: $correlation_id" \
                    "$CORRELATION_BASE/levels")
    
    if echo "$response" | grep -q "$correlation_id"; then
        print_success "Logs de diferentes niveles generados correctamente"
        echo "Respuesta: $response" | jq '.' 2>/dev/null || echo "Respuesta: $response"
    else
        print_error "No se pudieron generar logs de diferentes niveles"
        echo "Respuesta: $response"
    fi
}

# Función para probar creación de evento con correlation-id
test_evento_creation() {
    print_info "Probando creación de evento con correlation-id..."
    
    local correlation_id=$(generate_test_correlation)
    local request_id="req-$(date +%s)"
    
    evento_data=$(cat <<EOF
{
    "eventType": "TEST_CORRELATION",
    "message": "Prueba de correlation ID",
    "serviceName": "test-service",
    "level": "INFO",
    "userId": "user-test-789",
    "metadata": {
        "testCorrelationId": "$correlation_id",
        "testRequestId": "$request_id"
    }
}
EOF
)
    
    response=$(curl -s -X POST \
                    -H "Content-Type: application/json" \
                    -H "X-Correlation-ID: $correlation_id" \
                    -H "X-Request-ID: $request_id" \
                    -d "$evento_data" \
                    "$EVENTOS_BASE")
    
    if echo "$response" | grep -q "id"; then
        print_success "Evento creado correctamente con correlation-id"
        echo "Respuesta: $response" | jq '.' 2>/dev/null || echo "Respuesta: $response"
    else
        print_error "No se pudo crear el evento"
        echo "Respuesta: $response"
    fi
}

# Función para probar propagación de correlation-id
test_propagation() {
    print_info "Probando propagación de correlation-id..."
    
    local correlation_id=$(generate_test_correlation)
    
    response=$(curl -s -X POST \
                    -H "X-Correlation-ID: $correlation_id" \
                    "$CORRELATION_BASE/propagate?targetUrl=$BASE_URL")
    
    if echo "$response" | grep -q "propagationSuccessful.*true"; then
        print_success "Propagación de correlation-id exitosa"
        echo "Respuesta: $response" | jq '.' 2>/dev/null || echo "Respuesta: $response"
    else
        print_warning "Propagación de correlation-id falló (esto es normal si no hay otro servicio corriendo)"
        echo "Respuesta: $response"
    fi
}

# Función para verificar headers de respuesta
test_response_headers() {
    print_info "Verificando headers de respuesta..."
    
    local correlation_id=$(generate_test_correlation)
    
    headers=$(curl -s -I -H "X-Correlation-ID: $correlation_id" \
                   "$CORRELATION_BASE/info" | grep -E "(X-Correlation-ID|X-Request-ID)")
    
    if echo "$headers" | grep -q "X-Correlation-ID"; then
        print_success "Headers de correlation-id se incluyen en la respuesta"
        echo "Headers: $headers"
    else
        print_error "Headers de correlation-id no se incluyen en la respuesta"
        echo "Headers encontrados: $headers"
    fi
}

# Función para verificar logs en archivos
check_logs() {
    print_info "Verificando logs en archivos..."
    
    if [ -f "logs/monitoreo.log" ]; then
        print_success "Archivo de logs encontrado: logs/monitoreo.log"
        
        # Buscar logs recientes con correlation-id
        recent_logs=$(tail -n 50 logs/monitoreo.log | grep -c "correlationId" || echo "0")
        print_info "Logs recientes con correlation-id: $recent_logs"
        
        # Mostrar algunos logs de ejemplo
        print_info "Últimos logs con correlation-id:"
        tail -n 10 logs/monitoreo.log | grep "correlationId" | head -n 3 || print_warning "No se encontraron logs con correlation-id"
    else
        print_warning "Archivo de logs no encontrado: logs/monitoreo.log"
    fi
}

# Función para mostrar resumen
show_summary() {
    print_info "=== RESUMEN DE PRUEBAS DE CORRELATION ID ==="
    print_info "Todas las pruebas han sido ejecutadas"
    print_info "Revisa los logs para verificar la trazabilidad completa"
    print_info "Archivo de documentación: README_CORRELATION_ID.md"
}

# Función principal
main() {
    print_info "=== PRUEBAS DE CORRELATION ID ==="
    print_info "Iniciando pruebas de trazabilidad..."
    
    # Verificar que el servicio esté corriendo
    if ! check_service; then
        exit 1
    fi
    
    # Ejecutar todas las pruebas
    test_correlation_logs
    echo
    
    test_correlation_info
    echo
    
    test_log_levels
    echo
    
    test_evento_creation
    echo
    
    test_propagation
    echo
    
    test_response_headers
    echo
    
    check_logs
    echo
    
    show_summary
}

# Ejecutar función principal
main "$@" 