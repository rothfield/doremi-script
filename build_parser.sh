GRAMMAR=sargam.peg.js
echo "Running pegjs on $GRAMMAR to create parser in ./bin/doremi_web/public/js/"
pegjs -e SargamParser ./src/grammars/$GRAMMAR  ./webapp/doremi_web/public/js/sargam_parser.js
pegjs -e SargamParser ./src/grammars/$GRAMMAR  ./lib/sargam/sargam_parser.js

