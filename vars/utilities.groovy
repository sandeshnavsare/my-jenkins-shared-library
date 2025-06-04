// ===== vars/utilities.groovy =====
/**
 * Utility functions for common pipeline operations
 */

/**
 * Get Git commit information
 */
def getGitInfo() {
    def gitHelper = new org.company.jenkins.utils.GitHelper(this)
    return gitHelper.getCommitInfo()
}

/**
 * Check if branch is main/master
 */
def isMainBranch() {
    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH
    return branch in ['main', 'master', 'origin/main', 'origin/master']
}

/**
 * Generate build version
 */
def generateVersion() {
    def timestamp = new Date().format('yyyyMMdd-HHmmss')
    def shortCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    return "${env.BUILD_NUMBER}-${timestamp}-${shortCommit}"
}

/**
 * Archive artifacts with pattern
 */
def archiveArtifacts(String pattern) {
    if (fileExists(pattern)) {
        archiveArtifacts artifacts: pattern, fingerprint: true
        echo "📦 Archived artifacts: ${pattern}"
    } else {
        echo "⚠️ No artifacts found matching pattern: ${pattern}"
    }
}
