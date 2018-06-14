pipeline {
    agent { label 'android' }

    environment {
        ANDROID_HOME = '/home/services/.android/sdk'
        NEXUS = credentials('simple-nexus')
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
            steps {
                sh '''
                  ./gradlew -PnexusUsername=$NEXUS_USR -PnexusPassword=$NEXUS_PSW \
                      publishPluginMavenPublicationToSnapshotsRepository
                '''.stripIndent()
            }
        }

        stage('Deploy release') {
            when {
                buildingTag()
            }
            steps {
                sh '''
                  ./gradlew -PnexusUsername=$NEXUS_USR -PnexusPassword=$NEXUS_PSW \
                      publishPluginMavenPublicationToReleasesRepository
                '''.stripIndent()
            }
        }
    }
}
