//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
def mvnCmd = "mvn"
def version="1.0"
def app_name="my-sb-war"
def dev_project="dev-my-sb-war"
def quay_host="docker.io"
def quay_org="vizuri"

pipeline {

  agent {
    label 'maven-buildah'
  }
  stages {
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
        sh "${mvnCmd} install -DskipTests=true -Dbuild.number=${release_number}"
      }
    }
 
    stage('Test') {
      steps {
        sh "${mvnCmd} test -Dbuild.number=${release_number}"
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
                  buildah bud -t ${quay_org}/${app_name}:${version} .
                  buildah push ${quay_org}/${app_name}:${version}
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
      steps {
        container("buildah") { 
          sh  """
            echo '->> In Helm Install ${app_name}-${version} <<-'
            helm upgrade --install ${app_name} ${app_name}-${version}.tgz --namespace=${dev_project}
            echo '->> Done Helm Install <<-'
          """	            
        }
      }
    } 
  }
}
