//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
def mvnCmd = "mvn"
def version="1.0"
def app_name="my-sb-war"
def project="dev-my-sb-war"

pipeline {

  agent {
    label 'maven-buildah'
  }
  stages {
    stage('Build App') {
      steps {
        //git branch: 'master', url: 'http://gogs.apps.ocpws.kee.vizuri.com/student1/openshift-tasks.git'
        script {
            def pom = readMavenPom file: 'pom.xml'
            //version = pom.version
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
              withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'rh-credentials',
usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh  """
                  echo 'uname=$USERNAME pwd=$PASSWORD'
                  echo '->> In Buildah ${app_name}-${version} <<-'
                  //buildah login -u keudy@vizuri.com -p M@dison30 registry.redhat.io
                  buildah login -u $USERNAME -p $PASSWORD registry.redhat.io
                  buildah login -u kenteudy -p M@dison30 docker.io                 
                  buildah bud -t vizuri/my-sb-war:${version} .
                  buildah push vizuri/my-sb-war:${version}
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
            helm upgrade --install ${app_name} ${app_name}-${version}.tgz --namespace=${project}
            echo '->> Done Helm Install <<-'
          """	            
        }
      }
    } 
  }
}
