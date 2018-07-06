#!/bin/sh
# Build & extract a set of apk from an aab for a specific device and install the results on it.
#
# This needs https://github.com/google/bundletool to be available as `bundletool`.
# Also **exactly** one device needs to be connected to adb.
# Usage: installFromBundle bundle.aab
# optional `--extract-apks` to keep the set on your workstation as well.

basename=${1%%.*}
keystore="~/.android/debug.keystore"
ks_alias="androiddebugkey"
pass="pass:android"

bundletool build-apks --bundle=$1 --ks=$keystore --ks-key-alias=$ks_alias --ks-pass=$pass --output=$basename.apks
bundletool install-apks --apks=$basename.apks

if [ "$2" = "--extract-apks" ]
then
  bundletool get-device-spec --output=spec.json
  bundletool extract-apks --apks=$basename.apks --device-spec=spec.json --output-dir=.
fi
