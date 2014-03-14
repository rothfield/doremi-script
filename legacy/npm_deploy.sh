#!/bin/bash
# copy files as necessary for npm package
CSS_DIR=./lib/doremi_script_base/css
mkdir -p $CSS_DIR
mkdir -p ./lib/doremi_script_base/third_party
echo "copying mustache files to lib/doremiscript"
cp src/*.mustache lib/doremi_script_base/
cp ./vendor/third_party/zepto.unminified.js lib/doremi_script_base/third_party/
cp ./vendor/third_party/fraction.js lib/doremi_script_base/third_party/

