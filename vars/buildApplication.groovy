// ===== vars/buildApplication.groovy =====
/**
 * Build application pipeline step
 * @param config Map containing build configuration
 *   - appName: Application name
 *   - buildTool: Build tool (maven, gradle, npm)
 *   - dockerImage: Docker image to use for build
 *   - skipTests: Skip tests (default: false)
 */
def call(Map config) {
    def buildStage = new org.company.jenkins.pipeline.BuildStage(this)
    def dockerHelper = new org.company.jenkins.utils.DockerHelper(this)
    
    // Validate required parameters
    if (!config.appName) {
        error "appName is required"
    }
    
    // Set defaults
    config.buildTool = config.buildTool ?: 'maven'
    config.skipTests = config.skipTests ?: false
    config.dockerImage = config.dockerImage ?: 'maven:3.8.4-openjdk-11'
    
    echo "Building application: ${config.appName} using ${config.buildTool}"
    
    try {
        // Execute build stage
        buildStage.execute(config)
        
        // Build Docker image if Dockerfile exists
        if (fileExists('Dockerfile')) {
            dockerHelper.buildImage(config.appName, env.BUILD_NUMBER)
        }
        
        echo "✅ Build completed successfully for ${config.appName}"
        
    } catch (Exception e) {
        echo "❌ Build failed for ${config.appName}: ${e.getMessage()}"
        throw e
    }
}
