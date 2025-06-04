# ===== resources/templates/scripts/deploy.sh =====
#!/bin/bash
set -euo pipefail

# Deployment script template
# Usage: ./deploy.sh [ENVIRONMENT] [VERSION] [APP_NAME]

ENVIRONMENT=${1:-dev}
VERSION=${2:-latest}
APP_NAME=${3:-{{APP_NAME}}}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

# Validate environment
validate_environment() {
    case $ENVIRONMENT in
        dev|staging|prod)
            log_info "✅ Valid environment: $ENVIRONMENT"
            ;;
        *)
            log_error "❌ Invalid environment: $ENVIRONMENT. Valid options: dev, staging, prod"
            exit 1
            ;;
    esac
}

# Pre-deployment checks
pre_deployment_checks() {
    log_info "🔍 Running pre-deployment checks..."
    
    # Check if kubectl is available and configured
    if command -v kubectl &> /dev/null; then
        kubectl version --client --short
        log_info "✅ kubectl is available"
    else
        log_warn "⚠️ kubectl not found, skipping Kubernetes deployment"
    fi
    
    # Check if Docker is available
    if command -v docker &> /dev/null; then
        docker version --format 'Client: {{.Client.Version}}'
        log_info "✅ Docker is available"
    else
        log_warn "⚠️ Docker not found"
    fi
    
    # Check if image exists
    if docker images | grep -q "$APP_NAME:$VERSION"; then
        log_info "✅ Docker image $APP_NAME:$VERSION found locally"
    else
        log_warn "⚠️ Docker image $APP_NAME:$VERSION not found locally"
    fi
}

# Deploy to Kubernetes
deploy_kubernetes() {
    log_info "☸️ Deploying to Kubernetes ($ENVIRONMENT)..."
    
    # Set namespace based on environment
    case $ENVIRONMENT in
        dev)
            NAMESPACE="development"
            ;;
        staging)
            NAMESPACE="staging"
            ;;
        prod)
            NAMESPACE="production"
            ;;
    esac
    
    log_info "📦 Deploying to namespace: $NAMESPACE"
    
    # Apply Kubernetes manifests
    if [[ -f "k8s/deployment.yaml" ]]; then
        kubectl apply -f k8s/ -n $NAMESPACE
        kubectl rollout status deployment/$APP_NAME -n $NAMESPACE --timeout=300s
    else
        log_warn "⚠️ No Kubernetes manifests found in k8s/ directory"
    fi
}

# Deploy with Docker Compose
deploy_docker_compose() {
    log_info "🐳 Deploying with Docker Compose..."
    
    # Set environment file
    ENV_FILE=".env.$ENVIRONMENT"
    
    if [[ -f "$ENV_FILE" ]]; then
        log_info "📋 Using environment file: $ENV_FILE"
        export $(cat $ENV_FILE | xargs)
    fi
    
    # Set version for docker-compose
    export VERSION=$VERSION
    export APP_NAME=$APP_NAME
    
    # Deploy
    docker-compose down --remove-orphans
    docker-compose up -d
    
    # Wait for services to be ready
    sleep 10
    docker-compose ps
}

# Health check
health_check() {
    log_info "🏥 Performing health check..."
    
    # Determine health check URL based on environment
    case $ENVIRONMENT in
        dev)
            HEALTH_URL="http://localhost:8080/actuator/health"
            ;;
        staging)
            HEALTH_URL="https://app.staging.company.com/actuator/health"
            ;;
        prod)
            HEALTH_URL="https://app.company.com/actuator/health"
            ;;
    esac
    
    log_info "🔍 Checking health at: $HEALTH_URL"
    
    # Wait for application to be ready
    RETRY_COUNT=0
    MAX_RETRIES=30
    
    while [[ $RETRY_COUNT -lt $MAX_RETRIES ]]; do
        if curl -f -s "$HEALTH_URL" > /dev/null 2>&1; then
            log_info "✅ Health check passed!"
            return 0
        else
            log_debug "⏳ Health check failed, retry $((RETRY_COUNT + 1))/$MAX_RETRIES..."
            sleep 10
            ((RETRY_COUNT++))
        fi
    done
    
    log_error "❌ Health check failed after $MAX_RETRIES attempts"
    return 1
}

# Rollback function
rollback() {
    log_error "🔄 Initiating rollback..."
    
    if command -v kubectl &> /dev/null; then
        kubectl rollout undo deployment/$APP_NAME -n $NAMESPACE
        log_info "✅ Kubernetes rollback completed"
    fi
    
    exit 1
}

# Main deployment flow
main() {
    log_info "🚀 Starting deployment of $APP_NAME:$VERSION to $ENVIRONMENT"
    
    validate_environment
    pre_deployment_checks
    
    # Choose deployment method
    if [[ -d "k8s" ]] && command -v kubectl &> /dev/null; then
        deploy_kubernetes
    elif [[ -f "docker-compose.yml" ]]; then
        deploy_docker_compose
    else
        log_error "❌ No supported deployment method found"
        exit 1
    fi
    
    # Perform health check
    if health_check; then
        log_info "🎉 Deployment completed successfully!"
    else
        log_error "❌ Deployment failed health check"
        rollback
    fi
}

# Trap errors and rollback
trap rollback ERR

# Run main function
main

---
