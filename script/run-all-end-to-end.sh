dirname=`dirname $0`
all-end-to-end.sh $dirname/../resources/fixtures/*.txt
chromium http://doremi.local/report.html

