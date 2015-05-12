(ns doremi-script.app
  ;;(:require-macros 
  (:require-macros 
    ;;               [cljs.core :refer [assert]]
                     [cljs.core.async.macros :refer [go]]
                   )
  (:require 
    #?(:clj
        [com.stuartsierra.component :as component]
        )
    #?(:cljs
        [quile.component :as component]
        )
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
    [goog.net.NetworkTester]
    [goog.net.XhrIo]
    [goog.json]
    [clojure.set]
    [clojure.string :as string :refer [lower-case upper-case join]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [cljs.core.async :refer [<! chan close! timeout put!]]
    [reagent.core]
    [cljs.reader :refer [read-string]]
    [instaparse.core :as insta] 
    ))
(enable-console-print!)

(defn by-id [id]
    (.getElementById js/document (name id)))

(defn listen [el event-type]
  (let [out (chan)]
    (events/listen el event-type
                   (fn [e] (put! out e)))
    out))

(comment
(let [uri (new goog/Uri "//www.google.com/images/zcleardot.gif")
      _  (.makeUnique uri)
      img (new js/Image)
      ch (listen img "load")
      _ (set! (.-src img) (str uri))
      _ (.log js/console  "img is" img)
      ]
  (go  (println "img on load returns" (.log js/console (<! ch)))
      (println "after image has loaded") 
      ))
(println "after let")
)




(def production?
  ;; set the global in index.html
  (and (not= js/undefined js/DOREM_SCRIPT_APP_ENV)
       (= js/DOREM_SCRIPT_APP_ENV "production")
       ))
