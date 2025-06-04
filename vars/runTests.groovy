// ===== vars/runTests.groovy =====
/**
 * Run tests for the application
 * @param config Map containing test configuration
 *   - testType: Type of tests (unit, integration, e2e)
 *   - buildTool: Build tool (maven, gradle, npm)
 *   - testReports: Generate test reports (default: true)
 */
def call(Map config) {
    def testStage = new org.company.jenkins.pipeline.TestStage(this)
    
    config.testType = config.testType ?: 'unit'
    config.buildTool = config.buildTool ?: 'maven'
    config.testReports = config.testReports ?: true
    
    echo "Running ${config.testType} tests using ${config.buildTool}"
    
    try {
        testStage.execute(config)
        echo "✅ Tests completed successfully"
        
    } catch (Exception e) {
        echo "❌ Tests failed: ${e.getMessage()}"
        throw e
    }
}
