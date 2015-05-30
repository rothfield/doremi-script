(ns doremi-script.app
  (:require-macros 
    ;;               [cljs.core :refer [assert]]
    [cljs.core.async.macros :refer [go]]
    [reagent.ratom :refer [reaction]]
    )
  (:require 
    [quile.component :as component]
    [doremi-script.dom-utils :refer [by-id listen seconds]]
    [doremi-script.views :refer [doremi-box]]
    [doremi-script.handlers]
    [doremi-script.subscriptions]
    [doremi-script.sargam-key-map :refer [default-key-map]]
    [doremi-script.core :refer [ new-parser ]]
    [doremi-script.utils :refer [log] ]
    [goog.dom :as dom]
    [goog.Uri] 
    [goog.net.XhrIo]
    [goog.json]
    [cljs.core.async :refer [<! chan close! timeout put!]]
    [reagent.core :as reagent]
    [re-frame.core :refer [register-handler
                           path
                           register-sub
                           dispatch
                           dispatch-sync
                           subscribe]]
    [cljs.reader :refer [read-string]]
    [instaparse.core :as insta] 
    ))

(def debug false)

(enable-console-print!)


(def production?
  ;; set the global in index.html
  (and (not= js/undefined js/DOREM_SCRIPT_APP_ENV)
       (= js/DOREM_SCRIPT_APP_ENV "production")
       ))

(log "production? = " production?)

(def development?
  (not production?))

(def GENERATE-STAFF-NOTATION-URL
  (if production?
    "http://ragapedia.com/doremi-server/run-lilypond-on-doremi-text"
    "http://localhost:4000/doremi-server/run-lilypond-on-doremi-text")
  )
(def PARSE-URL
  ;; TODO dry
  (if production?
    "http://ragapedia.com/doremi-server/parse"
    "http://localhost:4000/doremi-server/parse")
  )

;; takes 30 seconds to load unserialized grammar. 1 second for serialized
(def unserialized-grammar-path "ebnf/doremiscript.ebnf")
(def serialized-grammar-path "ebnf/grammar.txt") 

(defn stop-default-action[event]
  (.preventDefault event)) 



(def initial-state
  {
   :generate-staff-notation-url GENERATE-STAFF-NOTATION-URL
   :parse-url PARSE-URL
   :parser nil
   :links []
   :doremi-text "|S"
   :online true 
   :the-parser nil
   :key-map  default-key-map
   :rendering false
   :ajax-is-running false 
   :composition-kind :sargam-composition
   :mp3-url nil
   ;;"http://ragapedia.com/compositions/yesterday.mp3"
   :render-as :abc-composition
   :staff-notation-path nil 
   :composition nil 
   })






(defonce printing (reagent.core/atom false))

(declare draw-item) ;; need forward reference since it is recursive

(defn load-grammar-xhr[]
  (log "loading unserialized-grammar")
  ;; returns a channel which will contain the (uncompiled) grammar
  (let [out (chan)]
    (.send goog.net.XhrIo 
           unserialized-grammar-path
           (fn load-grammar-callback[x]
             (put! out (.getResponseText (.-target x))))
           )
    out))

(defn load-serialized-grammar-xhr[]
  (log "loading serialized-grammar from ebnf/grammar.txt") 
  ;; returns a channel which will contain the compiled grammar
  (let [out (chan)]
    (.send goog.net.XhrIo 
           serialized-grammar-path
           (fn load-grammar-callback[x]
             (let [data (js->clj (.getResponseText (.-target x))
                                 :keywordize-keys true)]
               (put! out (read-string data))
               ))
           )
    out))





(defn milliseconds-since-epoch[]
  (.getTime (js/Date.))
  )
(def million 1000000)
(defn print-elapsed-seconds[t1 t2]
  (println "seconds elapsed="  
           (* .001 (- (milliseconds-since-epoch) t1)))
  )



(defn print-out-grammar[]
  (println "Save this in resources/ebnf.txt")
  (go
    (let [parser (component/start (new-parser (<! (load-grammar-xhr))))]
      ;; use this to create ebnf.txt file
      (binding [*print-dup* true] 
        (prn (:grammar (:parser parser)) 
             )))))


;;;; *******************IMPORTANT****************
;;;; When the doremi-script grammar changes, grab the generated file
;;;; and add it to doremi-script
;;;; Then run the following code and save the output in doremi-script/resources
;;;; see serialized-grammar-path and unserialized-grammar-path
(when false
  (print-out-grammar))

(defn user-entry[]
  (.-value (dom/getElement "area2"))
  )

(defn parse-xhr[url {txt :txt kind :kind }]
  (println "parse-xhr stub")
  (chan)
  ;; TODO: review old code
  )


(defn init []
  (dispatch [:start-check-network])
  (dispatch-sync [:initialize initial-state])
  (go
     (dispatch [:set-parser 
           (component/start (new-parser
                              (<! (load-serialized-grammar-xhr))))
           ]) 
    )
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

    (log "starting timer")
    (.focus (by-id "area2"))
    (if old-val
      (set! (.-value (by-id "area2")) old-val))
    ))


