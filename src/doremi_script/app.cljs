(ns doremi-script.app
(:require-macros [clojure.core.strint :as strint])
  (:require 
    [doremi-script.db :refer [initial-state]]
    [doremi-script.dom-utils :refer [by-id]]
    [doremi-script.views :refer [doremi-box]]
    [doremi-script.handlers]
    [doremi-script.subscriptions]
    [doremi-script.utils :refer [log] ]
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]
    ))

;;; Clojurescript web app. 
(def debug false)

(enable-console-print!)

(let [name "Ethel Smyth"
            profession "Composer"
                  born 1858]
    (println
         (strint/<< "The person named ~{name} works as a ~{profession}
                    and was born in ~{born}")))

(defn user-entry[]
  (.-value (by-id "area2"))
  )

(defonce app-initialized? (atom false))

(defn init []
  ;; Don't reset the db if already initialized
  (let [old-val (user-entry)]
    (println "entering init")
    (when (not @app-initialized?)
      (when debug (println "initializing"))
      (dispatch-sync [:initialize initial-state])
      (dispatch [:start-check-network])
      (dispatch [:load-parser])
      (let [
            url-to-load (.getParameterValue
                          (new goog/Uri (.-href (.-location js/window)))
                          "url")
            _ (log "url-to-load is" url-to-load)
            ]

        (when url-to-load
          (dispatch [:open-url url-to-load]))
        (.focus (by-id "area2"))
        ))
    (reagent.core/render-component [doremi-box] (by-id "container"))
    ;; restore value to textarea
    (when (not= "" old-val) 
      (println "restoring " old-val)
       (set! (.-value (by-id "area2")) old-val)
      (dispatch [:set-doremi-text old-val]))
    (when (not @app-initialized?)
      (reset! app-initialized? true))
    ;;  (set! (.-value (by-id "area2")) old-val))
    )) 



