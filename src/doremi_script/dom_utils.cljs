(ns doremi-script.dom-utils
  (:require-macros 
    [cljs.core.async.macros :refer [go]]
    )
  (:require	
    [goog.events :as events]
    [cljs.core.async :refer [<! chan close! timeout put!]]
    ))

(def production?
  ;; set the global in index.html
  (and (not= js/undefined js/DOREM_SCRIPT_APP_ENV)
       (= js/DOREM_SCRIPT_APP_ENV "production")
       ))

(def seconds 1000)

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn listen [el event-type]
  (let [out (chan)]
    (events/listen el event-type
                   (fn [e] (put! out e)))
    out))
