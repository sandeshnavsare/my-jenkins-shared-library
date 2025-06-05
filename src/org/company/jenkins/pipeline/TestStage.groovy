// ===== src/org/company/jenkins/pipeline/TestStage.groovy =====
package org.company.jenkins.pipeline

/**
 * Test stage implementation
 */
class TestStage {
    def script
    
    TestStage(script) {
        this.script = script
    }
    
    /**
     * Execute test stage
     * @param config Test configuration
     */
    def execute(Map config) {
        script.echo "🧪 Executing ${config.testType} tests"
        
        try {
            switch (config.testType.toLowerCase()) {
                case 'unit':
                    runUnitTests(config)
                    break
                case 'integration':
                    runIntegrationTests(config)
                    break
                case 'e2e':
                    runE2ETests(config)
                    break
                default:
                    script.error "Unsupported test type: ${config.testType}"
            }
            
            if (config.testReports) {
                publishTestReports(config)
            }
            
        } catch (Exception e) {
            script.echo "❌ Tests failed: ${e.getMessage()}"
            throw e
        }
    }
    
    /**
     * Run unit tests
     */
    private def runUnitTests(Map config) {
        script.echo "🔬 Running unit tests"
        
        switch (config.buildTool.toLowerCase()) {
            case 'maven':
                script.sh "mvn test"
                break
            case 'gradle':
                script.sh "./gradlew test"
                break
            case 'npm':
                script.sh "npm test"
                break
        }
    }
    
    /**
     * Run integration tests
     */
    private def runIntegrationTests(Map config) {
        script.echo "🔗 Running integration tests"
        
        switch (config.buildTool.toLowerCase()) {
            case 'maven':
                script.sh "mvn verify -P integration-tests"
                break
            case 'gradle':
                script.sh "./gradlew integrationTest"
                break
            case 'npm':
                script.sh "npm run test:integration"
                break
        }
    }
    
    /**
     * Run E2E tests
     */
    private def runE2ETests(Map config) {
        script.echo "🌐 Running E2E tests"
        
        switch (config.buildTool.toLowerCase()) {
            case 'maven':
                script.sh "mvn verify -P e2e-tests"
                break
            case 'gradle':
                script.sh "./gradlew e2eTest"
                break
            case 'npm':
                script.sh "npm run test:e2e"
                break
        }
    }
    
    /**
     * Publish test reports
     */
   private def publishTestReports(Map config) {
    script.echo "📊 Publishing test reports"

    switch (config.buildTool.toLowerCase()) {
        case 'maven':
            if (script.fileExists('target/surefire-reports')) {
                script.junit 'target/surefire-reports/*.xml'
                script.publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'Coverage Report'
                ])
            } else {
                script.echo "⚠️ No surefire test reports found to publish."
            }
            break

        case 'gradle':
            if (script.fileExists('build/test-results/test')) {
                script.junit 'build/test-results/test/*.xml'
                script.publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: 'build/reports/jacoco/test/html',
                    reportFiles: 'index.html',
                    reportName: 'Coverage Report'
                ])
            } else {
                script.echo "⚠️ No gradle test reports found to publish."
            }
            break

        case 'npm':
            if (script.fileExists('test-results.xml')) {
                script.junit 'test-results.xml'
            } else {
                script.echo "⚠️ No npm test results found to publish."
            }
            break
    }
}

}
