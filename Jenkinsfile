pipeline {
    agent { label 'android' }

    environment {
        ANDROID_HOME = '/home/services/.android/sdk'
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
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
                sh '''
                  ./gradlew -PnexusUsername=$NEXUS_USR -PnexusPassword=$NEXUS_PSW \
                      publishPluginMavenPublicationToSnapshotsRepository
                '''.stripIndent()
            }
        }
    }
}
