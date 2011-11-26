OUT_DIR="./webapp/doremi_web/public/js/" 
echo "compiling to $OUT_DIR"
coffee -c -w -o $OUT_DIR src/test/*.coffee &
coffee -c -w -o lib/doremi-script src/*.coffee &
coffee -c -w -o lib/doremi-script src/test/*.coffee &
coffee -c -w -o $OUT_DIR src/*.coffee 

