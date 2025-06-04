// ===== src/org/company/jenkins/config/Constants.groovy =====
package org.company.jenkins.config

/**
 * Constants class for shared library
 */
class Constants {
    
    // Build tools
    static final String MAVEN = 'maven'
    static final String GRADLE = 'gradle'
    static final String NPM = 'npm'
    
    // Environments
    static final String DEV = 'dev'
    static final String STAGING = 'staging'
    static final String PROD = 'prod'
    
    // Docker registries
    static final String DOCKER_HUB = 'docker.io'
    static final String AWS_ECR = 'aws_account_id.dkr.ecr.region.amazonaws.com'
    static final String AZURE_ACR = 'myregistry.azurecr.io'
    
    // Notification channels
    static final String SLACK = 'slack'
    static final String EMAIL = 'email'
    static final String TEAMS = 'teams'
    
    // Test types
    static final String UNIT_TESTS = 'unit'
    static final String INTEGRATION_TESTS = 'integration'
    static final String E2E_TESTS = 'e2e'
    
    // Pipeline statuses
    static final String SUCCESS = 'success'
    static final String FAILURE = 'failure'
    static final String UNSTABLE = 'unstable'
    static final String STARTED = 'started'
    
    // Timeouts (in minutes)
    static final Integer BUILD_TIMEOUT = 30
    static final Integer TEST_TIMEOUT = 45
    static final Integer DEPLOY_TIMEOUT = 15
    
    // Retry counts
    static final Integer DEFAULT_RETRY_COUNT = 3
    static final Integer DEPLOY_RETRY_COUNT = 2
}
