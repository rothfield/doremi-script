#!/bin/sh

# get the current path
current_path=`pwd`
watch_dir=$HOME/compositions
echo "Watching $watch_dir for changes"
inotifywait -mr --timefmt '%d/%m/%y %H:%M' --format '%T %w %f' \
		-e close_write $watch_dir | while read date time dir file; do
file_changed=${dir}${file}
# convert absolute path to relative
FILECHANGEREL=`echo "$file_changed" | sed 's_'$current_path'/__'`
ext=${file##*.}
cd $watch_dir
if [ $ext = "ly" ]
then
		fp="${file%.*}"  # file without .ly extension
		name_with_page1=${fp}-page1.png
		echo "fp= $fp"
		echo "name_with_page1= $name_with_page1"
		lily2image -f=png -q $file_changed
		#lilypond --png $file_changed
		if [ -f "$name_with_page1" ]
		then
				echo "$name_with_page1 found."
				echo "Combining pages using convert"
				#lilypond --png $file_changed
				convert ${fp}-page*.png -append ${fp}.png
		else
				echo "$name_with_page1 not found."
		fi
		echo "At ${time} on ${date}, file $file_changed was run through lily2image"
fi
done
