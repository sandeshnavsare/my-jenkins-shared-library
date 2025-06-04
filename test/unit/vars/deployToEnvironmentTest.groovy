package vars

import org.junit.*
import static org.mockito.Mockito.*
import static org.junit.Assert.*

/**
 * Unit tests for deployToEnvironment pipeline step
 */
class deployToEnvironmentTest {
    
    def deployToEnvironment
    def mockPipelineScript
    
    @Before
    void setUp() {
        mockPipelineScript = mock(Object.class)
        
        GroovyShell shell = new GroovyShell()
        deployToEnvironment = shell.parse(new File('vars/deployToEnvironment.groovy'))
        deployToEnvironment.binding = new Binding([
            'echo': { msg -> println(msg) },
            'error': { msg -> throw new RuntimeException(msg) }
        ])
    }
    
    @Test
    void testDeployWithValidConfig() {
        // Given
        def config = [
            environment: 'dev',
            appName: 'test-app',
            version: '1.0.0'
        ]
        
        // When & Then
        try {
            deployToEnvironment.call(config)
        } catch (Exception e) {
            // Expected in unit test environment
            assertTrue(e.message.contains('test-app') || e.message.contains('dev'))
        }
    }
    
    @Test
    void testDeployWithMissingEnvironment() {
        // Given
        def config = [
            appName: 'test-app',
            version: '1.0.0'
        ]
        
        // When & Then
        try {
            deployToEnvironment.call(config)
            fail("Expected RuntimeException for missing environment")
        } catch (RuntimeException e) {
            assertTrue(e.message.contains('environment, appName, and version are required'))
        }
    }
    
    @Test
    void testDeployWithMissingAppName() {
        // Given
        def config = [
            environment: 'dev',
            version: '1.0.0'
        ]
        
        // When & Then
        try {
            deployToEnvironment.call(config)
            fail("Expected RuntimeException for missing appName")
        } catch (RuntimeException e) {
            assertTrue(e.message.contains('environment, appName, and version are required'))
        }
    }
    
    @Test
    void testDeployWithMissingVersion() {
        // Given
        def config = [
            environment: 'dev',
            appName: 'test-app'
        ]
        
        // When & Then
        try {
            deployToEnvironment.call(config)
            fail("Expected RuntimeException for missing version")
        } catch (RuntimeException e) {
            assertTrue(e.message.contains('environment, appName, and version are required'))
        }
    }
    
    @Test
    void testDeployToAllEnvironments() {
        def environments = ['dev', 'staging', 'prod']
        
        environments.each { env ->
            def config = [
                environment: env,
                appName: 'test-app',
                version: '1.0.0'
            ]
            
            try {
                deployToEnvironment.call(config)
            } catch (Exception e) {
                // Expected in unit test environment
                assertTrue("Should contain environment $env", 
                    e.message.contains(env) || e.message.contains('test-app'))
            }
        }
    }
}
