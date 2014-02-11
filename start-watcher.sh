WATCHER_PID_FILE="watcher.pid"
echo "Starting watch script (inotifywait)- watch_for_changed_ly_files.sh"
echo "Watch pid is in $WATCHER_PID_FILE"
WATCH_LOG="watch.log"
echo "Watch is logging to $WATCH_LOG"
nohup watch_for_changed_ly_files.sh > $WATCH_LOG &
WATCHER_PID=$!
echo $WATCHER_PID > $WATCHER_PID_FILE


