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

        stage('Remove local-maven-repo') {
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

        stage('Build') {
            steps {
                sh 'mvn -s $WORKSPACE/settings.xml -B -V -DskipTests=true clean package'
            }
        }

        stage('Unit Tests') {
            steps {
                withCredentials([string(credentialsId: 'MONGODB_URI', variable: 'MONGODB_URI')]) {
                    sh '''
                        mvn -s $WORKSPACE/settings.xml test jacoco:report \
                        -Dspring.profiles.active=test \
                        -Dtest=!UserInfoControllerAutomationTest
                    '''
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Coverage Check') {
            steps {
                sh '''
                    LINE=$(grep -A 1 "<counter type=\\"INSTRUCTION\\"" target/site/jacoco/jacoco.xml | grep covered)
                    COVERED=$(echo $LINE | sed -n 's/.*covered="\\([0-9]*\\)".*/\\1/p')
                    MISSED=$(echo $LINE | sed -n 's/.*missed="\\([0-9]*\\)".*/\\1/p')
                    TOTAL=$((COVERED + MISSED))
                    PERCENT=$((COVERED * 100 / TOTAL))

                    echo "Coverage: $PERCENT%"

                    if [ $PERCENT -lt 60 ]; then
                      echo "Coverage ($PERCENT%) is below 60% threshold!"
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
                script {
                    timeout(time: 15, unit: 'MINUTES') {
                        def qgStatus = null
                        while (qgStatus == null || qgStatus == 'IN_PROGRESS') {
                            sleep 15
                            try {
                                def qg = waitForQualityGate()
                                qgStatus = qg.status
                                echo "Current Quality Gate status: ${qgStatus}"
                                if (qgStatus != 'IN_PROGRESS' && qgStatus != 'OK') {
                                    error "Quality Gate failed: ${qgStatus}"
                                }
                            } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
                                error "Pipeline aborted due to timeout"
                            }
                        }
                    }
                }
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
            }
        }

        stage('Start Spring Boot') {
            steps {
                withCredentials([string(credentialsId: 'MONGODB_URI', variable: 'MONGODB_URI')]) {
                    sh '''
                      export MONGODB_URI="${MONGODB_URI}"

                      # Optional: check if port 8000 is free before start
                      if lsof -i :8000; then
                        echo "Port 8000 in use, killing existing process."
                        lsof -ti :8000 | xargs kill -9
                        sleep 5
                      fi

                      nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8000" > springboot.log 2>&1 &
                      SPRING_PID=$!

                      echo "Waiting for Spring Boot to start listening on port 8000..."

                      for i in {1..36}; do
                        if curl -fs http://localhost:8000/actuator/health; then
                          echo "Spring Boot is UP!"
                          break
                        fi
                        echo "Waiting... attempt $i"
                        sleep 5
                        tail -n 10 springboot.log
                      done

                      if ! ps -p $SPRING_PID > /dev/null; then
                        echo "Spring Boot process died unexpectedly."
                        cat springboot.log
                        exit 1
                      fi
                    '''
                }
            }
        }

        stage('api tests') {
            steps {
                withCredentials([string(credentialsId: 'MONGODB_URI', variable: 'MONGODB_URI')]) {
                    sh 'mvn -s $WORKSPACE/settings.xml test -Dspring.profiles.active=test -Dtest=UserInfoControllerAutomationTest'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Stop Spring Boot') {
            steps {
                sh '''
                    pid=$(lsof -ti :8000)
                    if [ ! -z "$pid" ]; then
                      kill $pid
                    fi
                '''
            }
        }

        //         stage('User Approval for CF Deployment') {
        //             steps {
        //                 script {
        //                     def userInput = input(
        //                         id: 'Approval', message: 'Approve deployment to CF?', ok: 'Deploy',
        //                         parameters: [
        //                             choice(name: 'Approval', choices: ['Approve', 'Decline'], description: 'Select an option')
        //                         ]
        //                     )
        //
        //                     if (userInput == 'Decline') {
        //                         error "Deployment declined by user."
        //                     } else {
        //                         echo "User approved deployment. Continuing..."
        //                     }
        //                 }
        //             }
        //         }

        stage('Login to CF') {
            steps {
                withCredentials([
                    string(credentialsId: 'CF_ENV', variable: 'CF_ENV'),
                    string(credentialsId: 'CF_USER', variable: 'CF_USER'),
                    string(credentialsId: 'CF_PASSWORD', variable: 'CF_PASSWORD'),
                    string(credentialsId: 'CF_SPACE', variable: 'CF_SPACE')
                ]) {
                    sh '''
                        cf login -a ${CF_ENV} -u ${CF_USER} -p ${CF_PASSWORD} -s ${CF_SPACE}
                    '''
                }
            }
        }

        stage('Undeploy Existing App') {
            steps {
                sh 'cf delete microbees-service -f'
            }
        }

        stage('Deploy to Production') {
            steps {
                withCredentials([string(credentialsId: 'MONGODB_URI', variable: 'MONGODB_URI')]) {
                    sh 'cf push microbees-service --no-start'
                    sh 'cf set-env microbees-service MONGODB_URI ${MONGODB_URI}'
                    sh 'cf start microbees-service'
                }
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

