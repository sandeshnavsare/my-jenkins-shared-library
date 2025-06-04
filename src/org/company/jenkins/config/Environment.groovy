// ===== src/org/company/jenkins/config/Environment.groovy =====
package org.company.jenkins.config

/**
 * Environment configuration class
 */
class Environment {
    
    /**
     * Get environment-specific configuration
     * @param environment Environment name
     * @return Map of environment configuration
     */
    def getEnvironmentConfig(String environment) {
        def configs = [
            'dev': [
                namespace: 'development',
                replicas: 1,
                resources: [
                    requests: [cpu: '100m', memory: '128Mi'],
                    limits: [cpu: '500m', memory: '512Mi']
                ],
                ingress: [
                    host: 'app.dev.company.com',
                    tls: false
                ]
            ],
            'staging': [
                namespace: 'staging',
                replicas: 2,
                resources: [
                    requests: [cpu: '200m', memory: '256Mi'],
                    limits: [cpu: '1000m', memory: '1Gi']
                ],
                ingress: [
                    host: 'app.staging.company.com',
                    tls: true
                ]
            ],
            'prod': [
                namespace: 'production',
                replicas: 3,
                resources: [
                    requests: [cpu: '500m', memory: '512Mi'],
                    limits: [cpu: '2000m', memory: '2Gi']
                ],
                ingress: [
                    host: 'app.company.com',
                    tls: true
                ]
            ]
        ]
        
        return configs[environment] ?: [:]
    }
    
    /**
     * Get database configuration for environment
     * @param environment Environment name
     * @return Database configuration
     */
    def getDatabaseConfig(String environment) {
        def configs = [
            'dev': [
                host: 'dev-db.company.com',
                port: 5432,
                database: 'app_dev',
                ssl: false
            ],
            'staging': [
                host: 'staging-db.company.com',
                port: 5432,
                database: 'app_staging',
                ssl: true
            ],
            'prod': [
                host: 'prod-db.company.com',
                port: 5432,
                database: 'app_prod',
                ssl: true
            ]
        ]
        
        return configs[environment] ?: [:]
    }
}
