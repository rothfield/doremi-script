TARGET=app_target
echo "target directory is $TARGET"
echo "running git pull"
git pull
#echo "cleaning $TARGET"
#rm -rf $TARGET
echo "running boot cljs -O advanced"
boot cljs -O advanced
echo "minifying application.css and doremi.css in $TARGET/css.
#Uses npm minify command"
# sudo npm install -g minify 
cd ./$TARGET/css
# minify doesn't take multiple args
echo "minifying bootstrap.css doremi.css and application.css" 
minify bootstrap.css 
minify doremi.css 
minify application.css 
echo "combining css files into app.min.css"
cat bootstrap.min.css doremi.min.css application.min.css > app.min.css
cd ..
cd js
echo "minifying "
#minify jquery.js
#minify bootstrap.js
#cat jquery.min.js bootstrap.min.js app.js > app.min.js
cat app.js > app.min.js
cd ..
echo "adding async to script tag for app.js in index.html and changing app.js to app.min.js"
sed -i 's/app.js\"/app.min.js\" async/g' index.html 
echo "setting DOREM_SCRIPT_APP_ENV=production"
sed -i 's/DOREM_SCRIPT_APP_ENV=\"development\"/DOREM_SCRIPT_APP_ENV=\"production\"/' index.html

echo "adding manifest to html tag in index.html"
echo "deleting stylesheet tags"
sed -i 's#<link rel=\"stylesheet\".*># <!-- & --> #' index.html
# sed -i 's#<script src=\"js/jquery.js\".*>#<!-- & --> #' index.html
#sed -i 's#<script src=\"js/bootstrap.js\".*>#<!-- & --> #' index.html


echo "adding stylesheet css/app.min.css"
sed -i 's#</body># </body>\n<link rel=\"stylesheet\" href=\"css/app.min.css\">#' index.html
sed -i 's#<-- loadcsshere -->#<script> loadCSS(\"css/app.min.css\" ); </script>#' index.html
sed  -i 's/^<html /<html manifest="manifest.appcache" /' index.html
echo "bumping version of manifest.appcache"
sed  -i "s/^#version.*$/#version `date`/" manifest.appcache
echo "to test, cd $TARGET and start http server. I use node's http-server"
cd ..