(log "production? = " production?)
;;(= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

(def mode->notes-used
  {
   :ionian "SRGmPDN"
   :dorian "SRgmPDn"
   :phyrgian "SrgmPdn"
   :lydian "SRGMPDN"
   :mixolydian "SRGmPDn"
   :aeolian "SRgmPdn"
   :locrian "SrgmPdn"

   :major "SRGmPDN"
   :minor "SRgmPdn"
   (keyword "harmonic minor") "SRgmPdN"

   :bilaval "SRGmPDN"
   :kafi "SRgmPDn"
   :bhairavi "SrgmPdn"
   :kalyan "SRGMPDN"
   :khammaj "SRGmPDn"
   :asavri "SRgmPdn"

   :marwa "SrGMPDN"
   :purvi "SrGMPdN"
   :lalit "SrGmMPdN"
   :hindol "SGMDN"
   :kirwani "SRgmPdN"
   (keyword "ahir bhairav") "SrGmPDn"
   }) 


(def lower-sargam? #{"s" "r" "g" "m" "p" "d" "n"})

(def upper-sargam? #{"S" "R" "G" "M" "P" "D" "N"})

(defn remove-if-both-cases[my-set ch]
  ;;(log "remove-if-both-cases, " my-set ch)
  (let [lower-ch (lower-case ch)
        upper-ch (upper-case ch)]
    (if (and (get my-set lower-ch)
             (get my-set upper-ch)
             )
      (clojure.set/difference my-set #{ lower-ch upper-ch })
      my-set
      )))

;;(log "remove-if-both-cases test:" (remove-if-both-cases #{"N" "n" "S"} "n"))

(defn notes-used-set-for[{mode :mode notes-used :notes-used}]
  (log "entering notes-used-set-for, mode,notes-used" mode notes-used)
  (let [
        mode-notes-used (when mode
                          (get mode->notes-used  (keyword (lower-case mode)) ""))
        _ (log "mode-notes-used=*************" mode-notes-used)
        notes-used2 (or  notes-used
                        mode-notes-used
                        "SP")
        _ (log "notes-used2=*************" notes-used2)

        ]
    (assert (string? notes-used2))
    (set (reduce (fn[accum item] (remove-if-both-cases accum item))
                 notes-used2
                 "rgmdn"))
    ))

(defn sargam-set->key-map[sargam-set]
  ;; Returns a keymap: ie {"s" "S" }. Saves typing
  ;; example (sargam-set->key-map #{R}) -> {"r" "R" "R" "r"}
  (assert (set? sargam-set))
  (log "entering sargam-set->key-map sargam-set is" sargam-set)
  (reduce (fn[accum item]
            (if (upper-sargam? item)
              (assoc accum 
                     item (lower-case item) 
                     (lower-case item) item)
              ;; else
              accum
              )) 
          {"s" "S" "p" "P"}  sargam-set)
  )

(comment
  (sargam-set->key-map #{"S" "R" "G" "m" "P" "D" "N"})
  (comment "test:  @key-map is" @key-map)
  )

(defonce printing (reagent.core/atom false))

(defonce app-state
  (reagent.core/atom 
    {
     :the-parser nil
     :key-map {}
     :rendering false
     :ajax-is-running false 
     :composition-kind :sargam-composition
     :mp3-url nil
     ;;"http://ragapedia.com/compositions/yesterday.mp3"
     :render-as :sargam-composition
     :staff-notation-path nil 
     :composition nil 
     }))


(declare draw-item) ;; need forward reference since it is recursive

(enable-console-print!)

(defn css-class-name-for[x]
  (string/replace (name x) "-" "_")
  )


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

(defn network-tester[]
  ;; googles network tester seems broken!!!
  (println "entering network tester")
  ;; Returns a channel which will have true or false on it
  (let [out (chan)
        my-fn (fn [res] 
                (println "in network-tester callback, res=" res)
                (put! out res))

        req (goog.net.NetworkTester. my-fn) ]
    (.start req)
    out))


;;; (go  (println "network-tester returns" (<! (network-tester))))


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

(defn load-doremi-url-xhr[url]
  (swap! app-state assoc :ajax-is-running true)
  (log "load-doremi-url-xhr")
  (log "load-doremi-url-xhr: url is" url)
  (goog.net.XhrIo/send url
                       (fn [event]
                         (swap! app-state assoc :ajax-is-running false)
                         (log "in callback")
                         (let [raw-response (.-target event)
                               response-text (.getResponseText raw-response)
                               ]
                           (set! (.-value (sel1 :#the_area)) response-text)
                           ))
                       "GET"))


(defn key-map-for-composition[composition]
  (let [
        attributes (if composition (get-attributes composition) {})
        notes-used (:notesused attributes)
        mode (:mode attributes)
        ]
    (->
      {:mode mode :notes-used notes-used}
      notes-used-set-for
      sargam-set->key-map)
    ))


(defn generate-staff-notation-xhr-callback[event]
  ;; response looks somthing like
  ;; {
  ;;
  ;; :links [xxx]
  ;; :composition [:composition...
  ;; :error "fadfadf"
  ;;   }
  (println "entering generate-staff-notation-xhr-callback")
  (swap! app-state assoc :ajax-is-running false)
  (log "in generate-staff-notation-xhr callback")
  (let [raw-response (.-target event)
        response-text (.getResponseText raw-response)
  ;;    {:keys [:composition :error] :as results}
        results
         (-> response-text
                   goog.json/parse
                   (js->clj :keywordize-keys true)
                   )
        my-map (if (:error results)
                      results
                   (update-in results [:composition]
                              keywordize-vector))
        _ (prn "my-map" my-map)
        {:keys [:links :composition :error]} my-map 
        ]
    (log "in callback my-map" my-map)
    (log "in callback, links=" links)
    (swap!  app-state
           assoc 
           :composition
           composition
           :error
           error
           :links
           links
           :key-map
           (if (not (:error my-map))
             (key-map-for-composition composition)
             (:key-map @app-state))
           )
    (log "after xhr callback-app-state is" @app-state)
    ))


(defn update-app-state![ {:keys [:composition :error] :as results}]
   (swap! app-state merge results
             (if error
               {}
                {:key-map (key-map-for-composition composition)})))

(defn parse-local[doremi-text kind]
  (let [composition-view (dom/getElement "doremiContent")]
  ;; reagent doesn't immediately redraw it, so do it manually
;;   (set! (.-innerHTML composition-view) "Redrawing: Please wait")
     (-> doremi-text  
           (doremi-text->collapsed-parse-tree (get @app-state :the-parser) kind)
            update-app-state!)))



(defn parse-xhr-callback[event]
  (let [raw-response (.-target event)
        response-text (.getResponseText raw-response)
        results (-> response-text
                   goog.json/parse
                   (js->clj :keywordize-keys true))
        _ (assert (contains? results :composition))
        _ (assert (contains? results :error))
        results2 (if (not (:error results))
                          (assoc results :composition
                                 (keywordize-vector (:composition results)))
                          results)
        ]
    (update-app-state! results2)))




(defn parse-xhr[url {src :src kind :kind}]
  (log "entering parse-xhr:"  "url=" url " src= " src "\nkind=" kind)
  (let [ query-data (new goog.Uri/QueryData) ]
    (.set query-data "src"  src)
    (.set query-data "kind" (name  kind))
    (goog.net.XhrIo/send url
                         parse-xhr-callback
                         "POST"
                         query-data)))


(defn parse[]
  (parse-xhr 
    PARSE-URL
    {:src (.-value (sel1 :#the_area))
     :kind (get-in @app-state [:composition-kind])
     }))


(defn generate-staff-notation-xhr [url content]
  (when (not (:ajax-is-running @app-state))
    (log "entering GENERATE-STAFF-NOTATION-URL" url content)
    (swap! app-state 
           assoc :links nil
           :ajax-is-running true)

    (let [ query-data (new goog.Uri/QueryData) ]
      ;; TODO: try sending json
      (.set query-data "src"  (:src content))
      (.set query-data "kind"  (name (:kind content)))
      (.set query-data "mp3"  true)
      (goog.net.XhrIo/send url
                           generate-staff-notation-xhr-callback
                           "POST"
                           query-data))))

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

(defn downloads[]
  [:select#downloads.form-control
   {
    :on-change (fn[evt]
                 (.preventDefault evt)
                 (.open js/window (.-value (.-target evt))))
  ;;  :value "TODO"                         
    } 
   [:option
    {
     :defaultValue true
      }
    "Links"]
   (when-let [links (get-in @app-state [:links])] 
     (doall (map-indexed
              (fn[idx [k v]] 
                [:option
                 {
                  :value v
                  :key idx
                  }
                 (string/replace (name k) "-url" "")
                 ])
              links
              ))) 
   ]) 



(defn display-parse-to-user-box []
  (let [error (get-in @app-state [:error])
        _ (log "in display-parse-to-user-box, error=" error)
        parse-results (:composition @app-state)
        _ (log "in display-parse-to-user-box, parsed" parse-results)
        ]
    [:div.form-group.hidden-print
     {
      :class (if error "has-error" "") 
      }
     [:label.control-label {:for "parse-results"
                            } "Parse Results:"]
     [:textarea#parse-results.form-control 
      {:rows "3" 
       :spellCheck false
       :readOnly true
       :value 
       (if error
         (print-str error)
         (print-str parse-results))
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


(defn on-key-press[evt] 
  (if (not= :sargam-composition (get @app-state :composition-kind))
    true 
    (do
      (log "entering on-key-press")
      (let [
            my-key-map (:key-map @app-state)
            target (.-target evt)
            key-code (.-keyCode evt)
            ctrl-key? (or (.-ctrlKey evt)
                          (.-altKey evt)
                          (.-metaKey evt))
            from-char-code-fn (.-fromCharCode js/String)
            ch (from-char-code-fn key-code)
            ; _ (log "evt is" evt) 
            ; _ (comment "ch is ****" ch)
            ; _ (comment "my-key-map is" my-key-map)
            new-char (if-not ctrl-key?  (get my-key-map ch ))
            ; _ (comment "new-char ****" new-char " *********")
            caret-pos (.-selectionStart target)
            ; _ (comment "caret-pos" caret-pos)
            ;; var caretPos = document.getElementById("txt").selectionStart;
            text-area-text (.-value target)
            ; _ (comment "text-area-text=" text-area-text) 
            selection (get-selection target)
            ; _ (comment "selection is" selection)
            my-within-sargam-line (within-sargam-line? text-area-text (:start selection))
            ; _ (comment "my-within-sargam-line=" my-within-sargam-line)
            ]
        ;;; nativeEvent looks like  {which: 189, keyCode: 189, charCode: 0, repeat: false, metaKey: false…}
        (comment "app-state is" @app-state)
        ;;  jQuery("#txt").val(textAreaTxt.substring(0, caretPos) + txtToAdd + textAreaTxt.substring(caretPos) );
        ;;  TODO: review returning true/false. Shouldn't it be prevent default?
        (if (and my-within-sargam-line
                 new-char)
          (do
            ;; (.preventDefault evt)
            (set! (.-value target)
                  (str (.substring text-area-text 0 caret-pos) 
                       new-char 
                       (.substring text-area-text caret-pos)))
            (set! (.-selectionStart target)
                  (inc (:start selection)))
            (set! (.-selectionEnd target)
                  (inc (:end selection)))
            false)
          ;; else
          true )
        ))))

(defn parse-local-button[]
  [:button.btn.btn-primary
   {
    :title "Redraw Letter Notation use when server is down",
    :name "redraw_letter_notation"
    :on-click 
    (fn [e]
      (.preventDefault e)
      (parse-local (.-value (sel1 :#the_area))
                   (:composition-kind @app-state))
      )
    }
   "Redraw Local"
   ] 
  )

(defn parse-button[]
  [:button.btn.btn-primary
   {
    :title "Redraw Letter Notation",
    :name "redraw_letter_notation"
    :on-click 
    (fn [e]
      (.preventDefault e)
      (parse)
      )
    }
   "Redraw"
   ] 
  )

(defn entry-area-box[]
  [:div.form-group.hidden-print
   [:label {:for "entryArea"} "Enter Letter Notation Source:"]
   [:textarea#the_area.entryArea.form-control
    {
     :autofocus true
     :placeholder text-area-placeholder
     :name "src",
     :spellCheck false
     } 
    ]])


(defn draw-children[items]
  (doall (map-indexed
           (fn notes-line-aux[idx item]
             (draw-item item idx))
           items)))

(defn staff-notation[]
  [:img#staff_notation.hidden-print 
   {:class (if @printing "printing" "")
    :src (get-in @app-state [:links :staff-notation-url])}])

(defn html-rendered-composition[]
  (let [composition (:composition @app-state)] 

    (if (not composition)
      [:div#doremiContent.composition.doremiContent ]
      ;; else
      [:div#doremiContent.composition.doremiContent {:class (if @printing "printing"
                                                "")}
       (draw-children (rest composition))]
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


(def seconds 1000)
(defn start-parse-timer[]
  (js/setInterval parse (* 6 seconds))
  )


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
    [:li
     [parse-local-button]  
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
  (let [items (rest item)
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
                                 :render-as (get @app-state :render-as)
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
                                       render-as)
          [:kommal-indicator])
        deconstructed-pitch ;; C#,sargam -> ["S" "#"] 
        (deconstruct-pitch-string-by-kind (second item)
                                          render-as
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
  (let [my-key  (first item)]
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
      [ornament-pitch {:key idx :item item
                       :render-as (get @app-state :render-as)}]
      (= my-key :pitch)
      [pitch {:key idx :item item
              :render-as (get @app-state :render-as)}]
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
  [:div.form-group ;;selectNotationBox
   [:label {:for "selectNotation"}
    "Enter Notation as: "]
   [:select#selectNotation.selectNotation.form-control
    {:value (get @app-state :composition-kind)
     :on-change 
     #(let
        [kind-str (-> % .-target .-value)
         my-kind (if (= "" kind-str)
                   nil
                   ;; else
                   (keyword kind-str))
         ]
        (swap! app-state assoc :composition-kind my-kind)
        )
     } 
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

(defn render-as-box[]
  [:div.form-group ;;selectNotationBox
   ;;[:div.RenderAsBox
   [:label { :for "renderAs"} "Render as:"]
   [:select#renderAs.renderAs.form-control
    {:value (name (get @app-state :render-as))
     :on-change 
     #(swap! app-state 
             assoc
             :render-as
             (keyword (-> % .-target .-value))
             )
     }
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
(defn print-toggle[]
  [:button.btn.btn-primary
   {
    :title "Toggle font size for rendered letter notation"
    :name "font_toggle"
    :on-click 
    (fn [e]
      (.preventDefault e)
      (swap! printing not)
      )
    }
   "Toggle Font Size"
   ] 
  )

(defn generate-staff-notation-button[]
  [:button.btn.btn-primary
   {
    :title "Redraws rendered letter notation and Generates staff notation and MIDI file using Lilypond",
    :name "generateStaffNotation"
    :disabled (:ajax-is-running @app-state)
    :on-click 
    (fn [e]
      (.preventDefault e)
      (when (not (:ajax-is-running @app-state))
        (log "in generate-staff-notation-button callback")
        (generate-staff-notation-xhr 
          GENERATE-STAFF-NOTATION-URL
          {:src (.-value (sel1 :#the_area))
           :kind (get-in @app-state [:composition-kind])
           })
        ))
    }
   (if (:ajax-is-running @app-state)
     "Generating staff notation... Please wait. This may take some time..."
     "Generate Staff Notation and audio..."
     )
   ] 
  )



(defn mp3-url[] 
  [:a.btn.btn-info 
   { :href (:mp3-url @app-state)
    :target "_blank",
    :title "Opens in new window"}
   "Play mp3"]
  )

(defn audio-div[]
  (when-let [mp3-url (get-in @app-state [:links :mp3-url])]
    [:audio#audio
     {
      :controls "controls"
      :preload "auto",
      :src mp3-url
      }]
    ))

(defn controls[]
  [:form.form-inline.hidden-print
   [select-notation-box (get @app-state :kind)]
   [render-as-box (get @app-state :render-as)]
   [generate-staff-notation-button]
   [downloads]
   [audio-div]
   [print-toggle]
   ]
  )

(defn doremi-box[]
  [:div.doremiBox
   [controls]
   [entry-area-box]
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

(defn init []
  (go
    (swap! app-state 
           assoc
           :the-parser 
           (component/start (new-parser
                                    (<! (load-serialized-grammar-xhr))))
           ))
  (let [old-val (.-value (.getElementById js/document "the_area"))
        url-to-load (.getParameterValue
                      (new goog/Uri (.-href (.-location js/window)))
                      "url")
        _ (log "url-to-load is" url-to-load)
        ]
    (when url-to-load
      (load-doremi-url-xhr url-to-load))

    (reagent.core/render-component 
      [doremi-box]
      (.getElementById js/document "container"))
    (log "starting timer")
    (.focus (.getElementById js/document "the_area"))
    (set! (.-onkeypress (by-id "the_area"))
          on-key-press
          )
    (if old-val
      (set! (.-value (sel1 :#the_area)) old-val))
    ;; (start-parse-timer)
    ))


;; ********************** code from xhr-keystrokes follows****
(comment
  (ns async-test.app
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require
     i        [goog.dom :as dom]
              [goog.Uri] 
              [goog.net.Jsonp]
              [goog.events :as events]
              [cljs.core.async :refer [close! timeout put! chan <!]])
    )

  (def wiki-url
    "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

  (defn jsonp [uri]
    (let [out (chan)
          req (goog.net.Jsonp. (goog/Uri. uri))]
      (.send req nil (fn [res] (put! out res)))
      out))

  (defn listen [el event-type]
    (let [out (chan)]
      (events/listen el event-type
                     (fn [e] (put! out e)))
      out))

  (def seconds 1000)

  (defn user-query []
    (.-value (dom/getElement "query")))

  (defn render-query [results]
    (str
      "<ul>"
      (apply str
             (for [result results]
               (str "<li>" result "</li>")))
      "</ul>"))

  (defn zzzzinit []
    (println "in init")
    (let [ results-view (dom/getElement "results")
          last-value (atom "") 
          keypresses (listen (dom/getElement "query") "keypress")]
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
            ))))


  )
