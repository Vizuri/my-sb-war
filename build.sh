#mvn clean install -DskipTests
docker build -t vizuri/my-sb-war:1.0 .
docker push vizuri/my-sb-war:1.0
docker build -t quay.apps.ocpinfra.kee.vizuri.com/vizuri/my-sb-war:1.0 .
docker push quay.apps.ocpinfra.kee.vizuri.com/vizuri/my-sb-war:1.0

