

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
    DYNATRACEID="${env.DT_ACCOUNTID}.live.dynatrace.com"
    DYNATRACEAPIKEY="${env.DT_API_TOKEN}"
    NLAPIKEY="${env.NL_WEB_API_KEY}"
    OUTPUTSANITYCHECK="$WORKSPACE/infrastructure/sanitycheck.json"
    DYNATRACEPLUGINPATH="$WORKSPACE/lib/DynatraceIntegration-3.0.1-SNAPSHOT.jar"
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


            sh "mvn -B clean package -DdynatraceId=$DYNATRACEID -DneoLoadWebAPIKey=$NLAPIKEY -DdynatraceApiKey=$DYNATRACEAPIKEY -Dtags=${NL_DT_TAG} -DoutPutReferenceFile=$OUTPUTSANITYCHECK -DcustomActionPath=$DYNATRACEPLUGINPATH -DjsonAnomalieDetectionFile=$SHIPPING_ANOMALIEFILE"

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
   /* stage('DT Deploy Event') {
        when {
            expression {
            return env.BRANCH_NAME ==~ 'release/.*' || env.BRANCH_NAME ==~'master'
            }
        }
        steps {
          container("curl") {
            // send custom deployment event to Dynatrace
            sh "curl -X POST \"$DT_TENANT_URL/api/v1/events?Api-Token=$DT_API_TOKEN\" -H \"accept: application/json\" -H \"Content-Type: application/json\" -d \"{ \\\"eventType\\\": \\\"CUSTOM_DEPLOYMENT\\\", \\\"attachRules\\\": { \\\"tagRule\\\" : [{ \\\"meTypes\\\" : [\\\"SERVICE\\\"], \\\"tags\\\" : [ { \\\"context\\\" : \\\"CONTEXTLESS\\\", \\\"key\\\" : \\\"app\\\", \\\"value\\\" : \\\"${env.APP_NAME}\\\" }, { \\\"context\\\" : \\\"CONTEXTLESS\\\", \\\"key\\\" : \\\"environment\\\", \\\"value\\\" : \\\"dev\\\" } ] }] }, \\\"deploymentName\\\":\\\"${env.JOB_NAME}\\\", \\\"deploymentVersion\\\":\\\"${_VERSION}\\\", \\\"deploymentProject\\\":\\\"\\\", \\\"ciBackLink\\\":\\\"${env.BUILD_URL}\\\", \\\"source\\\":\\\"Jenkins\\\", \\\"customProperties\\\": { \\\"Jenkins Build Number\\\": \\\"${env.BUILD_ID}\\\",  \\\"Git commit\\\": \\\"${env.GIT_COMMIT}\\\" } }\" "
          }
        }
    }*/
     stage('Start NeoLoad infrastructure') {

                                steps {
                                           sh "cp -f ${DOCKER_COMPOSE_LG_TEMPLATE_FILE} ${DOCKER_COMPOSE_LG_FILE}"
                                           sh "sed -i 's,TO_REPLACE,${APP_NAME},'  ${DOCKER_COMPOSE_LG_FILE}"
                                           sh "sed -i 's,TOKEN_TOBE_REPLACE,$NLAPIKEY,'  ${DOCKER_COMPOSE_LG_FILE}"
                                           sh 'docker-compose -f ${DOCKER_COMPOSE_LG_FILE} up -d'
                                           sleep 15

                                       }

                           }

    stage('Run functional check in dev') {

      steps {

       sleep 90
       sh "docker run --rm \
                         -v $WORKSPACE/target/neoload/Shipping_NeoLoad/:/neoload-project \
                         -e NEOLOADWEB_TOKEN=$NLAPIKEY \
                         -e TEST_RESULT_NAME=FuncCheck_shipping_${VERSION}_${BUILD_NUMBER} \
                         -e SCENARIO_NAME=Shipping_Load \
                         -e CONTROLLER_ZONE_ID=defaultzone \
                         -e LG_ZONE_IDS=defaultzone:1 \
                         --network ${APP_NAME} \
                          neotys/neoload-web-test-launcher:latest"
          /*script {
              neoloadRun executable: '/home/neoload/neoload/bin/NeoLoadCmd',
                      project: "$WORKSPACE/target/neoload/Shipping_NeoLoad/Shipping_NeoLoad.nlp",
                      testName: 'FuncCheck_shipping_${VERSION}_${BUILD_NUMBER}',
                      testDescription: 'FuncCheck_shipping_${VERSION}_${BUILD_NUMBER}',
                      commandLineOption: "-nlweb -L  Population_ShippingUserPath=$WORKSPACE/infrastructure/infrastructure/neoload/lg/remote.txt -L Population_Dynatrace_Integration=$WORKSPACE/infrastructure/infrastructure/neoload/lg/local.txt -nlwebToken $NLAPIKEY -variables carts_host=shipping,carts_port=80,shippingPath=${SHIPPINGURI}",
                      scenario: 'Shipping_Load', sharedLicense: [server: 'NeoLoad Demo License', duration: 2, vuCount: 200],
                      trendGraphs: [
                              [name: 'Limit test Shipping API Response time', curve: ['ShippingUserPath>Actions>Shipping'], statistic: 'average'],
                              'ErrorRate'
                      ]
          }*/



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
