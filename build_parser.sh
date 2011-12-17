#!/usr/bin/env sh
# Shell script to compile the sargam peg grammar
# Puts compiled grammar into both lib and the web app public directory
GRAMMAR=doremiscript.peg.js
LIB=./lib/doremi-script/doremi_script_parser.js
WEBAPP=./src/web_app/public/js/
echo "Running pegjs on $GRAMMAR to create parser in $LIB"
./bin/pegjs -e DoremiScriptParser ./src/$GRAMMAR $LIB 
echo "copying to $WEBAPP"
cp $LIB $WEBAPP



