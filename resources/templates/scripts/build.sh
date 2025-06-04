# ===== resources/templates/scripts/build.sh =====
#!/bin/bash
set -euo pipefail

# Build script template for {{APP_NAME}}
# Usage: ./build.sh [BUILD_TOOL] [SKIP_TESTS]

BUILD_TOOL=${1:-maven}
SKIP_TESTS=${2:-false}
APP_NAME="{{APP_NAME}}"
VERSION=${BUILD_NUMBER:-latest}

echo "🔨 Starting build for $APP_NAME using $BUILD_TOOL"
echo "📋 Build Number: $VERSION"
echo "⏭️ Skip Tests: $SKIP_TESTS"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Function to build with Maven
build_maven() {
    log_info "Building with Maven..."
    
    if [[ "$SKIP_TESTS" == "true" ]]; then
        mvn clean package -DskipTests -B -V
    else
        mvn clean package -B -V
    fi
    
    # Copy artifacts
    mkdir -p dist/
    cp target/*.jar dist/ 2>/dev/null || log_warn "No JAR files found in target/"
}

# Function to build with Gradle
build_gradle() {
    log_info "Building with Gradle..."
    
    # Make gradlew executable
    chmod +x ./gradlew
    
    if [[ "$SKIP_TESTS" == "true" ]]; then
        ./gradlew clean build -x test --no-daemon
    else
        ./gradlew clean build --no-daemon
    fi
    
    # Copy artifacts
    mkdir -p dist/
    cp build/libs/*.jar dist/ 2>/dev/null || log_warn "No JAR files found in build/libs/"
}

# Function to build with NPM
build_npm() {
    log_info "Building with NPM..."
    
    # Install dependencies
    npm ci
    
    # Run build
    npm run build
    
    # Run tests if not skipped
    if [[ "$SKIP_TESTS" != "true" ]]; then
        npm test
    fi
    
    # Copy artifacts
    mkdir -p dist/
    cp -r build/* dist/ 2>/dev/null || cp -r dist/* dist/ 2>/dev/null || log_warn "No build artifacts found"
}

# Main build logic
case $BUILD_TOOL in
    maven)
        build_maven
        ;;
    gradle)
        build_gradle
        ;;
    npm)
        build_npm
        ;;
    *)
        log_error "Unsupported build tool: $BUILD_TOOL"
        exit 1
        ;;
esac

log_info "✅ Build completed successfully!"

# Generate build info
cat > dist/build-info.json << EOF
{
    "appName": "$APP_NAME",
    "version": "$VERSION",
    "buildTool": "$BUILD_TOOL",
    "buildTime": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "gitCommit": "$(git rev-parse HEAD 2>/dev/null || echo 'unknown')",
    "gitBranch": "$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'unknown')"
}
EOF

log_info "📋 Build info saved to dist/build-info.json"

---
