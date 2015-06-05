(ns doremi-script.views
  (:require 
    [doremi-script.dom-utils :refer [by-id]] ;; listen seconds]]
    [doremi-script.utils :refer [get-attributes keywordize-vector 
                                 log is?] ]

    ;; [doremi-script.doremi_core :as doremi_core
    ;; :refer [doremi-text->collapsed-parse-tree]]
    [doremi-script.dom-fixes :refer [dom-fixes]]
    [goog.dom :as dom]
    [goog.Uri] 
    [goog.events :as events]
    [clojure.string :as string :refer [join]]
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

(def printing (atom false))

(defn stop-default-action[event]
  (.preventDefault event)) 

(declare draw-item) ;; need forward reference since it is recursive

(defn css-class-name-for[x]
  (string/replace (name x) "-" "_")
  )

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
(def sharp-symbol "#")
(def sharp-symbol-utf8 "&#9839;")
(def flat-symbol  "b")
(def flat-symbol-utf8  "&#9837;")

(def alteration->utf8 {
                         "b" flat-symbol-utf8
                         "#" sharp-symbol-utf8
                         })

;; TODO: need to put fallback mechanism if the UTF symbols are not supported
;; old versions added data-fallback-if-no-utf8-chars="|" for example for barline
;; to the html. Then a small bit of javascript can adjust things (dom_fixer) in the case of a statically generated page.
;; For dynamic pages, set the value appropriately. Perhaps a lookup table
;; utf->simple  { flat-symbol "b" bar-line "|" } etc.
;; app-state should have :supports-utf8-chars

(def lookup-sargam
  {
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

(defn deconstruct-pitch-string[pitch kind utf-supported]
   (let [ [p alteration] (deconstruct-pitch-string-by-kind pitch kind)]
     [p 
      (if utf-supported 
      (get alteration->utf8 alteration alteration)
      alteration)
      ]
  ))


(def mordent-entity "&#x1D19D&#x1D19D")

(def lookup-barline-utf8
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

(def lookup-barline
  {
   :single-barline "|"
   :double-barline "||"
   :left-repeat "|:"
   :mordent "~"
   :right-repeat ":|"
   :final-barline "||"
   :reverse-final-barline ":||"
   }
  )

(defn redraw-lilypond-button[]
  [:button.btn.btn-primary
   {
    :title "Display Lilypond Source",
    :name "redraw_lilypond_source"
    :on-click 
    (fn [e]
      (stop-default-action e)
      (dispatch [:redraw-lilypond-source]) ;; include dom-id as param ??
      )
    }
   "Lilypond Source"
   ] 
  )

(defn display-lilypond-source []
  (let [
        lilypond-source (subscribe [:lilypond-source])
        ]
    [:div.form-group.hidden-print
     {
     ;; :class (if @error "has-error" "") 
      }
     [redraw-lilypond-button]
     [:label.control-label {:for "lilypond_source"
                            } "Lilypond Source:"]

     [:textarea#lilypond_source.form-control 
      {:rows "3" 
       :spellCheck false
       :readOnly true
       :value 
         (print-str @lilypond-source)
       }
      ]]))

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
  (when debug (println "entering on-key-press, my-key-map=" my-key-map))
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
          (when debug "in do********")
          (set! (.-value target)
                (str (.substring text-area-text 0 caret-pos) 
                     new-char 
                     (.substring text-area-text caret-pos)))
          (set! (.-selectionStart target)
                (inc (:start selection)))
          (set! (.-selectionEnd target)
                (inc (:end selection)))
          (dispatch [:set-doremi-text (.-value target)])
          false
          )
        (do
          (dispatch [:set-doremi-text (.-value target)])
          true)
        ))
    ))


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

