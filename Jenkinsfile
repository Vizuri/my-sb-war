//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
import groovy.json.JsonSlurper
def mvnCmd = "mvn"
def version="1.0"
def app_name="my-sb-war"
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
 //   def tags = new ArrayList()
 //   if (json.tags == null || json.tags.size == 0)
//      tags.add("unable to fetch tags for ${APP_NAME}")
//    else
//      tags.addAll(json.tags)
	def tags = ["1.0", "1.1"]
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
                                    choice(name: 'ENVIRONMENT', choices: ['dev','test', 'perf', 'prod'].join('\n'), description: 'Please select the Environment'),
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
      when {
        expression { ENVIRONMENT != 'prod' }
      }
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
     when {
        expression { ENVIRONMENT != 'prod' }
      }      
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
    stage('Deploy') {
      when {
        expression { ENVIRONMENT != 'prod' }
      }
      steps {
        container("buildah") { 
          sh  """
            echo '->> In Helm Install DEV ${app_name}-${version} <<-'
            helm upgrade --install --set env=${ENVIRONMENT} ${ENVIRONMENT}-${app_name} ${app_name}-${version}.tgz --namespace=default
            echo '->> Done Helm Install <<-'
          """	            
        }
      }
    }	 	
    
    stage('Tag') {
      when {
        expression { ENVIRONMENT == 'prod' }
      }
      steps {
      sshagent (credentials: ['github-jenkins']) {
          sh  """
            echo '->> In Tag <<-'
            export GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
            ls -a
            cat .git/config
            git config user.name 'Jenkins'
			git config user.email 'jenkins@upenn.edu'
			git pull
			git status
            git tag -a v2.3.0 -m 'v2.3.0'
            git tag
            git push --tags
            echo '->> Done Tag <<-'
          """	            
      } 
      }
    }    
  }
}
