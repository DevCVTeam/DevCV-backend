pipeline {
    agent any
    environment {
        IMAGE_NAME = 'devcv-dev-zero'
        COMPOSE_FILE = 'docker-compose.yml'
        DOCKER_REGISTRY = 'spg9468/devcv-dev-zero'
        GIT_REPO = 'https://github.com/DevCVTeam/DevCV-backend.git'
        GIT_BRANCH = 'develop'
        DEPLOY_REPO = '/home/ubuntu/backend/deploy'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh "cp /var/jenkins_home/application/DevCV-dev/application.yml /var/jenkins_home/workspace/devcv-dev-zero/src/main/resources/application.yml"
                    sh "./gradlew clean --info build -Pprofile=dev -Djava.net.preferIPv4Stack=true"
                    sh "rm -rf /var/jenkins_home/workspace/devcv-dev-zero/src/main/resources/application.yml"
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sshPublisher(
                        failOnError: true,
                        publishers: [
                            sshPublisherDesc(
                                configName: 'devcv-dev',
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        cleanRemote:false,
                                        sourceFiles: 'build/libs/devcv-0.0.1-SNAPSHOT.jar',
                                        removePrefix: 'build/libs',
                                        remoteDirectory: '/backend/deploy',
                                    ),
                                    sshTransfer(
                                        execCommand: 'sudo /bin/sh /home/ubuntu/backend/deploy/deploy.sh'
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }


        // stage('Validate Deployment') {
        //     steps {
        //         script {
        //             def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://http://ec2-54-87-24-74.compute-1.amazonaws.com:3000", returnStdout: true).trim()
        //             if (response != '200') {
        //                 error "Deployment failed with status ${response}"
        //             }
        //         }
        //     }
        // }

    }

    post {
        always {
            echo "Pipeline finished."
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
