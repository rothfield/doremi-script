#!/usr/bin/env sh
# Shell script to compile the sargam peg grammar
# Puts compiled grammar into both lib and the web app public directory
PEGJS=./node_modules/pegjs/bin/pegjs
GRAMMAR=doremiscript.peg.js
LIB=./lib/doremi-script/doremi_script_parser.js
LIB2=./lib/doremi-script/doremi_script_line_parser.js
WEBAPP=./src/web_app/public/js/
echo "Running pegjs on $GRAMMAR to create parser in $LIB"
$PEGJS -e DoremiScriptParser ./src/$GRAMMAR $LIB 
$PEGJS -e DoremiScriptLineParser ./src/line.peg.js $LIB2 
echo "copying to $WEBAPP"
cp $LIB $WEBAPP



