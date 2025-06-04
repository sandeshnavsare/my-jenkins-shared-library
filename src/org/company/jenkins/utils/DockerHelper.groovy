package org.company.jenkins.utils

/**
 * Docker utility class for container operations
 */
class DockerHelper {
    def script
    
    DockerHelper(script) {
        this.script = script
    }
    
    /**
     * Build Docker image
     * @param imageName Name of the image
     * @param tag Tag for the image
     * @param dockerfile Path to Dockerfile (optional)
     */
    def buildImage(String imageName, String tag, String dockerfile = 'Dockerfile') {
        script.echo "🐳 Building Docker image: ${imageName}:${tag}"
        
        def buildArgs = dockerfile != 'Dockerfile' ? "-f ${dockerfile}" : ""
        script.sh "docker build ${buildArgs} -t ${imageName}:${tag} ."
        
        script.echo "✅ Docker image built successfully: ${imageName}:${tag}"
    }
    
    /**
     * Push Docker image to registry
     * @param imageName Name of the image
     * @param tag Tag of the image
     * @param registry Registry URL (optional)
     */
    def pushImage(String imageName, String tag, String registry = "") {
        def fullImageName = registry ? "${registry}/${imageName}:${tag}" : "${imageName}:${tag}"
        
        script.echo "🚀 Pushing Docker image: ${fullImageName}"
        
        if (registry) {
            script.sh "docker tag ${imageName}:${tag} ${fullImageName}"
        }
        
        script.sh "docker push ${fullImageName}"
        script.echo "✅ Docker image pushed successfully: ${fullImageName}"
    }
    
    /**
     * Run Docker container for testing
     * @param imageName Name of the image
     * @param tag Tag of the image
     * @param command Command to run (optional)
     */
    def runContainer(String imageName, String tag, String command = "") {
        def containerName = "${imageName}-test-${script.env.BUILD_NUMBER}"
        
        script.echo "🧪 Running Docker container: ${containerName}"
        
        try {
            def runCmd = "docker run --name ${containerName} ${imageName}:${tag}"
            if (command) {
                runCmd += " ${command}"
            }
            
            script.sh runCmd
            script.echo "✅ Container executed successfully"
            
        } finally {
            // Cleanup container
            script.sh "docker rm -f ${containerName} || true"
        }
    }
    
    /**
     * Clean up Docker resources
     */
    def cleanup() {
        script.echo "🧹 Cleaning up Docker resources"
        script.sh """
            docker system prune -f
            docker volume prune -f
        """
    }
}
