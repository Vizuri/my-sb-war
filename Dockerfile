#FROM registry.access.redhat.com/jboss-webserver-3/webserver31-tomcat8-openshift:1.4
FROM registry.redhat.io/jboss-webserver-5/webserver53-openjdk11-tomcat9-openshift-rhel7:1.0
COPY target/*.war /deployments/ROOT.war 
RUN sed  '/^exec/i umask 002' /opt/jws-5.3/tomcat/bin/launch.sh > /tmp/launch.sh;cp /tmp/launch.sh /opt/jws-5.3/tomcat/bin/launch.sh;
RUN sed  '/^exec/i echo ">>>>> ***** STARTING TOMCAT ***** <<<<<<<"' /opt/jws-5.3/tomcat/bin/launch.sh > /tmp/launch.sh;cp /tmp/launch.sh /opt/jws-5.3/tomcat/bin/launch.sh;
