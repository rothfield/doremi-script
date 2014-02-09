#!/bin/sh

# get the current path
CURPATH=`pwd`

inotifywait -mr --timefmt '%d/%m/%y %H:%M' --format '%T %w %f' \
		-e close_write /home/john/doremi-script-clojure/resources/public/compositions | while read date time dir file; do

FILECHANGE=${dir}${file}
# convert absolute path to relative
FILECHANGEREL=`echo "$FILECHANGE" | sed 's_'$CURPATH'/__'`
EXT=${file##*.}
if [ $EXT = "ly" ]
then
lily2image -f=png -q $FILECHANGE
#lilypond --png $FILECHANGE
echo "At ${time} on ${date}, file $FILECHANGE was run through lily2image"
fi
done
