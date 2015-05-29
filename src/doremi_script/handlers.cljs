(ns doremi-script.handlers
  ;;(:require-macros 
  (:require-macros 
    ;;               [cljs.core :refer [assert]]
    [cljs.core.async.macros :refer [go]]
    [reagent.ratom :refer [reaction]]
    )
  (:require 
    [quile.component :as component]
    [doremi-script.sargam-key-map :refer
     [default-key-map mode-and-notes-used->key-map ]]
    [doremi-script.core :refer [
                                new-parser
                                doremi-text->collapsed-parse-tree]]
    [doremi-script.utils :refer [get-attributes keywordize-vector 
                                 log is?] ]

    ;; [doremi-script.doremi_core :as doremi_core
    ;; :refer [doremi-text->collapsed-parse-tree]]
    [goog.dom :as dom]
    [goog.Uri] 
    [goog.events :as events]
    [goog.net.XhrIo]
    [goog.json]
    [clojure.set]
    [clojure.string :as string :refer [lower-case upper-case join]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
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

(defn key-map-for-composition[composition]
  (let [
        attributes (if composition (get-attributes composition) {})
        notes-used (:notesused attributes)
        mode (:mode attributes)
        ]
    (mode-and-notes-used->key-map mode notes-used)
    ))

(defn update-db-with-parse-results[ db {:keys [:composition :error] :as results}]
  (merge db results
         (if error
           {}
           {:key-map (key-map-for-composition composition)})))


(println "in handlers")
(register-handler :set-parser
   (fn [db [_ parser]]
     (assoc db :parser parser)
     ))

(register-handler :redraw-letter-notation
   (fn [db [_]]
  (let [results
        (-> (:doremi-text db)  
      (doremi-text->collapsed-parse-tree 
        (:parser db) 
        (:composition-kind db)))]
    (update-db-with-parse-results db results)
     )))

(register-handler :open-url
   (fn [db [_ url]]
     (println ":open-url" url)
  (goog.net.XhrIo/send url
                       (fn[event]
                         (let [raw-response (.-target event)
                               response-text (.getResponseText raw-response)
                               ]
              (dispatch [:open-url-callback response-text])))
                       "GET")
     (assoc db :ajax-is-running true)))


(register-handler :open-url-callback
   (fn [db [_ response-text]]
     (if (and response-text
              (:current-entry-area db))
       (do
     (println :open-url-callback response-text)
     (println (:current-entry-area db))
     (set! (.-value 
     (.getElementById
     js/document
     (:current-entry-area db)
     ))
           response-text)
     (assoc db :ajax-is-running false))
       ;; else
      db 
       )))

(register-handler :set-current-entry-area
   (fn [db [_ dom-id]]
     (assoc db :current-entry-area dom-id)))

(register-handler :open-link
   (fn [db [_ link]]
     (println ":open-link" link)
     (.open js/window link)
     db))

(register-handler :xhr-callback
   (fn [db [_ event]]
     (.log js/console "event is" event)
       (println ":xhr-callback, event is" event)
  (let [
        tgt (.-target event)
        response-text (.getResponseText tgt) ;;(.-target event))
        _ (when debug (prn "response-text is" response-text))
        results
        (-> response-text
            goog.json/parse
            (js->clj :keywordize-keys true)
            )
        my-map (if (:error results)
                 results
                 (update-in results [:composition]
                            keywordize-vector))
        _ (when debug (prn "my-map" my-map))
        {:keys [:links :composition :error]} my-map ;; destructure
        ]
    (when debug
    (log "in callback my-map" my-map)
    (log "in callback, links=" links))
    (assoc db
           :ajax-is-running
           false
           :composition
           composition
           :error
           error
           :links
           links
           :key-map
           (if (not (:error my-map))
             (key-map-for-composition composition)
             (:key-map db))
           )
    )
  ))

 (register-handler :generate-staff-notation-handler
     (fn [db [ _ response-text]]
       (when debug (println "in :generate-staff-notation-handler")
       (println "response-text=" response-text))
  (let [
        results
        (-> response-text
            goog.json/parse
            (js->clj :keywordize-keys true)
            )
        my-map (if (:error results)
                 results
                 (update-in results [:composition]
                            keywordize-vector))
        _ (when debug (println"my-map" my-map))
        _ (when debug (println "results=" results))
        {:keys [:links :composition :error]} my-map 
       ]
    (assoc db
           :ajax-is-running
           false
           :composition
           composition
           :error
           error
           :links
           links
           :key-map
           (if (not (:error my-map))
             (key-map-for-composition composition)
             (:key-map db))
           )
   )))
;; event handlers
(register-handler :generate-staff-notation
   (fn register-handler-aux[db _]
(println "in :generate-staff-notation")
  (if (not (:ajax-is-running db))
    (let [ query-data (new goog.Uri/QueryData) ]
      ;; TODO: try sending json
      (.set query-data "src"  (:doremi-text db))
      (.set query-data "kind"  (name (:composition-kind db)))
      (.set query-data "mp3"  true)
      (goog.net.XhrIo/send (:generate-staff-notation-url db)
            (fn[event]
              (println "in callback")
              (dispatch [:generate-staff-notation-handler
                       (.getResponseText (.-target event))]
                        ))
                           "POST"
                           query-data)
      (println "setting ajax-is-running true")
       (assoc db :ajax-is-running true) 
      )
    db)))



(register-handler :initialize 
                  (fn 
                    [db [_ initial-state]]
                    (merge db initial-state))) 
(register-handler :set-online-state
                  (fn [db [_ value]]
                    (assoc db :online value)))
(register-handler :set-render-as
                  (fn [db [_ value]]
                    (println "in set-render-as, value=" value)
                    (assoc db :render-as value)))

(register-handler :set-doremi-text
                  (fn [db [_ value]]
                    (assoc db :doremi-text value)))

(register-handler :set-composition-kind
                  (fn [db [_ value]]
                    (assoc db :composition-kind value)))



(register-handler :set-parser
   (fn [db [_ parser]]
     (assoc db :parser parser)
     ))

(register-handler :redraw-letter-notation
   (fn [db [_]]
  (let [results
        (-> (:doremi-text db)  
      (doremi-text->collapsed-parse-tree 
        (:parser db) 
        (:composition-kind db)))]
    (update-db-with-parse-results db results)
     )))

(register-handler :open-url
   (fn [db [_ url]]
     (println ":open-url" url)
  (goog.net.XhrIo/send url
                       (fn[event]
                         (let [raw-response (.-target event)
                               response-text (.getResponseText raw-response)
                               ]
              (dispatch [:open-url-callback response-text])))
                       "GET")
     (assoc db :ajax-is-running true)))


(register-handler :open-url-callback
   (fn [db [_ response-text]]
     (if (and response-text
              (:current-entry-area db))
       (do
     (println :open-url-callback response-text)
     (println (:current-entry-area db))
     (set! (.-value 
     (.getElementById
     js/document
     (:current-entry-area db)
     ))
           response-text)
     (assoc db :ajax-is-running false))
       ;; else
      db 
       )))

(register-handler :set-current-entry-area
   (fn [db [_ dom-id]]
     (assoc db :current-entry-area dom-id)))

(register-handler :open-link
   (fn [db [_ link]]
     (println ":open-link" link)
     (.open js/window link)
     db))

(register-handler :xhr-callback
   (fn [db [_ event]]
     (.log js/console "event is" event)
       (println ":xhr-callback, event is" event)
  (let [
        tgt (.-target event)
        response-text (.getResponseText tgt) ;;(.-target event))
        _ (when debug (prn "response-text is" response-text))
        results
        (-> response-text
            goog.json/parse
            (js->clj :keywordize-keys true)
            )
        my-map (if (:error results)
                 results
                 (update-in results [:composition]
                            keywordize-vector))
        _ (when debug (prn "my-map" my-map))
        {:keys [:links :composition :error]} my-map ;; destructure
        ]
    (when debug
    (log "in callback my-map" my-map)
    (log "in callback, links=" links))
    (assoc db
           :ajax-is-running
           false
           :composition
           composition
           :error
           error
           :links
           links
           :key-map
           (if (not (:error my-map))
             (key-map-for-composition composition)
             (:key-map db))
           )
    )
  ))

 (register-handler :generate-staff-notation-handler
     (fn [db [ _ response-text]]
       (when debug (println "in :generate-staff-notation-handler")
       (println "response-text=" response-text))
  (let [
        results
        (-> response-text
            goog.json/parse
            (js->clj :keywordize-keys true)
            )
        my-map (if (:error results)
                 results
                 (update-in results [:composition]
                            keywordize-vector))
        _ (when debug (println"my-map" my-map))
        _ (when debug (println "results=" results))
        {:keys [:links :composition :error]} my-map 
       ]
    (assoc db
           :ajax-is-running
           false
           :composition
           composition
           :error
           error
           :links
           links
           :key-map
           (if (not (:error my-map))
             (key-map-for-composition composition)
             (:key-map db))
           )
   )))
;; event handlers
(register-handler :generate-staff-notation
   (fn register-handler-aux[db _]
(println "in :generate-staff-notation")
  (if (not (:ajax-is-running db))
    (let [ query-data (new goog.Uri/QueryData) ]
      ;; TODO: try sending json
      (.set query-data "src"  (:doremi-text db))
      (.set query-data "kind"  (name (:composition-kind db)))
      (.set query-data "mp3"  true)
      (goog.net.XhrIo/send (:generate-staff-notation-url db)
            (fn[event]
              (println "in callback")
              (dispatch [:generate-staff-notation-handler
                       (.getResponseText (.-target event))]
                        ))
                           "POST"
                           query-data)
      (println "setting ajax-is-running true")
       (assoc db :ajax-is-running true) 
      )
    db)))



(register-handler :initialize 
                  (fn 
                    [db [_ initial-state]]
                    (merge db initial-state))) 
(register-handler :set-online-state
                  (fn [db [_ value]]
                    (assoc db :online value)))
(register-handler :set-render-as
                  (fn [db [_ value]]
                    (println "in set-render-as, value=" value)
                    (assoc db :render-as value)))

(register-handler :set-doremi-text
                  (fn [db [_ value]]
                    (assoc db :doremi-text value)))

(register-handler :set-composition-kind
                  (fn [db [_ value]]
                    (assoc db :composition-kind value)))

