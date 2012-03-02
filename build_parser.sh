#!/usr/bin/env sh
# Shell script to compile the sargam peg grammar
# Puts compiled grammar into both lib and the web app public directory
PEGJS=./node_modules/pegjs/bin/pegjs
GRAMMAR=doremiscript.peg.js
LIB_DIR=./lib/doremi-script-base/
LIB=./lib/doremi-script-base/doremi_script_parser.js
LIB2=./lib/doremi-script-base/doremi_script_line_parser.js
echo "building parser and line parser to $LIB_DIR"
echo "Running pegjs on $GRAMMAR to create parser in $LIB"
$PEGJS -e DoremiScriptParser ./src/$GRAMMAR $LIB 
$PEGJS -e DoremiScriptLineParser ./src/line.peg.js $LIB2 



