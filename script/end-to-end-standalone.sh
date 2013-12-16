tempdir=$HOME/tmp
fname=$tempdir/$(basename $1)
dirname=`dirname $0`
jarpath=$dirname/../target/doremi-script-standalone.jar
echo "Using jar file $jarpath"
echo "Using tmp directory $tempdir"
echo "Creating  directory if it doesn't exist"
mkdir -p $tempdir
echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo "Reading $1"
echo "Temporary files created are $fname.json and $fname.ly"
echo "creating $fname.json"
cat $1 | java -jar $jarpath --json > $fname.json
echo "using lein run --ly"
cat $1 | java -jar $jarpath --ly | tee $fname.ly |  lilypond -o $fname -f png - ; feh $fname.png