;; "form-3" component see 
;; https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components
(defn entry-area-input[]  
  ;; textarea input keypresses is not handled by reagent
  (let [ dom-id "area2"
        online (subscribe [:online])
        composition-kind (subscribe [:composition-kind])
        key-map (subscribe [:key-map])
        ]   
    (reagent.core/create-class    
      {:component-did-mount       
       (fn entry-area-input-did-mount[this]
         (dispatch [:start-parse-timer dom-id])
         (dispatch [:set-current-entry-area dom-id])
         (set! (.-onkeypress 
                 (by-id dom-id)
                 )
               (fn my-on-key-press[event]
                 (when debug (println "my-on-key-press"))
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
  (let [utf-supported (subscribe [:supports-utf8-characters])]

  ;; item looks like:
  ;; ;; ["ornament",["ornament-pitch","B",["octave",1]]
  ;; [:span.ornament_item.upper_octave_1 "g"]
  (log "entering ornament-pitch") 
  (log item)
  (let [
        deconstructed-pitch ;; C#,sargam -> ["S" "#"] 
        (deconstruct-pitch-string (second item)
                                          render-as
                                          @utf-supported
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
    )))

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
  (let [utf-supported (subscribe [:supports-utf8-characters])]
  [:span.mordent
   {:dangerouslySetInnerHTML 
    { :__html 
    (if @utf-supported 
     mordent-entity
    "~") }
    }])
  ) 

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
  (let [barline-name (first (second item))
       utf-supported (subscribe [:supports-utf8-characters])
       ]
    (log "barline-name is" barline-name)
    [:span.note_wrapper
     [:span.note.barline 
      {:dangerouslySetInnerHTML 
       { :__html 
        (if @utf-supported
        (get lookup-barline-utf8 (keyword (first (second item))))
        (get lookup-barline (keyword (first (second item))))
        )
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
  (let [utf-supported (subscribe [:supports-utf8-characters])]
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
        (deconstruct-pitch-string (second item)
                                          @render-as
                                          @utf-supported
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
    )))

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
        (println "error-don't use-my-key= :ornament-pitch")
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

(defn render-as-box[]
  (let [render-as (subscribe [:render-as])]
    [:div.form-group ;;selectNotationBox
     ;;[:div.RenderAsBox
     [:label { :for "renderAs"} "Render as:"]
     [:select#renderAs.renderAs.form-control
      {:value @render-as
       :on-change 
       (fn on-change-render-as[x]
         (let [value (-> x .-target .-value)]
           (when (not= value "")
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
    ))

(defn generate-staff-notation-button[]
  (let [
        ajax-is-running (subscribe [:ajax-is-running])
        parse-xhr-is-running (subscribe [:parse-xhr-is-running])
        parser (subscribe [:parser])
        online (subscribe [:online]) 
        ]
    [:button.btn.btn-primary
     {
      :title "Redraws rendered letter notation and Generates staff notation and MIDI file using Lilypond",
      :name "generateStaffNotation"
      :disabled (or ;;;@parse-xhr-is-running
                    (not @online) @ajax-is-running)
      :on-click 
      (fn [e]
        (stop-default-action e)
        (dispatch [:generate-staff-notation]))
      }
     (cond
       ;; @parse-xhr-is-running
       ;;"Generate Staff Notation and audio"
       @ajax-is-running
       "Redrawing..."
       true
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

(defn key-map[]
  ;; for debugging. Not currently used
  (let [key-map (subscribe [:key-map])
        environment (subscribe [:environment])
        ]
    (when (= :development @environment)
      [:div
       (print-str @key-map)
       ]
      )))

(defn links[]
  (let [my-links (subscribe [:links])
        environment (subscribe [:environment])
        ]
    [:div.form-group ;;selectNotationBox
     ;;[:div.RenderAsBox
     [:select.form-control
      {
       :title "Opens a new window"
       :value ""
       :on-change 
       (fn[x]
         (let [value (-> x .-target .-value)]
           (cond (= value "")
                 nil
                 (= value "print-grammar")
                 (dispatch [:print-grammar])
                 true
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
      [:option  {:value "print-grammar"} "Print grammar to console"]
      [:option  {:value "https://github.com/rothfield/doremi-script/#readme"}
       "Help (github README)"]

      ]]))

(defn utf-support-div[]
  (let []
    (reagent.core/create-class    
      {:component-did-mount       
       (fn utf-support-div-did-mount[this]
          (dispatch [:check-utf-support (reagent/dom-node this) ])
         )
       :reagent-render
       (fn []  ;; remember to repeat parameters
         [:div.testing_utf_support
          [:span#utf_left_repeat.note.testing_utf_support
           {:style {:display "none"} }
           "𝄆"]
          [:span#utf_single_barline.note.testing_utf_support
           {:style {:display "none"} }
           "𝄀"]
          ]
         )
       }
      )))



(defn controls[]
  (let [mp3-url (subscribe [:mp3-url])
        composition-kind (subscribe [:composition-kind])
        ]
    [:form.form-inline
     [select-notation-box @composition-kind]
     [render-as-box]
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
   [display-lilypond-source]
   [utf-support-div]
   ]
  )

