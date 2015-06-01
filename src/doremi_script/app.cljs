(ns doremi-script.app
  (:require-macros 
    )
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

(def debug false)

(enable-console-print!)




(defn user-entry[]
    (.-value (by-id "area2"))
      )


(defn init []
  (dispatch-sync [:initialize initial-state])
  (dispatch [:start-check-network])
  (dispatch [:load-parser])
  (let [old-val (user-entry) 
        url-to-load (.getParameterValue
                      (new goog/Uri (.-href (.-location js/window)))
                      "url")
        _ (log "url-to-load is" url-to-load)
        ]
    (reagent.core/render-component 
      [doremi-box]
      (by-id "container"))

    (when url-to-load
        (dispatch [:open-url url-to-load]))

    (.focus (by-id "area2"))
    (if old-val
      (set! (.-value (by-id "area2")) old-val))
    ))


