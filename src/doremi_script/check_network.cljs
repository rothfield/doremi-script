(ns doremi-script.check-network
  (:require-macros 
    [cljs.core.async.macros :refer [go]]
    )
  (:require	
    [goog.Uri] 
    [doremi-script.dom-utils :refer [by-id listen seconds]]
    [cljs.core.async :refer [<! chan close! timeout put!]]
    [re-frame.core :refer [dispatch]]
    ))

(defn check-network[]
  (println "checking network")
  (let [uri (new goog/Uri "//www.google.com/images/cleardot.gif")
        _  (.makeUnique uri)
        img (new js/Image)
        ch (listen img "load")
        ch2 (listen img "error")
        _ (set! (.-src img) (str uri))
        ]
    (go  (let[result (<! ch)] 
             (dispatch [:set-online-state true]) 
             ))
    (go  (let[result (<! ch2)] 
             (dispatch [:set-online-state false]) 
    ))))

(defn start-check-network[]
(go (while true
      (check-network)
      (<! (timeout (* 300 seconds))) 
      )))

