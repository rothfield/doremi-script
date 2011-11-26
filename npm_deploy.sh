#!/bin/bash
# move files as necessary for npm
CSS_DIR=./lib/doremi-script/css
JS_DIR=./lib/doremi-script
SRC_WEBAPP_DIR=./src/web_app/
THIRD_PARTY_DIR=$WEBAPP_DIR/public/js/third_party

rm -rf $CSS_DIR
echo "move css  to $CSS_DIR"
mkdir -p $CSS_DIR
mkdir -p ./lib/doremi-script/third_party
cp -r $SRC_WEBAPP_DIR/css/* $CSS_DIR
cp ./vendor/third_party/zepto.unminified.js lib/doremi-script/third_party/
cp ./vendor/third_party/fraction.js lib/doremi-script/third_party/

