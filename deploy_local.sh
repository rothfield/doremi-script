#!/bin/bash
# deploy the sinatra web app
WEBAPP_DIR=./webapp/doremi_web
SRC_WEBAPP_DIR=./src/web_app/
THIRD_PARTY_DIR=$WEBAPP_DIR/public/js/third_party

echo "WEBAPP_DIR is" $WEBAPP_DIR
echo "copying application.rb  to $WEBAPP_DIR"
mkdir -p $WEBAPP_DIR/public/compositions

mkdir -p $WEBAPP_DIR/public/js/third_party
echo "copying $THIRD_PARTY_DIR files to $WEBAPP_DIR "
echo "copying third party to ..."
cp -r ./vendor/third_party/* $THIRD_PARTY_DIR/

cp ./src/sinatra_webapp/application.rb $WEBAPP_DIR/
echo "copying $SRC_WEBAPP_DIR/* to $WEBAPP_DIR/public"

cp -r $SRC_WEBAPP_DIR/* $WEBAPP_DIR/public
cd $WEBAPP_DIR
sudo killall -9 ruby
if [ "$(hostname)" == 'ragapedia' ]; then
        echo "running sudo nohup since on server"
        nohup ruby application.rb &
else ruby application.rb &
fi
firefox http://localhost:4567 &

