def call() {
    def version = "v1.0.${env.BUILD_NUMBER}"
    echo "🔧 Generated version: ${version}"
    return version
}
