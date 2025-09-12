pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'Maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.projectKey=MicroBees \
                          -Dsonar.organization=Qube-01 \
                          -Dsonar.host.url=https://sonarcloud.io \
                          -Dsonar.login=${SONAR_AUTH_TOKEN}
                    """
                }
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
