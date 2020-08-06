#helm repo add kee-helm-repo http://kee-helm-repo.s3-website-us-east-1.amazonaws.com
helm package src/main/helm/ --version=1.0 --app-version=1.0
mkdir charts
mv my-sb-war-1.0.tgz charts
helm repo index charts --url http://kee-helm-repo.s3-website-us-east-1.amazonaws.com
aws s3 sync --acl public-read charts s3://kee-helm-repo
