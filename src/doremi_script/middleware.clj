(ns doremi-script.middleware)

(defn- log [msg & vals]
    (let [line (apply format msg vals)]
          (locking System/out (println line))))
 
(defn wrap-request-logging [handler]
    (fn [{:keys [request-method uri] :as req}]
          (let [resp (handler req)]
                  (log "Processing %s %s" request-method uri)
                  resp)))
