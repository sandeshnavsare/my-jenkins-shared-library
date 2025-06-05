package org.company.jenkins.pipeline

/**
 * Deploy stage implementation
 */
class DeployStage {
    def script
    
    DeployStage(script) {
        this.script = script
    }
    
    /**
     * Execute deployment stage
     * @param config Deployment configuration
     */
    def execute(Map config) {
        script.echo "🚀 Deploying ${config.appName}:${config.version} to ${config.environment}"
        
        // Validate environment
        validateEnvironment(config.environment)
        
        // Deploy based on platform
        if (config.platform == 'kubernetes' || script.fileExists('k8s/')) {
            deployToKubernetes(config)
        } else if (config.platform == 'docker' || script.fileExists('docker-compose.yml')) {
            deployWithDocker(config)
        } else {
            deployTraditional(config)
        }
        
        // Verify deployment
        verifyDeployment(config)
    }
    
    /**
     * Validate environment
     */
    private def validateEnvironment(String environment) {
        def validEnvironments = ['dev', 'staging', 'prod']
        if (!validEnvironments.contains(environment)) {
            script.error "Invalid environment: ${environment}. Valid environments: ${validEnvironments.join(', ')}"
        }
    }
    
    /**
     * Deploy to Kubernetes
     */
    private def deployToKubernetes(Map config) {
        script.echo "☸️ Deploying to Kubernetes"
        
        // Load Kubernetes manifests
        def deploymentTemplate = script.libraryResource('templates/kubernetes/deployment.yaml')
        def serviceTemplate = script.libraryResource('templates/kubernetes/service.yaml')
        
        // Replace placeholders
        deploymentTemplate = deploymentTemplate
            .replace('{{APP_NAME}}', config.appName)
            .replace('{{VERSION}}', config.version)
            .replace('{{NAMESPACE}}', config.namespace)
        
        // Write manifest files
        script.writeFile file: 'deployment.yaml', text: deploymentTemplate
        script.writeFile file: 'service.yaml', text: serviceTemplate
        
        // Apply manifests
        script.sh """
            kubectl apply -f deployment.yaml -n ${config.namespace}
            kubectl apply -f service.yaml -n ${config.namespace}
            kubectl rollout status deployment/${config.appName} -n ${config.namespace}
        """
    }
    
    /**
     * Deploy with Docker
     */
    private def deployWithDocker(Map config) {
        script.echo "🐳 Deploying with Docker"
        
        script.sh """
            docker-compose down
            docker-compose up -d
            docker-compose ps
        """
    }
    
    /**
     * Traditional deployment
     */
    private def deployTraditional(Map config) {
        script.echo "🏛️ Traditional deployment"
        
        // Execute deployment script
        def deployScript = script.libraryResource('templates/scripts/deploy.sh')
        script.writeFile file: 'deploy.sh', text: deployScript
        script.sh "chmod +x deploy.sh && ./deploy.sh ${config.environment} ${config.version}"
    }
    
    /**
     * Verify deployment
     */
    private def verifyDeployment(Map config) {
        script.echo "✅ Verifying deployment"
        
        script.timeout(time: 5, unit: 'MINUTES') {
            script.waitUntil {
                script.script {
                    try {
                        // Health check
                        def healthUrl = config.healthUrl ?: "http://172.31.26.111:8089/health"
                          def response = script.sh(
                             returnStdout: true,
                             script: "curl -f ${healthUrl} || echo 'FAILED'"
                            ).trim()

                        
                        return !response.contains('FAILED')
                    } catch (Exception e) {
                        script.echo "Health check failed: ${e.getMessage()}"
                        return false
                    }
                }
            }
        }
        
        script.echo "✅ Deployment verified successfully"
    }
}
