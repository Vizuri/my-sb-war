mvn clean install -DskipTests
docker build -t vizuri/my-sb-war:1.0 .
docker push vizuri/my-sb-war:1.0

