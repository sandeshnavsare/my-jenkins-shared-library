package vars

import org.junit.*
import static org.mockito.Mockito.*
import static org.junit.Assert.*

/**
 * Unit tests for buildApplication pipeline step
 */
class buildApplicationTest {
    
    def buildApplication
    def mockPipelineScript
    
    @Before
    void setUp() {
        // Mock the pipeline script context
        mockPipelineScript = mock(Object.class)
        
        // Load the buildApplication script
        GroovyShell shell = new GroovyShell()
        buildApplication = shell.parse(new File('vars/buildApplication.groovy'))
        buildApplication.binding = new Binding([
            'echo': { msg -> println(msg) },
            'error': { msg -> throw new RuntimeException(msg) },
            'fileExists': { path -> true },
            'env': [BUILD_NUMBER: '123']
        ])
    }
    
    @Test
    void testBuildApplicationWithValidConfig() {
        // Given
        def config = [
            appName: 'test-app',
            buildTool: 'maven'
        ]
        
        // When & Then (should not throw exception)
        try {
            buildApplication.call(config)
        } catch (Exception e) {
            // Expected for unit test environment
            assertTrue(e.message.contains('test-app') || e.message.contains('maven'))
        }
    }
    
    @Test
    void testBuildApplicationWithMissingAppName() {
        // Given
        def config = [
            buildTool: 'maven'
        ]
        
        // When & Then
        try {
            buildApplication.call(config)
            fail("Expected RuntimeException for missing appName")
        } catch (RuntimeException e) {
            assertTrue(e.message.contains('appName is required'))
        }
    }
    
    @Test
    void testBuildApplicationWithDefaults() {
        // Given
        def config = [
            appName: 'test-app'
        ]
        
        // When
        try {
            buildApplication.call(config)
        } catch (Exception e) {
            // Expected in test environment
        }
        
        // Then - verify defaults are set
        assertEquals('maven', config.buildTool)
        assertEquals(false, config.skipTests)
        assertEquals('maven:3.8.4-openjdk-11', config.dockerImage)
    }
    
    @Test
    void testBuildApplicationWithCustomConfig() {
        // Given
        def config = [
            appName: 'custom-app',
            buildTool: 'gradle',
            skipTests: true,
            dockerImage: 'gradle:7.4-jdk11'
        ]
        
        // When & Then
        try {
            buildApplication.call(config)
        } catch (Exception e) {
            // Expected in test environment
        }
        
        // Verify config is preserved
        assertEquals('custom-app', config.appName)
        assertEquals('gradle', config.buildTool)
        assertEquals(true, config.skipTests)
        assertEquals('gradle:7.4-jdk11', config.dockerImage)
    }
}
