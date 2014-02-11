PID_FILE="comp.pid"
WATCHER_PID_FILE="watcher.pid"
SERVER_LOG="server.log"
echo "Starting compojure/ring in background"
echo "Running lein ring server-neadless &"
nohup lein ring server-headless > $SERVER_LOG &
COMPOJURE_PID=$!
echo $COMPOJURE_PID > $PID_FILE
echo "saved compojure pid to $PID_FILE"
echo "Starting watch script (inotifywait)- watch_for_changed_ly_files.sh"
echo "Watch pid is in $WATCHER_PID_FILE"
WATCH_LOG="watch.log"
echo "Watch is logging to $WATCH_LOG"
nohup watch_for_changed_ly_files.sh > $WATCH_LOG &
WATCHER_PID=$!
echo $WATCHER_PID > $WATCHER_PID_FILE

