pipeline {
    agent any

    tools {
        jdk 'sapmachine-17'
        maven 'Maven3'
    }

    options {
        ansiColor('xterm')
        timestamps()
        buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '10'))
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Remove local-maven-repo (if present)') {
            steps {
                sh '''
                    if [ -d "${WORKSPACE}/local-maven-repo" ]; then
                      echo "Removing workspace/local-maven-repo to force artifactory usage..."
                      rm -rf "${WORKSPACE}/local-maven-repo"
                    else
                      echo "No local-maven-repo found."
                    fi
                '''
            }
        }

        stage('Prepare Maven settings') {
            steps {
                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'cp "$MAVEN_SETTINGS" "$WORKSPACE/settings.xml"'
                    sh 'ls -la $WORKSPACE/settings.xml || true'
                }
            }
        }

        stage('Build (compile & package)') {
            steps {
                sh 'mvn -s $WORKSPACE/settings.xml -B -V -DskipTests=true clean package'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn -s $WORKSPACE/settings.xml -B test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_AUTH_TOKEN', variable: 'SONAR_AUTH_TOKEN')]) {
                    withSonarQubeEnv('SonarCloud') {
                        sh '''
                            mvn -s $WORKSPACE/settings.xml -B \
                              org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                              -Dsonar.login=${SONAR_AUTH_TOKEN} \
                              -Dsonar.host.url=https://sonarcloud.io \
                              -Dsonar.organization=qube-01 \
                              -Dsonar.projectKey=Qube-01_MicroBees
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
            }
        }

        stage('Deploy (to Artifactory)') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn -s $WORKSPACE/settings.xml -B -DskipTests=true deploy'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo "Build failed."
        }
    }
}
