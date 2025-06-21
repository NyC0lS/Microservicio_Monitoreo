#!/bin/bash

# Script para validar métricas y logs de ambos entornos
# Uso: ./validate-metrics.sh [production|staging|both]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Función para validar entorno
validate_environment() {
    local env=$1
    local port=$2
    local prometheus_port=$3
    local grafana_port=$4
    
    print_status "Validando entorno: $env"
    
    # Validar microservicio
    print_status "Verificando microservicio en puerto $port..."
    if curl -f -s "http://localhost:$port/actuator/health" > /dev/null; then
        print_status "✓ Microservicio $env está funcionando"
    else
        print_error "✗ Microservicio $env no está respondiendo"
        return 1
    fi
    
    # Validar métricas
    print_status "Verificando métricas en puerto $port..."
    if curl -f -s "http://localhost:$port/actuator/metrics" > /dev/null; then
        print_status "✓ Endpoint de métricas $env está funcionando"
    else
        print_error "✗ Endpoint de métricas $env no está respondiendo"
        return 1
    fi
    
    # Validar Prometheus
    print_status "Verificando Prometheus en puerto $prometheus_port..."
    if curl -f -s "http://localhost:$prometheus_port/api/v1/status/config" > /dev/null; then
        print_status "✓ Prometheus $env está funcionando"
    else
        print_error "✗ Prometheus $env no está respondiendo"
        return 1
    fi
    
    # Validar Grafana
    print_status "Verificando Grafana en puerto $grafana_port..."
    if curl -f -s "http://localhost:$grafana_port/api/health" > /dev/null; then
        print_status "✓ Grafana $env está funcionando"
    else
        print_error "✗ Grafana $env no está respondiendo"
        return 1
    fi
    
    # Validar métricas específicas
    print_status "Validando métricas específicas..."
    local metrics_response=$(curl -s "http://localhost:$port/actuator/prometheus")
    
    # Verificar métricas de eventos
    if echo "$metrics_response" | grep -q "monitoreo_eventos_creados_total"; then
        print_status "✓ Métricas de eventos están siendo recolectadas"
    else
        print_warning "⚠ Métricas de eventos no encontradas"
    fi
    
    # Verificar métricas de errores
    if echo "$metrics_response" | grep -q "monitoreo_errores"; then
        print_status "✓ Métricas de errores están siendo recolectadas"
    else
        print_warning "⚠ Métricas de errores no encontradas"
    fi
    
    # Verificar métricas de latencia
    if echo "$metrics_response" | grep -q "monitoreo_eventos_creacion_tiempo"; then
        print_status "✓ Métricas de latencia están siendo recolectadas"
    else
        print_warning "⚠ Métricas de latencia no encontradas"
    fi
    
    print_status "Validación de $env completada"
    echo "----------------------------------------"
}

# Función para verificar consumo de recursos
check_resource_usage() {
    local env=$1
    local container_prefix=$2
    
    print_status "Verificando consumo de recursos para $env..."
    
    # Verificar uso de CPU y memoria
    local cpu_usage=$(docker stats --no-stream --format "table {{.CPUPerc}}" "$container_prefix-monitoreo" 2>/dev/null | tail -n 1 | tr -d '%' | tr -d ' ')
    local mem_usage=$(docker stats --no-stream --format "table {{.MemPerc}}" "$container_prefix-monitoreo" 2>/dev/null | tail -n 1 | tr -d '%' | tr -d ' ')
    
    if [ ! -z "$cpu_usage" ] && [ ! -z "$mem_usage" ]; then
        print_status "Consumo de recursos $env:"
        print_status "  CPU: ${cpu_usage}%"
        print_status "  Memoria: ${mem_usage}%"
        
        # Alertar si el consumo es alto
        if (( $(echo "$cpu_usage > 80" | bc -l) )); then
            print_warning "⚠ Alto consumo de CPU en $env: ${cpu_usage}%"
        fi
        
        if (( $(echo "$mem_usage > 80" | bc -l) )); then
            print_warning "⚠ Alto consumo de memoria en $env: ${mem_usage}%"
        fi
    else
        print_warning "⚠ No se pudo obtener estadísticas de recursos para $env"
    fi
}

# Función principal
main() {
    local target=${1:-both}
    
    print_status "Iniciando validación de métricas y logs..."
    
    case $target in
        "production")
            validate_environment "production" 8080 9090 3000
            check_resource_usage "production" "monitoreo"
            ;;
        "staging")
            validate_environment "staging" 8081 9091 3001
            check_resource_usage "staging" "monitoreo-staging"
            ;;
        "both")
            validate_environment "production" 8080 9090 3000
            check_resource_usage "production" "monitoreo"
            echo
            validate_environment "staging" 8081 9091 3001
            check_resource_usage "staging" "monitoreo-staging"
            ;;
        *)
            print_error "Uso: $0 [production|staging|both]"
            exit 1
            ;;
    esac
    
    print_status "Validación completada"
}

# Ejecutar función principal
main "$@" 