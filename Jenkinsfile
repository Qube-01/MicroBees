pipeline {
    agent any

    tools {
        jdk 'jdk17'
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
                sh 'mvn -s $WORKSPACE/settings.xml -B test jacoco:report'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Enforce Coverage') {
            steps {
                sh '''
                    LINE=$(grep -A 1 "<counter type=\\"INSTRUCTION\\"" target/site/jacoco/jacoco.xml | grep covered)
                    COVERED=$(echo $LINE | sed -n 's/.*covered="\\([0-9]*\\)".*/\\1/p')
                    MISSED=$(echo $LINE | sed -n 's/.*missed="\\([0-9]*\\)".*/\\1/p')
                    TOTAL=$((COVERED + MISSED))
                    PERCENT=$((COVERED * 100 / TOTAL))

                    echo "Coverage: $PERCENT%"

                    if [ $PERCENT -lt 60 ]; then
                      echo "Coverage ($PERCENT%) is below 80% threshold!"
                      exit 1
                    fi
                '''
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

        stage('User Approval for CF Deployment') {
            steps {
                script {
                    def userInput = input(
                        id: 'Approval', message: 'Approve deployment to CF?', ok: 'Deploy',
                        parameters: [
                            choice(name: 'Approval', choices: ['Approve', 'Decline'], description: 'Select an option')
                        ]
                    )

                    if (userInput == 'Decline') {
                        error "Deployment declined by user."
                    } else {
                        echo "User approved deployment. Continuing..."
                    }
                }
            }
        }

        stage('Login to CF') {
            steps {
                sh 'cf login -a ${CF_ENV} -u ${CF_USER} -p ${CF_PASSWORD} -s ${CF_SPACE}'
            }
        }

        stage('Undeploy Existing App') {
            steps {
                sh 'cf delete microbees-service -f'
            }
        }

        stage('Deploy to CF Environment') {
            steps {
                sh 'cf push'
            }
        }

        stage('Create Application Route') {
            steps {
                sh 'cf map-route microbees-service de.a9sapp.eu --hostname microbees-service-live'
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
