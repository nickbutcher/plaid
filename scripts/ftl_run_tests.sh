#!/bin/sh

# The Continuous Integration build ID, unique for each CI test run
BUILD_ID=$1

# The directory that will contain the FTL test results
TEST_DIR=$2

# Store the exit code from running the gcloud command. This is returned at the end of the script
GCLOUD_EXIT_CODE=0

test_apk() {
    RESULTS_DIR=$1_$BUILD_ID

    gcloud firebase test android run \
        --type instrumentation \
        --app $2 \
        --test $3 \
        --device model=Pixel2,version=28,locale=en_US,orientation=portrait \
        --timeout 30m \
        --results-bucket cloud-test-android-devrel-ci \
        --results-dir=$RESULTS_DIR

    # Capture the exit code of the gcloud command
    # TODO: change this logic if running multiple gcloud commands, otherwise only the last gcloud's exit code is stored
    GCLOUD_EXIT_CODE=$?

    # Make result dir
    mkdir -p "$TEST_DIR/$RESULTS_DIR"

    # Pull down test results
    gsutil -m cp -r -U "gs://cloud-test-android-devrel-ci/$RESULTS_DIR/*" "$TEST_DIR/$RESULTS_DIR"
}

test_apk \
    "app" \
    "app/build/outputs/apk/debug/plaid-debug.apk" \
    "core/build/outputs/apk/androidTest/debug/core-debug-androidTest.apk"

exit "$GCLOUD_EXIT_CODE"
