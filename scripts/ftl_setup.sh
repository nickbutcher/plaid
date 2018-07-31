#!/bin/sh

# Check if this script is running on the main, non-forked repository.
# Environment variables are not set in forked repository builds;
# in this case, skip Firebase Test Lab steps and finish the build.
if [ -z "$IS_MAIN_PLAID_REPO" ]; then
  echo "Running build on a forked repository - skipping FTL tests."
  circleci step halt
  exit 0
fi

# Check if service key environment variable is set; exit if not
if [ -z "$GCLOUD_SERVICE_KEY" ]; then
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
