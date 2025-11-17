pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['develop', 'qa', 'release.s.2025.10'],
            description: 'Ambiente a usar (solo informativo)'
        )
        string(name: 'BRANCH', defaultValue: 'develop', description: 'Rama a construir')
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Tag de la imagen Docker')
        string(name: 'DOCKER_REGISTRY_HOST', defaultValue: '', description: 'Registry para push (vacío = no push)')
    }

    environment {
        MVN_CMD = "/usr/bin/mvn"
        IMAGE_NAME = "gateway-service"
        IMAGE_TAG = "${ENVIRONMENT}-${BUILD_NUMBER}" // Tag dinámico
        FULL_IMAGE = "${DOCKER_REGISTRY_HOST ? DOCKER_REGISTRY_HOST + '/' : ''}${IMAGE_NAME}:${IMAGE_TAG}"
        ENV_DEPLOY_FILE = ".env.deploy"
        DOCKER_REGISTRY_CRED = "docker-registry-creds"
        DEPLOY_DIR = "/apps/deploy"
        CONTAINER_NAME = "gateway-service"
    }

    options { timestamps() }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${BRANCH}",
                    url: 'https://github.com/JuanPerez012/university-academic-gateway.git',
                    credentialsId: 'github-https'
            }
        }

        stage('Copy .env') {
            steps {
                sh """
                    mkdir -p ${DEPLOY_DIR}
                    cp /apps/config/gateway/.env ${DEPLOY_DIR}/${ENV_DEPLOY_FILE}
                    echo ".env.deploy copiado desde volumen correctamente"
                """
            }
        }

        stage('Maven Package') {
            tools { maven 'Maven3.9' }
            steps {
                sh "mvn clean package -DskipTests -DskipITs"
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${FULL_IMAGE} ."
            }
        }

        stage('Docker Push') {
            when { expression { return DOCKER_REGISTRY_HOST?.trim() } }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_REGISTRY_CRED,
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo \$DOCKER_PASS | docker login ${DOCKER_REGISTRY_HOST} --username \$DOCKER_USER --password-stdin
                        docker push ${FULL_IMAGE}
                        docker logout ${DOCKER_REGISTRY_HOST}
                    """
                }
            }
        }

        stage('Update Deploy Env') {
            steps {
                sh """
                    # Actualiza IMAGE_TAG en el .env.deploy para que docker-compose use la última imagen
                    sed -i '/^IMAGE_TAG=/d' ${DEPLOY_DIR}/${ENV_DEPLOY_FILE}
                    echo "IMAGE_TAG=${IMAGE_TAG}" >> ${DEPLOY_DIR}/${ENV_DEPLOY_FILE}
                    echo "IMAGE_TAG actualizado en ${ENV_DEPLOY_FILE} con la última imagen: ${IMAGE_TAG}"
                """
            }
        }

        stage('Deploy with Compose') {
            steps {
                sh """
                    cd ${DEPLOY_DIR}
                    docker-compose down || true
                    docker-compose up -d
                """
            }
        }
    }

    post {
        success { echo "Pipeline completo. Imagen generada: ${FULL_IMAGE}" }
        failure { echo "Pipeline falló." }
        always { cleanWs() }
    }
}