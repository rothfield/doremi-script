PID_FILE="comp.pid"
SERVER_LOG="server.log"
echo "Starting compojure/ring in background"
echo "Running lein ring server-neadless &"
nohup lein ring server-headless > $SERVER_LOG &
COMPOJURE_PID=$!
echo $COMPOJURE_PID > $PID_FILE
echo "saved compojure pid $COMPOJURE_PID to $PID_FILE"
