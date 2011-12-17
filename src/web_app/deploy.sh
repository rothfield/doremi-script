#!/bin/bash
# deploy the sinatra web app
sudo killall -9 ruby
cd ../..
WEBAPP_DIR=./src/web_app
WEBAPP_THIRD_PARTY_DIR=$WEBAPP_DIR/public/js/third_party/

mkdir -p $WEBAPP_DIR/public/js/third_party
mkdir -p $WEBAPP_DIR/public/compositions

cp src/shims.js lib/doremi-script

echo "copying src/*.mustache $WEBAPP_DIR/public/js"
cp src/*.mustache $WEBAPP_DIR/public/js/
cp src/shims.js $WEBAPP_DIR/public/js/
echo "copying vendor/third_party to $WEBAPP_THIRD_PARTY_DIR "
cp -r ./vendor/third_party/* $WEBAPP_THIRD_PARTY_DIR/

cd $WEBAPP_DIR
echo "Running rackup. Access the app at http://localhost:9292/"
if [ "$(hostname)" == 'ragapedia' ]; then
        echo "running sudo nohup since on server"
        rackup &
 else rackup 
 fi

