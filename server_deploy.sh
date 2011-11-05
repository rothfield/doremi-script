git pull
#./deploy_local.sh
WEBAPP_DIR=./bin/doremi_web
echo "WEBAPP_DIR is $WEBAPP_DIR"
sudo killall -9 ruby
cd $WEBAPP_DIR
 nohup ruby application.rb  &
echo "restarted sinatra app"

