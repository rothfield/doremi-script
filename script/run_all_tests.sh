DIR=`dirname $0`  # directory of this script
$DIR/all-end-to-end.sh $DIR/../resources/fixtures/*.txt
target_directory=$DIR/../test/test_results
chromium $target_directory/report.html

