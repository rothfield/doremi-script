(ns doremi-script.app
  ;;(:require-macros 
  (:require-macros 
    ;;               [cljs.core :refer [assert]]
    [cljs.core.async.macros :refer [go]]
    [reagent.ratom :refer [reaction]]
    )
  (:require 
    [quile.component :as component]
    [doremi-script.handlers]
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

(enable-console-print!)

(def seconds 1000)

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


(defn by-id [id]
  (.getElementById js/document (name id)))

(defn listen [el event-type]
  (let [out (chan)]
    (events/listen el event-type
                   (fn [e] (put! out e)))
    out))

(defn key-map-for-composition[composition]
  (let [
        attributes (if composition (get-attributes composition) {})
        notes-used (:notesused attributes)
        mode (:mode attributes)
        ]
    (mode-and-notes-used->key-map mode notes-used)
    ))

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


;; subscriptions
(register-sub :links
              (fn [db _]  
                (reaction (:links @db))))

(register-sub :mp3-url
              (fn [db _]  
                (reaction (get-in @db [:links :mp3-url]))))

(register-sub :staff-notation-url
              (fn [db _]  
                (reaction (get-in @db [:links :staff-notation-url]))))

(register-sub :ajax-is-running
              (fn [db _]  
                (reaction (:ajax-is-running @db))))
(register-sub :online
              (fn [db _]  
                (reaction (:online @db))))

(register-sub :doremi-text
              (fn [db _]  
                (reaction (:doremi-text @db))))

(register-sub :composition
              (fn [db _]
                (reaction (:composition @db))))

(register-sub :composition-kind
              (fn [db _]
                (reaction (:composition-kind @db))))

(register-sub :current-entry-area
              (fn [db _]
                (reaction (:current-entry-area @db))))
(register-sub :render-as
              (fn [db _]
                (reaction (:render-as @db))))
(register-sub :error
              (fn [db _]
                (reaction (:error @db))))
(register-sub :parser
              (fn [db _]
                (reaction (:parser @db))))

(register-sub :key-map
              (fn [db _]
                (reaction (:key-map @db))))

(declare key-map-for-composition)

(defn update-db-with-parse-results[ db {:keys [:composition :error] :as results}]
  (merge db results
         (if error
           {}
           {:key-map (key-map-for-composition composition)})))

(comment register-handler :set-parser
   (fn [db [_ parser]]
     (assoc db :parser parser)
     ))

(comment register-handler :redraw-letter-notation
   (fn [db [_]]
  (let [results
        (-> (:doremi-text db)  
      (doremi-text->collapsed-parse-tree 
        (:parser db) 
        (:composition-kind db)))]
    (update-db-with-parse-results db results)
     )))

(comment register-handler :open-url
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


(comment register-handler :open-url-callback
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

(comment register-handler :set-current-entry-area
   (fn [db [_ dom-id]]
     (assoc db :current-entry-area dom-id)))

(comment register-handler :open-link
   (fn [db [_ link]]
     (println ":open-link" link)
     (.open js/window link)
     db))

(comment register-handler :xhr-callback
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

 (comment register-handler :generate-staff-notation-handler
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
(comment register-handler :generate-staff-notation
   (fn comment register-handler-aux[db _]
(println "in :generate-staff-notation")
  (if (not (:ajax-is-running db))
    (let [ query-data (new goog.Uri/QueryData) ]
      ;; TODO: try sending json
      (.set query-data "src"  (:doremi-text db))
      (.set query-data "kind"  (name (:composition-kind db)))
      (.set query-data "mp3"  true)
      (goog.net.XhrIo/send GENERATE-STAFF-NOTATION-URL
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



(comment register-handler :initialize 
                  (fn 
                    [db _]
                    (merge db initial-state))) 
(comment register-handler :set-online-state
                  (fn [db [_ value]]
                    (assoc db :online value)))
(comment register-handler :set-render-as
                  (fn [db [_ value]]
                    (println "in set-render-as, value=" value)
                    (assoc db :render-as value)))

(comment register-handler :set-doremi-text
                  (fn [db [_ value]]
                    (assoc db :doremi-text value)))

(comment register-handler :set-composition-kind
                  (fn [db [_ value]]
                    (assoc db :composition-kind value)))






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


(go (while true
      (check-network)
      (<! (timeout (* 300 seconds))) 
      ))



(defonce printing (reagent.core/atom false))

(declare draw-item) ;; need forward reference since it is recursive


(defn css-class-name-for[x]
  (string/replace (name x) "-" "_")
  )




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





(def class-for-octave
  {nil "octave0"
   0 "octave0"
   -1 "lower_octave_1"
   -2 "lower_octave_2"
   -3 "lower_octave_3"
   -4 "lower_octave_4"
   1 "upper_octave_1 upper_octave_indicator"
   2 "upper_octave_2 upper_octave_indicator"
   3 "upper_octave_3 upper_octave_indicator"
   4 "upper_octave_4 upper_octave_indicator"
   5 "upper_octave_5 upper_octave_indicator"
   }
  )
(def class-for-ornament-octave
  {nil "octave0"
   0 "octave0"
   -1 "lower_octave_1"
   -2 "lower_octave_2"
   -3 "lower_octave_3"
   -4 "lower_octave_4"
   1 "upper_octave_1"
   2 "upper_octave_2"
   3 "upper_octave_3"
   4 "upper_octave_4"
   5 "upper_octave_5"
   }
  )



;;     . The Unicode character ♭(U+266D) is the flat sign. Its HTML entity is &#9837;.
;;    In Unicode, the sharp symbol (♯) is at code point U+266F. Its HTML entity is &#9839;. The symbol for double sharp (double sharp) is at U+1D12A (so &#119082;). These characters may not display correctly in all fonts.

(def bullet "&bull;")
(def sharp-symbol "&#9839;")
(def flat-symbol  "&#9837;")
(def lookup_simple {
                    "b" "b"
                    "#" "#"
                    "." "&bull;"
                    "*" "&bull;"
                    "|:" "|:"
                    "~" "~"
                    ":|" ":|"
                    "|" "|"
                    "||" "||"
                    "%" "%"
                    "|]" "|"
                    "[|" "|"
                    })

(def lookup_html_entity {
                         "b" "&#9837;"
                         "#" "&#9839;"
                         "." "&bull;"
                         "*" "&bull;"
                         "|:" "&#x1d106"
                         "~" "&#x1D19D&#x1D19D"
                         ":|" "&#x1d107"
                         "|" "&#x1d100"
                         "||" "&#x1d101"
                         "%" "&#x1d10E"
                         "|]" "&#x1d102"
                         "[|" "&#x1d103"
                         })
(def lookup-sargam {
                    "Cb" ["S", flat-symbol]
                    "C" ["S"]
                    "C#" ["S", sharp-symbol]
                    "Db" ["r"]
                    "D" ["R"]
                    "D#" ["R", sharp-symbol]
                    "Eb" ["g"]
                    "E" ["G"]
                    "E#" ["G", sharp-symbol]
                    "F" ["m"]
                    "F#" ["M"]
                    "Gb" ["P", flat-symbol]
                    "G" ["P"]
                    "G#" ["P", sharp-symbol]
                    "Ab" ["d"]
                    "A" ["D"]
                    "A#" ["D", sharp-symbol]
                    "Bb" ["n"]
                    "B" ["N"]
                    "B#" ["N", sharp-symbol]
                    })

(def lookup-number  {
                     "Cb" ["1", flat-symbol]
                     "C" ["1"]
                     "C#" ["1", sharp-symbol]
                     "Db" ["2", flat-symbol]
                     "D" ["2"]
                     "D#" ["2", sharp-symbol]
                     "Eb" ["3", flat-symbol]
                     "E" ["3"]
                     "E#" ["3", sharp-symbol]
                     "F" ["4"]
                     "F#" ["4", sharp-symbol]
                     "Gb" ["5", flat-symbol]
                     "G" ["5"]
                     "G#" ["5", sharp-symbol]
                     "Ab" ["6", flat-symbol]
                     "A" ["6"]
                     "A#" ["6", sharp-symbol]
                     "Bb" ["7", flat-symbol]
                     "B" ["7"]
                     "B#" ["7", sharp-symbol]
                     })
(def lookup-hindi 
  ;; http://symbolcodes.tlt.psu.edu/bylanguage/devanagarichart.html
  (let [s "&#2360;" ;; "र"
        r "&#2352;" ;;  र
        g "&#2327;";; "ग़"
        m "&#2350;" ;; "म"
        p "&#2346;" ;; "प"
        d "&#2343" ;;"ध"
        n "&#2344;" ;; "ऩ"
        tick  "'"]
    {
     "Cb" [s  flat-symbol]
     "C" [s]
     "C#"[s  sharp-symbol]
     "Db" [r]
     "D" [r]
     "D#" [r sharp-symbol]
     "Eb" [g]
     "E" [g]
     "E#" [g  sharp-symbol]
     "F" [m]
     "F#" [m tick]
     "Gb" [p  flat-symbol]
     "G" [p]
     "G#" [p sharp-symbol]
     "Ab" [d]
     "A" [d]
     "A#" [d  sharp-symbol]
     "Bb" [n]
     "B" [n]
     "B#" [n  sharp-symbol]
     }))

(def lookup-ABC {
                 "Cb" ["C", flat-symbol]
                 "C" ["C"]
                 "C#" ["C", sharp-symbol]
                 "Db" ["D", flat-symbol]
                 "D" ["D"]
                 "D#" ["D", sharp-symbol]
                 "Eb" ["E", flat-symbol]
                 "E" ["E"]
                 "E#" ["E", sharp-symbol]
                 "F" ["F"]
                 "F#" ["F", sharp-symbol]
                 "Gb" ["G", flat-symbol]
                 "G" ["G"]
                 "G#" ["G", sharp-symbol]
                 "Ab" ["A", flat-symbol]
                 "A" ["A"]
                 "A#" ["A", sharp-symbol]
                 "Bb" ["B", flat-symbol]
                 "B" ["B"]
                 "B#" ["B", sharp-symbol]
                 })

(def lookup-DoReMi {
                    "Cb" ["D", flat-symbol]
                    "C" ["D"]
                    "C#" ["D", sharp-symbol]
                    "Db" ["R", flat-symbol]
                    "D" ["R"]
                    "D#" ["R", sharp-symbol]
                    "Eb" ["M", flat-symbol]
                    "E" ["M"]
                    "E#" ["M", sharp-symbol]
                    "F" ["F"]
                    "F#" ["F", sharp-symbol]
                    "Gb" ["S", flat-symbol]
                    "G" ["S"]
                    "G#" ["S", sharp-symbol]
                    "Ab" ["L", flat-symbol]
                    "A" ["L"]
                    "A#" ["L", sharp-symbol]
                    "Bb" ["T", flat-symbol]
                    "B" ["T"]
                    "B#" ["T", sharp-symbol]
                    })

;; not sure if using multi-methods is better than a case statement
(defmulti deconstruct-pitch-string-by-kind (fn [pitch kind] kind))

(defmethod deconstruct-pitch-string-by-kind :sargam-composition [pitch kind]
  (get lookup-sargam pitch))

(defmethod deconstruct-pitch-string-by-kind :number-composition [pitch kind]
  (get lookup-number pitch))

(defmethod deconstruct-pitch-string-by-kind :abc-composition [pitch kind]
  (get lookup-ABC pitch))

(defmethod deconstruct-pitch-string-by-kind :doremi-composition [pitch kind]
  (get lookup-DoReMi pitch))

(defmethod deconstruct-pitch-string-by-kind :hindi-composition [pitch kind]
  (get lookup-hindi pitch))

(defmethod deconstruct-pitch-string-by-kind :default [pitch _]
  (get lookup-sargam pitch))


(def mordent-entity "&#x1D19D&#x1D19D")

(def lookup-barline
  {
   :single-barline "&#x1d100"
   :double-barline "&#x1d101"
   :left-repeat "&#x1d106"
   :mordent "&#x1D19D&#x1D19D"
   :right-repeat "&#x1d107"
   :final-barline "&#x1d102"
   :reverse-final-barline "&#x1d103"
   }
  )




(defn display-parse-to-user-box []
  (let [error (subscribe [:error])
        composition (subscribe [:composition])
        ]
    [:div.form-group.hidden-print
     {
      :class (if @error "has-error" "") 
      }
     [:label.control-label {:for "parse-results"
                            } "Parse Results:"]
     [:textarea#parse-results.form-control 
      {:rows "3" 
       :spellCheck false
       :readOnly true
       :value 
       (if @error
         (print-str @error)
         (print-str @composition))
       }
      ]]))


(def text-area-placeholder
  "Select notation system from \"Enter notation as\" select box.
  Enter letter music notation as follows:
  For number notation use:    | 1234 567- | 
  For abc notation use:   | CDEF F#GABC |
  For sargam use:  | SrRg GmMP dDnN ---- |
  For devanagri/bhatkande use:   स र ग़ म म' प ध ऩ
  For doremi use: drmf slt-
  Use dots above/below notes for octave indicators."
  )

(defn get-selection[dom-node]
  ;; TODO: goog equivalent ???
  {:start (.-selectionStart dom-node)
   :end (.-selectionEnd dom-node)
   })

(defn my-contains?[x y]
  (not= -1 (.indexOf x y))
  )

(defn within-sargam-line?[txt idx]
  ;; TODO: Create function (defn get-current-line[txt idx])
  (comment "within-sargam-line?, txt,idx,class(text)" txt idx )
  (let [ left (.substring txt 0 idx) 
        right (.substring txt idx)
        x (.indexOf right "\n")
        index-of-right-newline (if (not= -1 x)
                                 (+ idx x)
                                 (.-length txt)
                                 )
        y (.lastIndexOf left "\n")
        index-of-left-newline (if (not= -1 y)
                                (inc y)
                                0) 
        _ (comment "index-of-left-newline=" index-of-left-newline)
        line (.substring txt index-of-left-newline 
                         index-of-right-newline)
        _ (comment "line is" line)
        ]
    (comment "left right index-of-right-newline" left right index-of-right-newline)
    (comment "line is" line)
    (my-contains? line "|")))


(defn on-key-press-new[event my-key-map composition-kind]
  ;; event is a dom event
  (if (not= :sargam-composition composition-kind)
    true
    (let [
          target (.-target event)
          key-code (.-keyCode event)
          ctrl-key? (or (.-ctrlKey event)
                        (.-altKey event)
                        (.-metaKey event))
          from-char-code-fn (.-fromCharCode js/String)
          ch (from-char-code-fn key-code)
          new-char (if-not ctrl-key?  (get my-key-map ch ))
          caret-pos (.-selectionStart target)
          text-area-text (.-value target)
          selection (get-selection target)
          my-within-sargam-line (within-sargam-line? text-area-text (:start selection))
          ]
      ;;; nativeEvent looks like  {which: 189, keyCode: 189, charCode: 0, repeat: false, metaKey: false…}
      (if (and my-within-sargam-line
               new-char)
        (do
          (set! (.-value target)
                (str (.substring text-area-text 0 caret-pos) 
                     new-char 
                     (.substring text-area-text caret-pos)))
          (set! (.-selectionStart target)
                (inc (:start selection)))
          (set! (.-selectionEnd target)
                (inc (:end selection)))
          false
          )
        true
        )
      )))


(defn parse-button[]
  [:button.btn.btn-primary
   {
    :title "Redraw Letter Notation",
    :name "redraw_letter_notation"
    :on-click 
    (fn [e]
      (stop-default-action e)
         (dispatch [:redraw-letter-notation]) ;; include dom-id as param ??
      )
    }
   "Redraw"
   ] 
  )

(comment
  (defn color-input
    []
    (let [doremi-text (subscribe [:doremi-text])]
      (fn color-input-render
        []
        [:div.color-input
         "Time color: "
         [:input {:type "text"
                  :value @time-color
                  :on-change #(dispatch [:time-color (-> % .-target .-value)])}]])))
  )


;; "form-3" component see 
;; https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components
(defn entry-area-input[]  
  ;; textarea input keypresses is not handled by reagent
  (let [ dom-id "area2"
        dom-id-key (keyword (str "#" dom-id))
        online (subscribe [:online])
        composition-kind (subscribe [:composition-kind])
        key-map (subscribe [:key-map])
        ]   
    (reagent.core/create-class    
      {:component-did-mount       
       (fn entry-area-input-did-mount[this]
         (dispatch [:set-current-entry-area dom-id])
         (set! (.-onkeypress 
                 (sel1 dom-id-key)
                 )
               (fn my-on-key-press[event]
                 (on-key-press-new event @key-map @composition-kind ))))
       :display-name  "entry-area-input" 
       :reagent-render
       (fn []  ;; remember to repeat parameters
         [:div.form-group.hidden-print
          [:label {:for "entryArea"} "Enter Letter Notation Source:"]
          (if (not @online)
            [:span.offline "You are working offline. Features such as generating staff notation are disabled" ]
            )
          [:textarea.entryArea.form-control
           {
            :autofocus true
            :placeholder text-area-placeholder
            :id "area2"
            :name "src",
            :spellCheck false
            ;; :onKeyPress - for handling key strokes see above

            :on-change 
            (fn on-change-text-area[event]
              (dispatch [:set-doremi-text (-> event .-target .-value)]))
            }
           ]]
         )})))


(defn draw-children[items]
  (doall (map-indexed
           (fn notes-line-aux[idx item]
             (draw-item item idx))
           items)))

(defn staff-notation[]
  (let [staff-notation-url (subscribe [:staff-notation-url])]
  [:img#staff_notation.hidden-print 
   {:class (if @printing "printing" "")
    :src @staff-notation-url
    }]))

(defn html-rendered-composition[]
  (let [composition (subscribe [:composition])] 

    (if (not @composition)
      [:div#doremiContent.composition.doremiContent ]
      ;; else
      [:div#doremiContent.composition.doremiContent 
       {:class (if @printing "printing" "")}
       (draw-children (rest @composition))]
      )))






(defn attribute-section[{item :item}]
  nil
  )



(defn notes-line [{item :item}]
  (log "notes-line, item is")
  (log item)
  (assert (is? :notes-line item))
  [:div.stave.sargam_line
   (draw-children (rest item))])
;; TODO
;;; componentDidMount: function () { window.dom_fixes($(this.getDOMNode())); },
;;   componentDidUpdate: function () { window.dom_fixes($(this.getDOMNode())); },
;; var items = rest(item);
;;

(defn user-entry[]
  (.-value (dom/getElement "area2"))
  )

(defn parse-xhr[url {txt :txt kind :kind }]
  (println "parse-xhr stub")
  (chan)
  ;; TODO: review old code
  )

(defn start-parse-timer[]
  (let [
        composition-kind (subscribe [:composition-kind])
        last-value (atom "") 
        keypresses (listen (dom/getElement "area2") "keypress")]
    (go (while true
          (<! keypresses)
          (let [cur-value (user-entry)]
            (when (not= cur-value @last-value) 
              (reset! last-value cur-value)
              (let [ results (<! 
                               (parse-xhr PARSE-URL {:src cur-value 
                                                     :kind @composition-kind
                                                     }))
                    ]
                (<! (timeout (* 6 seconds))) 
                )
              ))
          ))))


;;;;  add-right-margin-to-notes-with-pitch-signs = function(context) {
;;;;    if (context == null) {
;;;;      context = null;
;;;;    }
;;;;    return $('span.note_wrapper *.pitch_sign', context).each(function(index) {
;;;;      var current_margin_right, parent;
;;;;      parent = $(this).parent();
;;;;      current_margin_right = parseInt($(parent).css('margin-right').replace('px', ''));
;;;;      return $(parent).css('margin-right', $(this).width());
;;;;    });
;;;;  };


(defn add-right-margin-to-notes-with-pitch-signs[context] 
  (let [items (sel "span.note_wrapper *.pitch_sign") ]
    (dorun (map (fn[item]
                  (let
                    [parent (dommy/parent item)]
                    (dommy/set-style! (dommy/parent item) 
                                      :margin-right
                                      (str (dommy/px item :width) "px")
                                      )
                    )) items))))


(defn add-left-margin-to-notes-with-left-superscripts[]
  ;; TODO: Raise height of ornament if it is over a barline!!!
  (let [items (sel "span.note_wrapper *.ornament.placement_before")
        ]
    (dorun (map
             (fn[item]
               (let
                 [parent (dommy/parent item)]
                 (dommy/set-style! item 
                                   :margin-left
                                   (str (* -1 
                                           (dommy/px item :width)) "px"))
                 (dommy/set-style! parent 
                                   :margin-left
                                   (str (*  
                                          (dommy/px item :width)) "px"))
                 )) items))
    ))

(defn add-right-margin-to-notes-with-right-superscripts[]
  ;; Not sure why it adjusts note_wrapper and not pitch??
  (log "add-right-margin-to-notes-with-right-superscripts")
  (let [items (sel "span.note_wrapper *.ornament.placement_after") ]
    (dorun (map
             (fn[item]
               (let [parent (dommy/parent item)
                     width (dommy/px item :width)
                     ]            
                 (dommy/set-style! (dommy/parent item)
                                   :margin-right
                                   (str width "px"))
                 )) items))
    ))



(defn adjust-slurs-in-dom[context]
  (comment "html looks like"
           [:span.measure
            [:span.beat.looped
             [:span.note_wrapper ]]
            [:span#0.slur
             ]
            [:span.note.pitch "S"]
            [:span.note_wrapper
             {:data-begin-slur-id "0"}]
            [:span.note.pitch "R"]])
  (let [items (sel "span[data-begin-slur-id]")]
    (dorun 
      (map (fn[item]
             (let
               [ dom-id (dommy/attr item :data-begin-slur-id)
                slur (.getElementById js/document dom-id)
                rect1 (dommy/bounding-client-rect item)
                rect2 (when slur (dommy/bounding-client-rect slur))
                width (when slur (- (:right rect1) (:left rect2)))
                ]
               (log "adjust-slurs-in-dom" dom-id slur rect1 rect2 width)
               (when slur
                 (dommy/set-style! slur :width
                                   (str width "px"))
                 ))) items))))

(defn fallback-if-utf8-characters-not-supported[context]
  ;;; TODO
  ;;; See doremi.coffee from previous version
  )



(defn expand-note-widths-to-accomodate-syllables[context]
  (let [ items  (sel :.syl)]
    (dorun (map-indexed 
             (fn[idx item]
               (when-not (= idx (dec (count items))) ;; omit last syllable on line
                 (let
                   [
                    syl (dommy/text item)  ;; Move to react component css. hyphen
                    ends-word (not= (last syl) "-")
                    extra (if ends-word 5 0)
                    next-item (get items (inc idx)) 
                    pitch (some (fn[x] (when (not= -1 (.indexOf (dommy/class x) "pitch")) x))
                                (-> item dommy/parent dommy/children array-seq) )
                    rect1 (dommy/bounding-client-rect item)
                    rect2 (dommy/bounding-client-rect next-item)
                    ] 
                   (when false  (comment "pitch" pitch) (comment "next-item" next-item) (comment "rect1" rect1) (comment "rect2" rect2))
                   (when 
                     (and (= (:top rect1) (:top rect2))
                          (> (+ extra (:right rect1))
                             (:left rect2)))
                     ;;($note.css("margin-right", "" + (existing_margin_right     + syl_right - next_left + extra + extra2) + "px"));
                     (dommy/set-style! pitch :margin-right 
                                       (str (+ extra
                                               (- (:right rect1)
                                                  (:left rect2)
                                                  )) "px")) 
                     ))))
             items
             ))))

(defn dom-fixes[this]
  (expand-note-widths-to-accomodate-syllables this)
  (add-right-margin-to-notes-with-right-superscripts)
  (add-left-margin-to-notes-with-left-superscripts)
  (add-right-margin-to-notes-with-pitch-signs this)
  (adjust-slurs-in-dom this)
  (fallback-if-utf8-characters-not-supported this)
  )

(def composition-wrapper 
  (with-meta html-rendered-composition
             {:component-did-mount
              (fn[this]
                (log "component-did-mount composition to call dom_fixes")
                (dom-fixes this)
                )

              :component-did-update
              (fn[this]
                (log "component-did-update composition-about to call dom_fixes")
                (dom-fixes this)
                ) 
              }
             ))

(defn composition-box[]
  [:div
   [:ul.nav.nav-justified
    [:li
     [:label.hidden-print {:for "entryArea"} "Rendered Letter Notation: "]
     ]
    [:li
     [parse-button]  
     ]
    ]
   [composition-wrapper]
   ]
  )

(defn ornament-pitch[{item :item
                      render-as :render-as}]
  ;; item looks like:
  ;; ;; ["ornament",["ornament-pitch","B",["octave",1]]
  ;; [:span.ornament_item.upper_octave_1 "g"]
  (log "entering ornament-pitch") 
  (log item)
  (let [
        deconstructed-pitch ;; C#,sargam -> ["S" "#"] 
        (deconstruct-pitch-string-by-kind (second item)
                                          render-as
                                          ) 
        octave (some #(when (and (vector %)
                                 (= :octave (first %)))
                        (second %)) 
                     (rest item))
        alteration-string (second deconstructed-pitch)
        pitch-src (join deconstructed-pitch)
        octave_class (get class-for-ornament-octave octave)
        ]
    [:span.ornament_item 
     {:class octave_class
      :dangerouslySetInnerHTML {
                                :__html pitch-src
                                } 
      }
     ]
    ))

(defn ornament[{item :item}]
  ;; should generate something like this:
  (comment
    [:span.upper_attribute.ornament.placement_after
     [:span.ornament_item.upper_octave_1
      "g"]])
  (let [render-as (subscribe [:render-as])
        items (rest item)
        filtered (filter #(and (vector? %)
                               (= :ornament-pitch (first %))) items)
        _ (log "filtered " filtered)  

        placement (last item)
        placement-class (str "placement_" (name placement))
        ]
    [:span.upper_attribute.ornament {:class placement-class}
     (doall (map-indexed
              (fn notes-line-aux[idx item]
                [ornament-pitch {:item item
                                 :render-as @render-as
                                 :key idx
                                 }
                 ]) filtered)) 
     ] 
    ))




(defn mordent[{item :item}]
  [:span.mordent
   {:dangerouslySetInnerHTML 
    { :__html mordent-entity }
    }]) 

(defn ending[{item :item}]
  [:span.ending
   (second item)
   ])

(defn line-number[{item :item}]
  [:span.note_wrapper 
   [:span.note.line_number 
    (str (second item) ")")
    ]
   ])

(defn line-item 
  [{src :src kind :kind item :item}]
  (log "entering line-item, item")
  (log item)
  ;; className = item[0].replace(/-/g, '_');
  ;;src = "S" ;;; this.props.src;
  [:span {:class "note_wrapper" } 
   [:span.note {:class kind} ;; TODO: should use css-class-name-for ???
    src]])

(defn barline[{src :src item :item}]
  (let [barline-name (first (second item))]
    (log "barline-name is" barline-name)
    [:span.note_wrapper
     [:span.note.barline 
      {:dangerouslySetInnerHTML 
       { :__html 
        (get lookup-barline (keyword (first (second item))))
        }
       }
      ]]))


(defn beat[{item :item}]
  (log "entering beat")
  (assert (is? :beat item))
  (log "beat, item is")
  (log item)
  (let [beat-count 
        (reduce (fn count-beats[accum cur]
                  (log "cur is" cur) 
                  (if (and (vector? cur)
                           (get #{:pitch :dash} (first cur)))
                    (inc accum)
                    accum))
                0 (rest item))
        _ (log "beat-count is" beat-count) 
        looped (if (> beat-count 1) "looped" "")
        ]
    ;; TODO
    [:span.beat {:class looped}
     (draw-children (rest item))
     ]))

(comment
  ;; TODO: Add underline for hindi pitches that need it. Old code:
  ;;;;          if ((this.props.kind === "hindi-composition") &&
  ;;;;            (needs_underline[second(pitch)])
  ;;;;          ) {
  ;;;;            kommalIndicator = span({
  ;;;;              key: 99,
  ;;;;              className: "kommalIndicator"
  ;;;;            }, "_");
  ;;;;          }
  )

(defn pitch-name[{item :item}]
  ;; (assert (is? :pitch-name item))
  (when false
    (println "pitch-name, item is")
    (println item)
    (println (second item)))
  [:span.note.pitch {:dangerouslySetInnerHTML  
                     {
                      :__html 
                      (second item)
                      }
                     }
   ]
  )

(defn pitch-alteration[{item :item}]
  (log "pitch-alteration") 
  (assert (is? :pitch-alteration item))
  [:span.note.pitch.alteration 
   {:dangerouslySetInnerHTML
    {
     :__html 
     (second item)
     }
    }
   ]
  ) 

(defn begin-slur-id[{item :item}]
  [:span.slur {:id (second item)}]
  )

(defn needs-kommal-indicator?[normalized-pitch kind]
  (log "entering needs-kommal-indicator," normalized-pitch kind)
  (assert (string? normalized-pitch))
  (assert (keyword? kind))
  (and (= kind :hindi-composition)
       (#{"Db" "Eb" "Ab" "Bb"} normalized-pitch))) 

(defn pitch[{item :item
             render-as :render-as}]
  (log "entering pitch, item=" item) 
  ;; gnarly code here.
  (log "pitch, (first (last item))=" (first (last item))) 

  ;; In the following case
  ;; ["pitch","C",["begin-slur"],["octave",0],["begin-slur-id",0]]
  ;; if there is "begin-slur-id, add
  ;; <span class="slur" id="0"></span>
  ;;  before the note span.
  ;;
  ;; for end slur, add data-begin-slur-id to the note-wrapper
  ;; confusing
  ;;
  ;;
  ;;; ["pitch","C#",["octave",1],["syl","syl"]]
  ;;;  ["pitch","E",["end-slur"],["octave",0],["end-slur-id",0]]
  (log "entring pitch, item is" item) 
  ;;  (assert (is? :pitch item))
  (log item)
  ;; need to sort attributes in order:
  ;; ornament octave syl note alteration
  ;; TODO: refactor. Hard to understand.
  (let [
        ;; Looks like ["end-slur-id",0]
        begin-slur-id (some (fn[x] 
                              (if
                                (and (vector? x)
                                     (= :begin-slur-id (first x)))
                                x))
                            item)
        end-slur-id (some (fn[x] 
                            (if
                              (and (vector? x)
                                   (= :end-slur-id (first x)))
                              x))
                          item)
        h (if end-slur-id
            {:data-begin-slur-id (second end-slur-id) }
            {}
            :class (css-class-name-for (first item)) 
            )
        kommal-indicator 
        (when (needs-kommal-indicator? (second item)
                                       @render-as)
          [:kommal-indicator])
        deconstructed-pitch ;; C#,sargam -> ["S" "#"] 
        (deconstruct-pitch-string-by-kind (second item)
                                          @render-as
                                          ) 
        sort-table 
        {:ornament 1 
         :octave 2 
         :syl 3 
         :kommal-indicator 4
         :begin-slur-id 5 
         :slur 6 
         :pitch-name 7
         :pitch 8 
         :pitch-alteration 9}
        item1
        (into[] (cons [:pitch-name (first deconstructed-pitch)]
                      (rest (rest item))))
        item2 (if begin-slur-id
                (into[] (cons [:slur (second begin-slur-id)] item1))
                item1)

        alteration-string (second deconstructed-pitch)
        my-pitch-alteration (when alteration-string
                              [:pitch-alteration alteration-string])

        item4 
        (remove nil? (into[] (cons my-pitch-alteration item2)))
        item5
        (remove (fn[x] (get #{:end-slur-id :slur} (first x))) item4)
        item5a (remove nil? (into [] (cons kommal-indicator item5)))
        item6 (sort-by #(get sort-table (first %)) item5a)
        ]
    (log "item6 is")
    ;;[["pitch-name","D#"],["octave",1],["syl","syl"]] 
    (log item6)
    [:span.note_wrapper h  ;; This indicates slur is ending and gives the id of where the slur starts. NOTE.
     (draw-children item6)
     ]
    ))

(defn lyrics-section [{item :item}]
  ;; ["lyrics-section",["lyrics-line","first","line","of","ly-","rics"],["lyrics-line","se-","cond","line","of","ly-","rics"]]
  ;; assert(isA("lyrics-section", lyricsSection))
  ;; return rest(x) .join(" ");
  ;;
  (let [line-strings (map (fn[x] (join " " (rest x))) (rest item))
        s (join "\n" line-strings)    
        ]

    [:div.stave.lyrics_section.unhyphenated
     {:title "Lyrics Section"}
     s]))


(defn stave[{item :item}]
  (log "entering stave")
  (log item)
  ;;  (assert (is? :stave item))
  [notes-line {:item (second item)}]
  )



(defn measure[{item :item}]
  (assert (is? :measure item))
  [:span {:class "measure"} 
   (draw-children (rest item))])


(defn tala[{item :item}]
  (assert (is? :tala item))
  [:span.tala (second item)]
  )

(defn chord[{item :item}]
  (assert (is? :chord item))
  [:span.chord (second item)]
  )

(def EMPTY-SYLLABLE "\" \"") ;; " " which quotes
;; EMPTY_SYLLABLE is for the benefit of lilypond.

(defn syl[{item :item}]
  (assert (is? :syl item))
  (log "in syl, item is" item)
  (log "syl- item is")
  (log item)
  (when (not= (second item) EMPTY-SYLLABLE)
    [:span.syl (second item)]
    ))

(defn abs [n] (max n (- n)))

(defn octave[{item :item}]

  ;; TODO: support upper-upper and lower-lower
  (log "octave- item is")
  (log item)
  (assert (is? :octave item))
  (let [octave-num (second item)] 
    (if (or (nil? octave-num)
            (zero? octave-num))
      nil
      ;; else
      [:span {:class (class-for-octave (second item))
              :dangerouslySetInnerHTML {
                                        :__html 
                                        (clojure.string/join (repeat (abs octave-num) bullet))
                                        }
              } ]
      )))

(defn kommal-indicator[{item :item}]
  (assert (is? :kommal-indicator item))
  [:span.kommalIndicator
   "_"]
  )

;;      kommalIndicator = span({
;;       key: 99,
;;      className: "kommalIndicator"
;;   }, "_");

(defn draw-item[item idx]
  (let [my-key  (first item)
        render-as (subscribe [:render-as]) 
        ]
    (cond 
      (= my-key :begin-slur)
      nil
      (= my-key :end-slur)
      nil
      (= my-key :ornament)
      [ornament {:key idx :item item}]
      (= my-key :mordent)
      [mordent {:key idx :item item}]
      (= my-key :ending)
      [ending {:key idx :item item}]
      (= my-key :barline)
      [barline {:key idx :item item}]
      (= my-key :lyrics-section)
      [lyrics-section {:key idx :item item}]
      (= my-key :tala)
      [tala {:key idx :item item}]
      (= my-key :chord)
      [chord {:key idx :item item}]
      (= my-key :kommal-indicator)
      [kommal-indicator {:key idx :item item}]
      (= my-key :syl)
      [syl {:key idx :item item}]
      (= my-key :beat)
      [beat {:key idx :item item}]
      (= my-key :stave)
      [stave {:key idx :item item}]
      (= my-key :measure)
      [measure {:key idx :item item}]
      (= my-key :end-slur-id)
      nil
      (= my-key :begin-slur-id)
      [begin-slur-id {:key idx :item item}]
      (= my-key :attribute-section)
      [attribute-section {:key idx :item item}]
      (= my-key :pitch-alteration)
      [pitch-alteration {:key idx :item item}]
      (= my-key :ornament-pitch)
      (do
        (println "my-key= :ornament-pitch")
    ;;  [ornament-pitch {:key idx :item item :render-as @render-as }]
      )
      (= my-key :pitch)
      [pitch {:key idx :item item :render-as render-as }]
      (= my-key "syl")
      [syl {:key idx :item item}]
      (= my-key :octave)
      [octave {:key idx :item item}]
      (= my-key :pitch-name)
      [pitch-name {:key idx :item item}]
      (= my-key :notes-line)
      [notes-line {:key idx :item item}]
      (= my-key :line-number)
      [line-number {:key idx :item item}]
      (= my-key :dash)
      [line-item {:src "-" :key idx :item item}]
      true
      [:span {:key idx :item item}
       (str "todo-draw-item" (.stringify js/JSON (clj->js item)))
       ]
      )))

(defn select-notation-box[kind]
  (let [composition-kind (subscribe [:composition-kind])]
  [:div.form-group ;;selectNotationBox
   [:label {:for "selectNotation"}
    "Enter Notation as: "]
   [:select#selectNotation.selectNotation.form-control
    {:value @composition-kind 
     :on-change 
     (fn on-change-select-notation[x]
       (let
        [kind-str (-> x .-target .-value)
         my-kind (if (= "" kind-str)
                   nil
                   ;; else
                   (keyword kind-str))
         ]
        (dispatch [:set-composition-kind my-kind])
        ))
     } 
    [:option]
    [:option {:value :abc-composition}
     "ABC"]
    [:option {:value :doremi-composition}
     "doremi"]
    [:option {:value :hindi-composition}
     "hindi( स र ग़ म म' प ध ऩ )"]
    [:option {:value :number-composition}
     "number"]
    [:option {:value :sargam-composition}
     "sargam"]]]
  ))

(defn render-as-box[render-as]
  [:div.form-group ;;selectNotationBox
   ;;[:div.RenderAsBox
   [:label { :for "renderAs"} "Render as:"]
   [:select#renderAs.renderAs.form-control
    {:value (name render-as)
     :on-change 
     (fn on-change-render-as[x]
       (let [value (-> x .-target .-value)]
         (when (not= value "")
         (println "value=" value)
       (dispatch [:set-render-as (keyword value)]))))
     }
    [:option {:value nil}]
    [:option {:value :abc-composition}
     "ABC"]
    [:option {:value :doremi-composition}
     "doremi"]
    [:option {:value :hindi-composition}
     "hindi( स र ग़ म म' प ध ऩ )"]
    [:option {:value :number-composition}
     "number"]
    [:option {:value :sargam-composition}
     "sargam"]]]
  )

(defn generate-staff-notation-button[]
    (let [ajax-is-running (subscribe [:ajax-is-running])
          online (subscribe [:online]) 
          ]
  [:button.btn.btn-primary
   {
    :title "Redraws rendered letter notation and Generates staff notation and MIDI file using Lilypond",
    :name "generateStaffNotation"
    :disabled (or (not @online) @ajax-is-running)
    :on-click 
    (fn [e]
      (stop-default-action e)
      (dispatch [:generate-staff-notation]))
    }
   (if @ajax-is-running
     "Redrawing..."
     "Generate Staff Notation and audio"
     )
   ] 
  ))

(defn audio-div[mp3-url]
  [:audio#audio
   {
    :controls "controls"
    :preload "auto"
    :src mp3-url
    }
   ]
  )

(defn links[]
  (let [my-links (subscribe [:links])
        ]
  [:div.form-group ;;selectNotationBox
   ;;[:div.RenderAsBox
   [:select.form-control
    {
     :value ""
     :on-change 
     (fn[x]
       (let [value (-> x .-target .-value)]
       (when (not= value "")
       (dispatch [:open-link value]))))
     }
    [:option  {:value ""} "Links"]
      (doall (map-indexed
               (fn[idx z] 
                 (let [k (first z)
                       v (second z)]
    [:option {:key idx
              :value v}
                   (string/replace (name k) #"-url$"   "")]
    ))
               @my-links
               ))
      ]]))


(defn controls[]
  (let [mp3-url (subscribe [:mp3-url])
        render-as (subscribe [:render-as]) 
        composition-kind (subscribe [:composition-kind])
        ]
  [:form.form-inline
   [select-notation-box @composition-kind]
   [render-as-box @render-as]
   [generate-staff-notation-button]
   [links]
   (if @mp3-url
     [audio-div @mp3-url])
   ]
  ))

(defn doremi-box[]
  [:div.doremiBox
   [controls]
   [entry-area-input]
   [composition-box]
   [staff-notation]
   [display-parse-to-user-box]
   ]
  )


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

(comment
  (go (while true
        (<! keypresses)
        (let [cur-value (user-query)]
          (when (not= cur-value @last-value) 
            (reset! last-value cur-value)
            (let [ results (<! (jsonp (str wiki-url cur-value))) ]
              (set! (.-innerHTML results-view) (render-query results))
              )
            (<! (timeout (* 6 seconds))) 
            ))
        )))


(defn simple-example
  []
  [:div
   "HI"])



(defn init []
  (dispatch-sync [:initialize initial-state])
  (go
     (dispatch [:set-parser 
           (component/start (new-parser
                              (<! (load-serialized-grammar-xhr))))
           ]) 
    )
  (let [old-val (.-value (.getElementById js/document "the_area"))
        url-to-load (.getParameterValue
                      (new goog/Uri (.-href (.-location js/window)))
                      "url")
        _ (log "url-to-load is" url-to-load)
        ]
    (reagent.core/render-component 
      [doremi-box]
      (.getElementById js/document "container"))

    (when url-to-load
        (dispatch [:open-url url-to-load]))

    (log "starting timer")

    (.focus (.getElementById js/document "area2"))
    (if old-val
      (set! (.-value (sel1 :#area2)) old-val))
    (start-parse-timer)
    ))


