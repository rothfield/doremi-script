WEB_DIR="./src/web_app/public/js/" 
LIB_DIR="./lib/doremi_script_base/"
echo "compiling to $WEB_DIR"
echo "compiling to $LIB_DIR"
# TODO: DRY with watch.sh
coffee -c -o $WEB_DIR src/*.coffee &
coffee -c -o $WEB_DIR test/*.coffee &
coffee -c -o $LIB_DIR test/*.coffee &
coffee -c -o $LIB_DIR src/*.coffee
echo "running build_parser.sh"
./build_parser.sh
echo "running npm_deploy.sh"
./npm_deploy.sh
echo "running sudo npm link"
sudo npm link

