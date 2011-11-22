OUT_DIR="./webapp/doremi_web/public/js/" 
echo "compiling to $OUT_DIR"
coffee -c -w -o $OUT_DIR src/test/*.coffee &
coffee -c -w -o lib/sargam src/coffeescript/*.coffee &
coffee -c -w -o lib/sargam src/test/*.coffee &
coffee -c -w -o $OUT_DIR src/coffeescript/*.coffee 

