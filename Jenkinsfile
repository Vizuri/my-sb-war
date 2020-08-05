//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
import groovy.json.JsonSlurper
def mvnCmd = "mvn"
def version="1.0"
def app_name="my-sb-war"
def dev_project="dev-my-sb-war"
def test_project="test-my-sb-war"
def quay_host="docker.io"
def quay_org="vizuri"


def getDockerImages() {
    //final API_KEY = "FOOBARAPIKEY"
   // final REPO_NAME = "service-docker"
  //  final APP_NAME = "myapp"

  //  def cmd = [ 'bash', '-c', "curl -H 'X-JFrog-Art-Api: ${API_KEY}' https://artifactory.acme.co/artifactory/api/docker/${REPO_NAME}/v2/${APP_NAME}/tags/list".toString()]
 //   def result = cmd.execute().text

 //   def slurper = new JsonSlurper()
 //   def json = slurper.parseText(result)
    def tags = new ArrayList()
 //   if (json.tags == null || json.tags.size == 0)
//      tags.add("unable to fetch tags for ${APP_NAME}")
//    else
//      tags.addAll(json.tags)
    tags.add("1.0")
    tags.add("1.1");
    return tags.join('\n')
}
pipeline {

  agent {
    label 'maven-buildah'
  }
  stages {
  
      stage("Gather Deployment Parameters") {
        steps {
            timeout(time: 30, unit: 'SECONDS') {
                script {
                    // Show the select input modal
                   def INPUT_PARAMS = input message: 'Please Provide Parameters', ok: 'Next',
                                    parameters: [
                                    choice(name: 'ENVIRONMENT', choices: ['dev','qa'].join('\n'), description: 'Please select the Environment'),
                                    choice(name: 'IMAGE_TAG', choices: getDockerImages(), description: 'Available Docker Images')]
                    env.ENVIRONMENT = INPUT_PARAMS.ENVIRONMENT
                    env.IMAGE_TAG = INPUT_PARAMS.IMAGE_TAG
                }
            }
        }
    }  
  
    stage('Build App') {
      steps {
        //script {
        //    def pom = readMavenPom file: 'pom.xml'
        //    version = pom.version
        //}
        script {
	        if(BRANCH_NAME ==~ /(release.*)/) {
	            def tokens = BRANCH_NAME.tokenize( '/' )
	            branch_name = tokens[0]
	            branch_release_number = tokens[1]
	            version = branch_release_number
	        }
	        else {
	            sh (
	                    script: "${mvnCmd} -B help:evaluate -Dexpression=project.version | grep -e '^[^\\[]' > release.txt",
	                    returnStdout: true,
	                    returnStatus: false
	               )
	            version = readFile('release.txt').trim()
	            echo "release_number: ${version}"
	        }
        }
        sh "${mvnCmd} install -DskipTests=true -Dbuild.number=${version}"
      }
    }
 
    stage('Test') {
      steps {
        sh "${mvnCmd} test -Dbuild.number=${version}"
       // step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
      }
    }

    stage('Build Container') {
      steps {
            container("buildah") {
              withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'rh-credentials',
usernameVariable: 'RH_USERNAME', passwordVariable: 'RH_PASSWORD'], [$class: 'UsernamePasswordMultiBinding', credentialsId: 'quay-credentials',
usernameVariable: 'QUAY_USERNAME', passwordVariable: 'QUAY_PASSWORD']]) {
                sh  """
                  echo '->> In Buildah ${app_name}-${version} <<-'
                  buildah login -u $RH_USERNAME -p $RH_PASSWORD registry.redhat.io
                  buildah login -u $QUAY_USERNAME -p $QUAY_PASSWORD ${quay_host}                 
                  buildah bud -t ${quay_host}/${quay_org}/${app_name}:${version} .
                  buildah push ${quay_host}/${quay_org}/${app_name}:${version}
                  echo '->> Done Buildah <<-'
                """
            }
          }
        }
    }
   
    stage('Helm Package') {
      steps {
            container("buildah") {
                sh  """
                  echo '->> In Helm Package <<-'
                  helm package src/main/helm/ --version=${version} --app-version=${version} 
                  echo '->> Done Helm Package <<-'
                """
            }
        }
    }
    stage('Deploy DEV') {
      when {
        branch 'develop'
      }
      steps {
        container("buildah") { 
          sh  """
            echo '->> In Helm Install DEV ${app_name}-${version} <<-'
            helm upgrade --install ${app_name} ${app_name}-${version}.tgz --namespace=${dev_project}
            echo '->> Done Helm Install <<-'
          """	            
        }
      }
    }	 
    stage('Deploy TEST') {
      when {
        branch 'release/*'
      }
      steps {
        container("buildah") { 
          sh  """
            echo '->> In Helm Install TEST ${app_name}-${version} <<-'
            helm upgrade --install ${app_name} ${app_name}-${version}.tgz --namespace=${test_project}
            echo '->> Done Helm Install <<-'
          """	            
        }
      }
    }	
 
  }
}
