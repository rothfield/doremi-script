#!/bin/bash
# deploy the sinatra web app
sudo killall -9 ruby

WEBAPP_DIR=./webapp/doremi_web
SRC_WEBAPP_DIR=./src/web_app/
THIRD_PARTY_DIR=$WEBAPP_DIR/public/js/third_party

cp src/shims.js lib/doremi-script
cp src/shims.js $WEBAPP_DIR/public/js


echo "WEBAPP_DIR is" $WEBAPP_DIR
echo "copying application.rb  to $WEBAPP_DIR"
rm -rf $WEBAPP_DIR/views
mkdir -p $WEBAPP_DIR/views
cp -r $SRC_WEBAPP_DIR/views $WEBAPP_DIR/
mkdir -p $WEBAPP_DIR/public/compositions
rm -rf $WEBAPP_DIR/public/samples
echo "copying sample/* to  $WEBAPP_DIR/public/samples"
cp -r ./samples $WEBAPP_DIR/public/
mkdir -p $WEBAPP_DIR/public/js/third_party
echo "copying $THIRD_PARTY_DIR files to $WEBAPP_DIR "
echo "copying third party to ..."
cp -r ./vendor/third_party/* $THIRD_PARTY_DIR/

cp $SRC_WEBAPP_DIR/application.rb $WEBAPP_DIR/
echo "copying $SRC_WEBAPP_DIR/* to $WEBAPP_DIR/public"

cp -r $SRC_WEBAPP_DIR/* $WEBAPP_DIR/public
cd $WEBAPP_DIR
if [ "$(hostname)" == 'ragapedia' ]; then
        echo "running sudo nohup since on server"
        nohup ruby application.rb &
else ruby application.rb 
fi

