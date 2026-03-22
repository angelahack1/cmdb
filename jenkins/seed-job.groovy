// jenkins/seed-job.groovy
// ────────────────────────────────────────────────────────────
// Job DSL script – run this from a Jenkins "seed" job
// to auto-create the CMDB pipeline.
//
// Prerequisites:
//   - "Job DSL" plugin installed
//   - A seed job configured to execute this Groovy script
// ────────────────────────────────────────────────────────────

pipelineJob('cmdb-pipeline') {
    displayName('CMDB – CI/CD Pipeline')
    description('Full build, test, OWASP scan, and Ansible deployment pipeline for the CMDB application.')

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/xaiht/cmdb.git')      // ← Update with your repo URL
                        credentials('github-credentials')               // ← Update credential ID
                    }
                    branches('*/main')
                }
            }
            scriptPath('jenkins/Jenkinsfile')
        }
    }

    parameters {
        choiceParam('TARGET_ENV', ['dev', 'staging', 'prod'], 'Target environment for deployment')
        booleanParam('SKIP_OWASP', false, 'Skip OWASP dependency check')
        booleanParam('RUN_ANSIBLE_PROVISION', false, 'Run full infrastructure provisioning')
    }

    properties {
        disableConcurrentBuilds()
        buildDiscarder {
            strategy {
                logRotator {
                    numToKeepStr('20')
                    artifactNumToKeepStr('5')
                }
            }
        }
    }

    triggers {
        // Poll SCM every 5 minutes
        scm('H/5 * * * *')
    }
}
