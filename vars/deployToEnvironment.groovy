// ===== vars/deployToEnvironment.groovy =====
/**
 * Deploy application to specified environment
 * @param config Map containing deployment configuration
 *   - environment: Target environment (dev, staging, prod)
 *   - appName: Application name
 *   - version: Version to deploy
 *   - namespace: Kubernetes namespace (optional)
 */
def call(Map config) {
    def deployStage = new org.company.jenkins.pipeline.DeployStage(this)
    def envConfig = new org.company.jenkins.config.Environment()
    
    // Validate required parameters
    if (!config.environment || !config.appName || !config.version) {
        error "environment, appName, and version are required"
    }
    
    // Get environment configuration
    def envSettings = envConfig.getEnvironmentConfig(config.environment)
    config.namespace = config.namespace ?: envSettings.namespace
    
    echo "Deploying ${config.appName}:${config.version} to ${config.environment}"
    
    try {
        // Execute deployment
        deployStage.execute(config)
        
        echo "✅ Deployment completed successfully to ${config.environment}"
        
    } catch (Exception e) {
        echo "❌ Deployment failed to ${config.environment}: ${e.getMessage()}"
        throw e
    }
}
