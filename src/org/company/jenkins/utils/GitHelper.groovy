// ===== src/org/company/jenkins/utils/GitHelper.groovy =====
package org.company.jenkins.utils

/**
 * Git utility class for version control operations
 */
class GitHelper {
    def script
    
    GitHelper(script) {
        this.script = script
    }
    
    /**
     * Get commit information
     */
    def getCommitInfo() {
        def commitHash = script.sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
        def shortHash = script.sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
        def author = script.sh(returnStdout: true, script: 'git log -1 --pretty=format:"%an"').trim()
        def message = script.sh(returnStdout: true, script: 'git log -1 --pretty=format:"%s"').trim()
        def timestamp = script.sh(returnStdout: true, script: 'git log -1 --pretty=format:"%ci"').trim()
        
        return [
            hash: commitHash,
            shortHash: shortHash,
            author: author,
            message: message,
            timestamp: timestamp
        ]
    }
    
    /**
     * Check if working directory is clean
     */
    def isWorkingDirectoryClean() {
        def status = script.sh(returnStdout: true, script: 'git status --porcelain').trim()
        return status.isEmpty()
    }
    
    /**
     * Get current branch name
     */
    def getCurrentBranch() {
        return script.sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
    }
    
    /**
     * Create and push tag
     * @param tagName Name of the tag
     * @param message Tag message
     */
    def createTag(String tagName, String message = "") {
        script.echo "🏷️ Creating Git tag: ${tagName}"
        
        def tagCmd = "git tag -a ${tagName}"
        if (message) {
            tagCmd += " -m '${message}'"
        } else {
            tagCmd += " -m 'Release ${tagName}'"
        }
        
        script.sh tagCmd
        script.sh "git push origin ${tagName}"
        
        script.echo "✅ Tag created and pushed: ${tagName}"
    }
}
