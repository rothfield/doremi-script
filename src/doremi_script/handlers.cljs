(ns doremi-script.handlers
  (:require-macros 
    [cljs.core.async.macros :refer [go]]
    [reagent.ratom :refer [reaction]]
    )
  (:require 
    [doremi-script.check-network :refer [start-check-network]]
    [quile.component :as component]
    [doremi-script.to-lilypond :refer [to-lilypond]]
    [doremi-script.dom-utils :refer [listen seconds by-id production?]]
    [doremi-script.sargam-key-map :refer
     [default-key-map mode-and-notes-used->key-map ]]
    [doremi-script.core :refer [
                                new-parser
                                doremi-text->collapsed-parse-tree]]
    [doremi-script.utils :refer [get-attributes keywordize-vector 
                                 log]]

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
(enable-console-print!)

(defn key-map-for-composition[composition]
  (let [
        attributes (if composition (get-attributes composition) {})
        notes-used (:notesused attributes)
        mode-a (:mode attributes)
        mode (when mode-a
               (-> mode-a lower-case keyword))
        new-key-map (mode-and-notes-used->key-map mode notes-used)
        ]
    (when debug (println "attributes=" attributes "new-key-map=" new-key-map))
    new-key-map
    ))

(defn update-db-with-parse-results[ db {:keys [:composition :error] :as results}]
  (let [new-key-map (key-map-for-composition composition)]
    (when debug (println "new-key-map: " new-key-map))
    (merge db 
           results
           (if error
             {}
             {:key-map new-key-map}))))

;; takes 30 seconds to load unserialized grammar. 1 second for serialized
(def serialized-grammar-path "ebnf/grammar.txt") 

(def unserialized-grammar-path "ebnf/doremiscript.ebnf")

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


;;;; *******************IMPORTANT****************
;;;; When the doremi-script grammar changes,
;;;; add it to doremi-script
;;;; Then run the following code and save the output in doremi-script/resources
;;;; see serialized-grammar-path and unserialized-grammar-path
(defn print-out-grammar[]
  (println "Save this in resources/ebnf.txt")
  (go
    (let [parser (component/start (new-parser (<! (load-grammar-xhr))))]
      ;; use this to create ebnf.txt file
      (binding [*print-dup* true] 
        (prn (:grammar (:parser parser)) 
             )))))

(defn parse-xhr-callback[response-text]
  (when debug (.log js/console "parse-xhr-callback, response-text is " response-text))
  (dispatch [:generate-staff-notation-handler response-text :parse-xhr-is-running]))

(defn parse-xhr[url {src :src kind :kind}]
  (dispatch [:set-parse-xhr-is-running true]) 
  (when debug
    (println "entering parse-xhr:"  "url=" url " src= " src "\nkind=" kind))
  (let [out (chan)
        query-data (new goog.Uri/QueryData) ]
    (.set query-data "src"  src)
    (.set query-data "kind" (name  kind))
    (goog.net.XhrIo/send
      url
      (fn[event]
        (let [raw-response (.-target event)
              response-text (.getResponseText raw-response)
              ]
          (put! out response-text)))

      ;;  (fn [event] (put! out event))
      "POST"
      query-data)
    out 
    ))  

(def PARSE-URL
  ;; TODO dry
  (if production?
    "http://ragapedia.com/doremi-server/parse"
    "http://localhost:4000/doremi-server/parse")
  )

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

(defn start-parse-timer[dom-id]
  ;; TODO: use parse-local if offline...
  (when debug (println "start-parse-timer " dom-id))
  (let [
        composition-kind (subscribe [:composition-kind])
        last-value (atom "") 
        keypresses (listen (by-id dom-id) "keypress")]
    (go (while true
          (<! keypresses)
          (let [cur-value 
                (.-value (by-id dom-id))
                ]
            (when (not= cur-value @last-value) 
              (reset! last-value cur-value)
              (let [ response-text (<! 
                                     (parse-xhr PARSE-URL {:src cur-value 
                                                           :kind @composition-kind
                                                           }))
                    ]
                (parse-xhr-callback response-text)
                (<! (timeout (* 6 seconds))) 
                )
              ))
          ))))
(register-handler :set-ajax-is-running
                  (fn [db [_ b]]
                    (assoc db :ajax-is-running b)))

(register-handler :set-parse-xhr-is-running
                  (fn [db [_ b]]
                    (assoc db :parse-xhr-is-running b)))

(register-handler :print-grammar
                  (fn [db [_ dom-id]]
                    (print-out-grammar)
                    db
                    ))
(register-handler :start-parse-timer
                  (fn [db [_ dom-id]]
                    (start-parse-timer dom-id)
                    db
                    ))


(register-handler :start-check-network
                  (fn [db [_ _]]
                    (start-check-network)
                    db
                    ))
(register-handler :load-parser
                  (fn [db [_ url]]
                    (go
                      (dispatch [:set-parser 
                                 (component/start (new-parser
                                                    (<! (load-serialized-grammar-xhr))))
                                 ]) 
                      )
                    ;;  (assoc db :parser parser)
                    db
                    ))

(register-handler :set-parser
                  (fn [db [_ parser]]
                    (assoc db :parser parser)
                    ))

(register-handler :redraw-lilypond-source
                  (fn [db [_]]
                    (let [results
                          (-> (:doremi-text db)  
                              (doremi-text->collapsed-parse-tree 
                                (:parser db) 
                                (:composition-kind db)))
                           error (:error results) 
                           _ (println "results=" results)
                           lilypond-source (to-lilypond (:composition results) (:doremi-text db))
                          ]
                       (assoc db :lilypond-source 
                              (if error "parse error, can't generate lilypond source"
                                lilypond-source))
                      )))


(register-handler :redraw-letter-notation
                  ;; TODO: redraw on server unless offline ???????or composition gets too big ????
                  (fn [db [_]]
                    (when debug ":redraw-letter-notation")
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
                  (fn [db [ _ response-text which]]
                    ;;; which is :ajax-is-running or :parse-xhr-is-running
                    ;;; parse-xhr doesn't generate links.
                    ;;; TODO: rename which and rename this handler.
                    (println "in :generate-staff-notation-handler, which is" which)
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
                             which
                             false
                             ;; Following is awkward code. 
                             ;; only update links on generate xhr. not parse-xhr 
                             :links
                             (if (not (nil? links)) links (:links db))  
                             :composition
                             composition
                             :error
                             error
                             :key-map
                             (if (not (:error my-map))
                               (key-map-for-composition composition)
                               (:key-map db))
                             )
                      )))
;; event handlers
(register-handler :generate-staff-notation
                  (fn register-handler-aux[db _]
                    (when debug (println "in :generate-staff-notation"))
                    (if (not (:ajax-is-running db))
                      (let [ query-data (new goog.Uri/QueryData) ]
                        ;; TODO: try sending json
                        (.set query-data "src"  (:doremi-text db))
                        (.set query-data "kind"  (name (:composition-kind db)))
                        (.set query-data "mp3"  true)
                        (goog.net.XhrIo/send (:generate-staff-notation-url db)
                                             (fn[event]
                                               (when debug (println "in callback"))
                                               (dispatch [:generate-staff-notation-handler
                                                          (.getResponseText (.-target event))
                                                          :ajax-is-running 
                                                          ]
                                                         ))
                                             "POST"
                                             query-data)
                        (assoc db :ajax-is-running true)
                        )
                      db)))

;;;;          fallback_if_utf8_characters_not_supported = function(context) 
;;;;          {
;;;;               var tag, width1, width2;
;;;;               if (context == null) {
;;;;               context = null;
;;;;               }
;;;;               if (!(window.ok_to_use_utf8_music_characters != null)) {
;;;;               width1 = $('#utf_left_repeat').show().width();
;;;;               width2 = $('#utf_single_barline').show().width();
;;;;               $('#utf_left_repeat').hide();
;;;;               $('#utf_single_barline').hide();
;;;;               window.ok_to_use_utf8_music_characters = width1 !== width2;
;;;;               }
;;;;               if (!window.ok_to_use_utf8_music_characters) {
;;;;               tag = "data-fallback-if-no-utf8-chars";
;;;;               $("span[" + tag + "]", context).addClass('dont_use_utf8_chars');
;;;;               return $("span[" + tag + "]", context).each(function(index) {
;;;;               var attr, obj;
;;;;               obj = $(this);
;;;;               attr = obj.attr(tag);
;;;;               return obj.html(attr);
;;;;               });
;;;;               }
;;;;               };

(register-handler :check-utf-support
                  (fn 
                    [db [_ dom-id]]
                    (let [
                          _ (println "check-utf-support")
                          item1 (sel1 "#utf_left_repeat")
                          item2 (sel1 "#utf_single_barline")
                          _ (dommy/set-style! item1 :display "inline-block")
                          _ (dommy/set-style! item2 :display "inline-block")
                          width1 (dommy/px item1 :width)
                          width2 (dommy/px item2 :width)
                          new-db
                          (assoc db 
                                 :supports-utf8-characters
                                 (not= 
                                   (dommy/px item1 :width) 
                                   (dommy/px item2 :width)) 
                                 )

                          ]
                      (when debug (println ":check-utf-support handler, width1, width2= " width1 width2))
                      (dommy/set-style! item1 :display "none")
                      (dommy/set-style! item2 :display "none")
                      db
                      )))

(defn db-was-initialized?[db]
  (:the-parser db)
  )

(register-handler :initialize 
                  ;; implemented so it will only run once
                  (fn 
                    [db [_ initial-state]]
                    (if (db-was-initialized? db)
                      db
                      (merge db initial-state)
                      ))) 

(register-handler :set-online-state
                  (fn [db [_ value]]
                    (assoc db :online value)))
;; (assoc db :online true )))
(register-handler :set-render-as
                  (fn [db [_ value]]
                    (when debug (println "in set-render-as, value=" value))
                    (assoc db :render-as value)))

(register-handler :set-doremi-text
                  (fn [db [_ value]]
                    (assoc db :doremi-text value)))

(register-handler :set-composition-kind
                  (fn [db [_ value]]
                    (assoc db :composition-kind value)))

