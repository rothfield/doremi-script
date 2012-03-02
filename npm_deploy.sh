#!/bin/bash
# copy files as necessary for npm package
CSS_DIR=./lib/doremi-script-base/css
mkdir -p $CSS_DIR
mkdir -p ./lib/doremi-script-base/third_party
echo "copying mustache files to lib/doremiscript"
cp src/*.mustache lib/doremi-script-base/
cp ./vendor/third_party/zepto.unminified.js lib/doremi-script-base/third_party/
cp ./vendor/third_party/fraction.js lib/doremi-script-base/third_party/

