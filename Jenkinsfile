pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'Maven3'
    }

    environment {
        SONAR_AUTH_TOKEN = credentials('SONAR_AUTH_TOKEN')
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
                        mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                            -Dsonar.token=${SONAR_AUTH_TOKEN} \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.organization=qube-01 \
                            -Dsonar.projectKey=Qube-01_MicroBees
                    """
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
    }
}
