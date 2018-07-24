#!/bin/sh

# Check if service key environment variable is set; exit if not
if if [ -z "$GCLOUD_SERVICE_KEY" ]; then
  echo "GCLOUD_SERVICE_KEY env variable is empty. Exiting."
  exit 1
fi

# Export to secrets file
echo $GCLOUD_SERVICE_KEY | base64 -di > client_secret.json

# Set project ID
gcloud config set project android-devrel-ci

# Auth account
gcloud auth activate-service-account plaid-ftl@android-devrel-ci.iam.gserviceaccount.com --key-file client_secret.json

# Delete secret
rm client_secret.json
