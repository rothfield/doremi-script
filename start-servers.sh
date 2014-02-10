PID_FILE="comp.pid"
WATCHER_PID_FILE="watcher.pid"
echo "Starting compojure/ring in background"
nohup lein ring server-headless &
COMPOJURE_PID=$!
echo $COMPOJURE_PID > $PID_FILE
echo "Starting watch script (inotifywait)- watch_for_changed_ly_files.sh"
nohup watch_for_changed_ly_files.sh &
WATCHER_PID=$!
echo $WATCHER_PID > $WATCHER_PID_FILE

