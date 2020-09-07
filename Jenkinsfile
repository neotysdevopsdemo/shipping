

pipeline {
    agent  { label 'master' }
    tools {
        maven 'Maven 3.6.0'
        jdk 'jdk8'
    }
  environment {
    VERSION="0.1"
    APP_NAME = "shipping"
    TAG = "neotysdevopsdemo/${APP_NAME}"
    TAG_DEV = "${TAG}:DEV-${VERSION}"
    TAG_STAGING = "${TAG}-stagging:${VERSION}"
    NL_DT_TAG="app:${env.APP_NAME},environment:dev"
    SHIPPING_ANOMALIEFILE="$WORKSPACE/monspec/shipping_anomalieDection.json"
    DYNATRACEID="https://${env.DT_ACCOUNTID}.live.dynatrace.com/"
    DYNATRACEAPIKEY="${env.DT_API_TOKEN}"
    NLAPIKEY="${env.NL_WEB_API_KEY}"
    DOCKER_COMPOSE_TEMPLATE="$WORKSPACE/infrastructure/infrastructure/neoload/docker-compose.template"
    DOCKER_COMPOSE_LG_FILE = "$WORKSPACE/infrastructure/infrastructure/neoload/docker-compose-neoload.yml"
    BASICCHECKURI="/health"
    SHIPPINGURI="/shipping"
    GROUP = "neotysdevopsdemo"
    COMMIT = "DEV-${VERSION}"
  }
  stages {
      stage('Checkout') {
          agent { label 'master' }
          steps {
              git  url:"https://github.com/${GROUP}/${APP_NAME}.git",
                      branch :'master'
          }
      }
    stage('Maven build') {
      steps {
        // checkout scm


            sh "mvn -B clean package -DdynatraceURL=$DYNATRACEID -DneoLoadWebAPIKey=$NLAPIKEY -DdynatraceApiKey=$DYNATRACEAPIKEY -DdynatraceTags==${NL_DT_TAG} -DjsonAnomalieDetectionFile=$SHIPPING_ANOMALIEFILE"

      }
    }
    stage('Docker build') {

        steps {
            withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
                sh "cp ./target/*.jar ./docker/${APP_NAME}"
                sh "docker build --build-arg BUILD_VERSION=${VERSION} --build-arg COMMIT=$COMMIT -t ${TAG_DEV} $WORKSPACE/docker/${APP_NAME}/"
                sh "docker login --username=${USER} --password=${TOKEN}"
                sh "docker push ${TAG_DEV}"
            }

        }
    }
     stage('create docker netwrok') {

                                      steps {
                                           sh "docker network create ${APP_NAME} || true"

                                      }
                       }
    stage('Deploy to dev namespace') {

        steps {
            sh "sed -i 's,TAG_TO_REPLACE,${TAG_DEV},' $WORKSPACE/docker-compose.yml"
            sh "sed -i 's,TO_REPLACE,${APP_NAME},' $WORKSPACE/docker-compose.yml"
            sh "docker-compose -f $WORKSPACE/docker-compose.yml up -d"
        }
    }

     stage('Start NeoLoad infrastructure') {

                                steps {
                                           sh "cp -f ${DOCKER_COMPOSE_TEMPLATE} ${DOCKER_COMPOSE_LG_FILE}"
                                           sh "sed -i 's,TO_REPLACE,${APP_NAME},'  ${DOCKER_COMPOSE_LG_FILE}"
                                           sh "sed -i 's,TOKEN_TOBE_REPLACE,$NLAPIKEY,'  ${DOCKER_COMPOSE_LG_FILE}"
                                           sh 'docker-compose -f ${DOCKER_COMPOSE_LG_FILE} up -d'
                                           sleep 15

                                       }

                           }
     stage('NeoLoad Test')
        {
         agent {
         docker {
             image 'python:3-alpine'
             reuseNode true
          }

            }
        stages {
             stage('Get NeoLoad CLI') {
                          steps {
                            withEnv(["HOME=${env.WORKSPACE}"]) {

                             sh '''
                                  export PATH=~/.local/bin:$PATH
                                  pip3 install neoload
                                  neoload --version
                              '''

                            }
                          }
             }
            stage('Run functional check in dev') {

              steps {
                 withEnv(["HOME=${env.WORKSPACE}"]) {
                   sleep 90


                  sh """
                             export PATH=~/.local/bin:$PATH
                             neoload \
                             login --workspace "Default Workspace" $NLAPIKEY \
                             test-settings  --zone defaultzone --scenario Shipping_Load  use ShippingDynatrace \
                             project --path  $WORKSPACE/target/neoload/Shipping_NeoLoad/ upload
                    """
                }

              }
            }
            stage('Run Test') {
                      steps {
                        withEnv(["HOME=${env.WORKSPACE}"]) {
                          sh """
                               export PATH=~/.local/bin:$PATH
                               neoload run \
                              --return-0 \
                                ShippingDynatrace
                             """
                        }
                      }
             }

        }
    }
    stage('Mark artifact for staging namespace') {

        steps {

            withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
                sh "docker login --username=${USER} --password=${TOKEN}"
                sh "docker tag ${TAG_DEV} ${TAG_STAGING}"
                sh "docker push ${TAG_STAGING}"
            }

        }
    }

  }
  post {
      always {


          sh 'docker-compose -f $WORKSPACE/infrastructure/infrastructure/neoload/lg/docker-compose.yml down'
          sh 'docker-compose -f $WORKSPACE/docker-compose.yml down'
           cleanWs()
          sh 'docker volume prune'
      }

      }
}
