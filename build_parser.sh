#!/usr/bin/env sh
# Shell script to compile the sargam peg grammar
# Puts compiled grammar into both lib and the web app public directory
GRAMMAR=sargam.peg.js
LIB=./lib/sargam/sargam_parser.js
WEBAPP=./webapp/doremi_web/public/js/sargam_parser.js
echo "Running pegjs on $GRAMMAR to create parser in $LIB"
pegjs -e SargamParser ./src/grammars/$GRAMMAR $LIB 
echo "copying to $WEBAPP"
cp $LIB $WEBAPP



