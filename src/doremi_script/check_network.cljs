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

(def debug false)

(defn check-network[]
  (when debug (println "checking network"))
  (let [uri (new goog/Uri "//www.google.com/images/cleardot.gif")
        _  (.makeUnique uri)
        img (new js/Image)
        loaded-chan (listen img "load")
        error-chan (listen img "error")
        _ (set! (.-src img) (str uri))
        ]
    ;; TODO: use alt ???? review
    (go  (let[result (<! loaded-chan)] 
             (dispatch [:set-online-state true]) 
             ))
    (go  (let[result (<! error-chan)] 
             (dispatch [:set-online-state false]) 
    ))))

(defn start-check-network[]
(go (while true
      (check-network)
      (<! (timeout (* 20 seconds))) 
      )))

