WEB_DIR="./src/web_app/public/js/" 
LIB_DIR="./lib/doremi-script"
echo "compiling to $WEB_DIR"
echo "compiling to $LIB_DIR"
coffee -c -w -o $WEB_DIR src/*.coffee &
coffee -c -w -o $WEB_DIR test/*.coffee &
coffee -c -w -o $LIB_DIR test/*.coffee &
coffee -c -w -o $LIB_DIR src/*.coffee

