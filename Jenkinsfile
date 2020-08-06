//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
import groovy.json.JsonSlurper
def mvnCmd = "mvn"
def version="1.0"
def app_name="my-sb-war"
def quay_host="docker.io"
def quay_org="vizuri"

def nextVersionFromGit(scope) {
    def latestVersion = sh returnStdout: true, script: 'git describe --tags "$(git rev-list --tags=*.*.* --max-count=1 2> /dev/null)" 2> /dev/null || echo 0.0.0'
    def (major, minor, patch) = latestVersion.tokenize('.').collect { it.toInteger() }
    def nextVersion
    switch (scope) {
        case 'major':
            nextVersion = "${major + 1}.0.0"
            break
        case 'minor':
            nextVersion = "${major}.${minor + 1}.0"
            break
        case 'patch':
            nextVersion = "${major}.${minor}.${patch + 1}"
            break
    }
    nextVersion
}
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
                                    choice(name: 'RELEASE_SCOPE', choices: ['major','minor', 'patch'].join('\n'), description: 'Release Scope'),
                                    booleanParam(name: 'UNINSTALL', defaultValue: false, description: 'Perform Uninstall')]
                    env.ENVIRONMENT = INPUT_PARAMS.ENVIRONMENT
                    env.RELEASE_SCOPE = INPUT_PARAMS.RELEASE_SCOPE
                    env.UNINSTALL = INPUT_PARAMS.UNINSTALL
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
	        if(ENVIRONMENT == 'prod') {
	            version = nextVersionFromGit(RELEASE_SCOPE)
	            echo "release_number: ${version}"
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
           if( UNINSTALL ) {
	         sh  """
               echo '->> In Uninstall <<-'
               helm uninstall ${ENVIRONMENT}-${app_name} --namespace=default
               echo '->> Done Uninstall <<-'
             """	
	      }
          
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
      sshagent (credentials: ['github-jenins']) {
          sh  """
            echo '->> In Tag <<-'
            git config --global user.email 'jenkins@upenn.edu'
  			git config --global user.name 'Jenkins'
            
            git tag -a ${version} -m 'Jenkins Tag'
            git push --tags
            echo '->> Done Tag <<-'
          """	            
      } 
      }
    }    
  }
}
