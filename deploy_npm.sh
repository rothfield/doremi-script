#!/bin/bash
# move files as necessary for npm
CSS_DIR=./lib/sargam/css
JS_DIR=./lib/sargam
SRC_WEBAPP_DIR=./src/web_app/
THIRD_PARTY_DIR=$WEBAPP_DIR/public/js/third_party

rm -rf $CSS_DIR
echo "move css  to $CSS_DIR"
mkdir -p $CSS_DIR
mkdir -p ./lib/sargam/third_party
cp -r $SRC_WEBAPP_DIR/css/* $CSS_DIR
cp ./vendor/third_party/zepto.unminified.js lib/sargam/third_party/

