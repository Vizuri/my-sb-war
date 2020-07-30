//def version, mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
def version, mvnCmd = "mvn"

pipeline {

  agent {
    label 'maven-buildah'
  }
  
  stages {
//	stage('Checkout') {
//	  steps {
//		//checkout scm
//	    checkout([
//         $class: 'GitSCM',
//         branches: scm.branches,
//         doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
//        extensions: scm.extensions,
//         userRemoteConfigs: scm.userRemoteConfigs
//    	])
//      }
//    }
    // Add Lab 3 Here
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
                  buildah bud -t vizuri/my-sb-war:1.0 .
                  buildah push vizuri/my-sb-war:1.0
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
                  helm package src/main/helm/ --app-version=1.0
                  echo '->> Done Helm Package <<-'
                '''
            }
        }
    }


    stage('Deploy Dev') {
      steps {
        container("buildah") {  
          scripts {    
	          openshift.withCluster() {
	            openshift.withProject("dev-my-sb-war") {
                sh  '''
                  echo '->> In Helm Install <<-'
                  helm install src/main/helm/ --app-version=1.0
                  echo '->> Done Helm Install <<-'
                '''	            
	            }
	          }
	       }
        }
      }
    }
  }
}
