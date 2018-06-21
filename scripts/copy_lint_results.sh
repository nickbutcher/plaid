#!/bin/sh

RESULTS_DIR="ci_results/lint/"
mkdir -p $RESULTS_DIR

# Find all lint result files, except ones for third party libraries
LINT_FILES=`find . -type f -regex ".*/build/reports/lint-results\..*" | grep -v third_party`

# Rename each file and append the module name. Examples:
# app/build/reports/lint-results.html -> app/build/reports/lint-results-app.html
# base/build/reports/lint-results.html -> base/build/reports/lint-results-base.html
for file in $LINT_FILES
do
  rename 's/\/(.*)\/build\/reports\/lint-results/\/$1\/build\/reports\/lint-results-$1/' $file
done

# Move all of the renamed files to the $RESULTS_DIR
find . -type f -regex ".*/build/reports/lint-results-.*\..*" -exec cp {} $RESULTS_DIR \;
