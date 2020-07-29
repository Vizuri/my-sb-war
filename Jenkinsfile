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



  }
}
