pipeline {
    agent { label 'android' }

    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
            }
            post {
                always {
                    junit '**/build/test-results/*.xml'
                }
            }
        }

        stage('Deploy snapshot') {
            when {
                branch 'master'
            }
            environment {
                NEXUS = credentials('simple-nexus')
            }
            steps {
                sh './gradlew publishPluginMavenPublicationToSnapshotsRepository -PnexusUsername=$NEXUS_USR -PnexusPassword=$NEXUS_PSW'
            }
        }
    }
}
