#!/bin/sh

BUILD_ID=$1
TEST_DIR=$2

test_apk() {
	RESULTS_DIR=$1_$BUILD_ID

    gcloud firebase test android run \
	    --type instrumentation \
	    --app $2 \
	    --test $3 \
	    --device model=Nexus6P,version=27,locale=en_US,orientation=portrait \
	    --timeout 30m \
	    --results-bucket cloud-test-android-devrel-ci \
	    --results-dir=$RESULTS_DIR \
	    --no-record-video \
	    --no-performance-metrics

    # Make result dir
    mkdir -p "$TEST_DIR/$RESULTS_DIR"

	# Pull down test results
	gsutil -m cp -r -U "gs://cloud-test-android-devrel-ci/$RESULTS_DIR/*" "$TEST_DIR/$RESULTS_DIR"
}

test_apk \
	"app" \
	"app/build/outputs/apk/debug/plaid-debug.apk" \
	"app/build/outputs/apk/androidTest/debug/plaid-debug-androidTest.apk"