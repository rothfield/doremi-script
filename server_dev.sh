echo "running  /etc/init.d/doremi-serverd stop to stop the production service"
sudo /etc/init.d/doremi-serverd stop
lein ring server-headless 4000
