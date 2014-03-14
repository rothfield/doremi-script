#!/usr/bin/env sh
# Shell script to compile the sargam peg grammar
# Puts compiled grammar into both lib and the web app public directory
PEGJS=./node_modules/pegjs/bin/pegjs
HANDLEBARS=./node_modules/.bin/handlebars
GRAMMAR=doremiscript.peg.js
LIB_DIR=./lib/doremi_script_base/
LIB=./lib/doremi_script_base/doremi_script_parser.js
LIB2=./lib/doremi_script_base/doremi_script_line_parser.js
echo "building parser and line parser to $LIB_DIR"
echo "Running pegjs on $GRAMMAR to create parser in $LIB"
$PEGJS -e DoremiScriptParser ./src/$GRAMMAR $LIB 
$PEGJS -e DoremiScriptLineParser ./src/line.peg.js $LIB2 
#echo "compiling handlebar templates for html renderer to $LIB_DIR"
#$HANDLEBARS src/views/ -f $LIB_DIR/templates.handlebars.js



