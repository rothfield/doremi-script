#!/bin/bash
# copy files as necessary for npm package
CSS_DIR=./lib/doremi-script/css
mkdir -p $CSS_DIR
mkdir -p ./lib/doremi-script/third_party
echo "copying mustache files to lib/doremiscript"
cp src/*.mustache lib/doremi-script/
cp ./vendor/third_party/zepto.unminified.js lib/doremi-script/third_party/
cp ./vendor/third_party/fraction.js lib/doremi-script/third_party/

