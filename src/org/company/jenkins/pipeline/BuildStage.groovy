// ===== src/org/company/jenkins/pipeline/BuildStage.groovy =====
package org.company.jenkins.pipeline

/**
 * Build stage implementation
 */
class BuildStage {
    def script
    
    BuildStage(script) {
        this.script = script
    }
    
    /**
     * Execute build stage
     * @param config Build configuration
     */
    def execute(Map config) {
        script.echo "🔨 Executing build stage for ${config.appName}"
        
        // Checkout code
        script.checkout script.scm
        
        // Execute build based on build tool
        switch (config.buildTool.toLowerCase()) {
            case 'maven':
                executeMavenBuild(config)
                break
            case 'gradle':
                executeGradleBuild(config)
                break
            case 'npm':
                executeNpmBuild(config)
                break
            default:
                script.error "Unsupported build tool: ${config.buildTool}"
        }
        
        // Archive artifacts
        archiveBuildArtifacts(config)
    }
    
    /**
     * Execute Maven build
     */
    private def executeMavenBuild(Map config) {
        script.echo "📦 Building with Maven"
        
        def mvnGoals = config.skipTests ? 'clean package -DskipTests' : 'clean package'
        
        script.sh """
            mvn --version
            mvn ${mvnGoals}
        """
        
        if (!config.skipTests) {
            script.junit testsPattern: 'target/surefire-reports/*.xml'
        }
    }
    
    /**
     * Execute Gradle build
     */
    private def executeGradleBuild(Map config) {
        script.echo "🐘 Building with Gradle"
        
        def gradleTasks = config.skipTests ? 'clean build -x test' : 'clean build'
        
        script.sh """
            ./gradlew --version
            ./gradlew ${gradleTasks}
        """
        
        if (!config.skipTests) {
            script.publishTestResults testsPattern: 'build/test-results/test/*.xml'
        }
    }
    
    /**
     * Execute NPM build
     */
    private def executeNpmBuild(Map config) {
        script.echo "📦 Building with NPM"
        
        script.sh """
            npm --version
            npm ci
            npm run build
        """
        
        if (!config.skipTests) {
            script.sh "npm test"
        }
    }
    
    /**
     * Archive build artifacts
     */
    private def archiveBuildArtifacts(Map config) {
        script.echo "📦 Archiving build artifacts"
        
        switch (config.buildTool.toLowerCase()) {
            case 'maven':
                script.archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                break
            case 'gradle':
                script.archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                break
            case 'npm':
                script.archiveArtifacts artifacts: 'dist/**/*', fingerprint: true
                break
        }
    }
}
