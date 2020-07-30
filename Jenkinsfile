//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
def version, mvnCmd = "mvn"
def app_name="my-sb-war"
def project="dev-my-sb-war"

pipeline {

  agent {
    label 'maven-buildah'
  }
  
    stage('Build App') {
      steps {
        //git branch: 'master', url: 'http://gogs.apps.ocpws.kee.vizuri.com/student1/openshift-tasks.git'
        script {
            def pom = readMavenPom file: 'pom.xml'
            version = pom.version
        }
        sh "${mvnCmd} install -DskipTests=true"
      }
    }
 
    stage('Test') {
      steps {
        sh "${mvnCmd} test"
       // step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
      }
    }

    stage('Build Container') {
      steps {
            container("buildah") {
                sh  '''
                  echo '->> In Buildah <<-'
                  buildah login -u keudy@vizuri.com -p M@dison30 registry.redhat.io
                  buildah login -u kenteudy -p M@dison30 docker.io                 
                  buildah bud -t vizuri/my-sb-war:${version} .
                  buildah push vizuri/my-sb-war:${version}
                  echo '->> Done Buildah <<-'
                '''
            }
        }
    }
   
    stage('Helm Package') {
      steps {
            container("buildah") {
                sh  '''
                  echo '->> In Helm Package <<-'
                  helm package src/main/helm/ --version=1.0 --app-version=1.0
                  echo '->> Done Helm Package <<-'
                '''
            }
        }
    }
    
    stage('Deploy DEV') {
      steps {
        container("buildah") { 
          sh  '''
            echo '->> In Helm Install <<-'
            helm upgrade --install ${app_name} my-sb-war-1.0.tgz --namespace=dev-my-sb-war
            echo '->> Done Helm Install <<-'
          '''	            
        }
      }
    } 
  }
}
