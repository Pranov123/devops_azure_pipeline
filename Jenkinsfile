// CI/CD pipeline: Git -> Maven build & test -> Docker image -> Azure Container Registry
//                 -> Ansible deploy to Kubernetes -> verify
// Runs on a Windows Jenkins controller; Ansible runs inside WSL (Ubuntu).
pipeline {
    agent any

    triggers {
        // Jenkins runs locally, so GitHub cannot reach it with a webhook: poll the repo every minute instead
        pollSCM('* * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        REGISTRY    = 'pranovdevopsacr.azurecr.io'
        IMAGE_NAME  = 'devops-azure-pipeline'
        K8S_CONTEXT = 'minikube'
        K8S_NS      = 'devops-cia2'
        APP_NAME    = 'devops-app'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = bat(returnStdout: true, script: '@git rev-parse --short HEAD').trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_SHORT}"
                    env.IMAGE     = "${env.REGISTRY}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                }
                echo "Building commit ${env.GIT_SHORT} as image ${env.IMAGE}"
            }
        }

        stage('Maven Build') {
            steps {
                bat 'mvn -B -DskipTests clean package'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t %IMAGE% -t %REGISTRY%/%IMAGE_NAME%:latest ."
            }
        }

        stage('Push to ACR') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'acr-credentials',
                                                  usernameVariable: 'ACR_USER',
                                                  passwordVariable: 'ACR_PASS')]) {
                    bat '''
                        echo %ACR_PASS%| docker login %REGISTRY% -u %ACR_USER% --password-stdin
                        docker push %IMAGE%
                        docker push %REGISTRY%/%IMAGE_NAME%:latest
                        docker logout %REGISTRY%
                    '''
                }
            }
        }

        stage('Deploy with Ansible') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'acr-credentials',
                                                  usernameVariable: 'ACR_USER',
                                                  passwordVariable: 'ACR_PASS')]) {
                    // Self-contained kubeconfig for the target cluster, handed to Ansible in WSL.
                    // WSLENV forwards these variables into WSL (/p converts the Windows path).
                    bat '''
                        kubectl config view --flatten --minify --context %K8S_CONTEXT% > "%WORKSPACE%\\.kubeconfig"
                        set K8S_AUTH_KUBECONFIG=%WORKSPACE%\\.kubeconfig
                        set WSLENV=ACR_USER:ACR_PASS:K8S_AUTH_KUBECONFIG/p
                        wsl -d Ubuntu --cd "%WORKSPACE%\\ansible" -- ansible-playbook -i inventory.ini deploy.yml -e image_tag=%IMAGE_TAG% -e build_number=%BUILD_NUMBER%
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                bat '''
                    kubectl --context %K8S_CONTEXT% -n %K8S_NS% rollout status deployment/%APP_NAME% --timeout=120s
                    kubectl --context %K8S_CONTEXT% -n %K8S_NS% get deployment,pods,service -o wide
                    kubectl --context %K8S_CONTEXT% -n %K8S_NS% get deployment %APP_NAME% -o jsonpath="{.spec.template.spec.containers[0].image}"
                '''
            }
        }
    }

    post {
        always {
            bat 'if exist "%WORKSPACE%\\.kubeconfig" del /q "%WORKSPACE%\\.kubeconfig"'
        }
        success {
            echo "Deployed ${env.IMAGE} to ${env.K8S_CONTEXT} (namespace ${env.K8S_NS})"
        }
        failure {
            echo 'Pipeline failed - check the stage logs above'
        }
    }
}
