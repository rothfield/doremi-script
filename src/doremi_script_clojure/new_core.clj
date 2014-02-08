(ns doremi_script_clojure.new_core
  (:require	
    [instaparse.core :as insta]

    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [clojure.string :refer [lower-case join]] 
    [clojure.java.io :as io :refer [input-stream resource]]
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk]]
    [clojure.zip :as zip :refer [seq-zip]]
    ))

(def assigned (atom {}))

(defn unique-for-assigned[x]
  "Make a unique key using meta-data"
  [ (meta x) x]) 

(defn in-assigned?[my-set x]
  (contains? my-set (unique-for-assigned x)))

(def debug false)

(def doremi-script-parser  
  (insta/parser
    (slurp (resource "new_doremiscript.ebnf")) :total true))

(defn is-attribute-section?[x]
  (and (vector? x)
       (contains? #{:attribute-section} (first x))))

(defn is-composition?[x]
  (and (vector? x)
       (contains? #{:composition} (first x))))

(defn doremi-text->parse-tree[txt]
  {
   :pre [(string? txt)]
   :post [ (or (is-composition? %)
               (map? %)) ]
   }
  ;; Parser doesn't handle input that doesn't end with a newline.
  ;; So fix it here. 
  (doremi-script-parser txt))


(defn items[x] (nnext x))

(defn add-ids-and-create-map[parse-tree]
  (let [ctr (atom  0)
        parse-tree2 (postwalk (fn[x] (if (and (vector? x) (insta/span x))
                                       (with-meta (into [] (concat [(first x)]
                                                                   [(swap! ctr inc)] (rest x)))
                                                  (meta x)) 
                                       x))
                              parse-tree)
        saved-meta-data (reduce (fn[memo x] 
                                  (if (vector? x)
                                    (assoc memo (second x) (meta x))
                                    memo 
                                    )) {} (tree-seq vector? identity parse-tree2)
                                )
        _ (when false (println "saved-meta-data" (pprint saved-meta-data)))
        ]
    [saved-meta-data parse-tree2]))



(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.new_core :reload) (ns doremi_script_clojure.new_core) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )

(comment
  ;;  (doremi-script-text->parsed-doremi-script 
  ;;   (slurp (resource "fixtures/georgia.doremiscript.txt")))
  )

(defn doremi-script-text->parsed-doremi-script[txt]
  (transform-parse-tree (doremi-text->parse-tree txt) txt))

(defn slurp-fixture[file-name]
  (slurp (resource 
           (str "fixtures/" file-name))))


(defn pprint-results[x]
  (if (:my_type x)
    (with-out-str (json/pprint x))
    (with-out-str (pprint x))))

(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (clojure.string/join "\n" seq)))))


(defn -myparse[txt]
  (try
    (let [ x (transform-parse-tree 
               (doremi-text->parse-tree  txt) txt)
          ]
      x)
    (catch Exception e (str "Error:" (.getMessage e)))
    )) 

(comment
  (pprint (-myparse "S"))
  )
(defn main-json[txt]
  (pprint-results 
    (transform-parse-tree (doremi-text->parse-tree  txt)
                          txt)))
(defn doremi-text->json-data[txt]
  (let [
        parse-tree (doremi-text->parse-tree txt)
        ]
    (transform-parse-tree parse-tree txt)
    ))


(defn usage[]
  (println "Usage: pipe std in as follows: \"SRG\" |  java -jar doremi-script-standalone.jar > my.ly to produce lilypond output. Or --json to produce doremi-script json format. Or --ly to produce lilypond output.") 
  )


(defn -main[& args]

  "Read from stdin. Writes results to stdout"
  "Command line params: --json returns doremi json data"
  "--ly returns lilypond data"
  "defaults to lilypond"
  (try
    (let [
          txt (get-stdin)
          parse-tree (doremi-text->parse-tree  txt)
          x (transform-parse-tree parse-tree txt)
          ]
      (cond 
        (= (first args) "--parse-tree")
        (pprint parse-tree)
        (= (first args) "--json")
        (println (pprint-results x))
        (or (= (first args) "--ly") (empty? args)) 
        "" ;; (println (to-lilypond x))
        true
        (println (usage))
        )
      )
    (catch Exception e (println (str "Error:" (.getMessage e))))
    ))
;; (println (doremi-text->lilypond "<S-R>"))
;;(pprint (doremi-text->parse-tree "<S-R>"))
;; 
;;
(defn is-ornament?[x]
  {:pre [(vector? x)]}
  (contains? #{:ornament} (first x)))



(defn is-sargam-line?[x]
  (and (vector? x)
       (contains? #{:sargam-line} (first x))))

(defn is-sargam-section?[x]
  (and (vector? x)
       (contains? #{:sargam-section} (first x))))

(defn start-index[x]
  (let [x (insta/span x)]
    (if x
      (first x))))


(defn line-column-map[my-map my-line]
  (when false (println "entering line-column-map, my-map=" my-map))
  (let [line-start (start-index my-line) 
        ]
    (reduce (fn[accum obj]
              (if debug (println "line-column-map:, obj =" obj))
              (let [start-index (start-index obj)
                    column (if start-index 
                             (- start-index line-start))
                    _ (if debug (do (println "obj")))
                    ;;  (pprint (start-index obj))))
                    ]
                (cond (not start-index)
                      accum
                      (is-ornament? obj) 
                      ;; Ornaments are not directly over notes. They are before or after
                      ;; Add the ornament twice in the column map.
                      ;; ;;  [:ornament [:G] [:m] [:P] [:D]]
                      (let [
                            span (insta/span obj) 
                            ornament-string-length (apply - (reverse span))
                            column-for-after-ornament (dec column)
                            column-for-before-ornament (+ column  ornament-string-length)
                            ]
                        ;;(println "ornament case")
                        (assoc accum
                               column-for-after-ornament
                               (conj (get accum column-for-after-ornament [])
                                     obj)
                               column-for-before-ornament
                               (conj (get accum column-for-before-ornament [])
                                     obj)
                               )
                        )
                      true
                      (let [ column (- start-index line-start)
                            _ (when false ( println "true case, column is" column " obj is" obj)) ]
                        (assoc accum 
                               column 
                               (conj (get accum column [])
                                     obj))
                        ))))
            my-map
            (tree-seq vector? identity my-line)
            )))
(defn is-end-slur?[x]
  (and (vector? x) (= :end-slur (first x))))

(defn is-begin-slur?[x]
  (and (vector? x) (= :begin-slur (first x))))

(defn is-beat?[x]
  (and (vector? x)
       (= :beat (first x))))
(defn is-measure?[x]
  (and (vector? x)
       (= :measure (first x))))

(defn is-pitch?[x]
  ;; (when false (println "is-pitch, x is " x);)
  (and (vector? x)
       (= :pitch (first x))))

(defn takes-values-from-column?[x]
  (contains? #{:pitch :dash  } 
             x))

(defn is-upper-dot?[x]
  (when false (println "is-upper-dot?, x is") (pprint x))
  (and (vector? x)
       (contains? #{:upper-octave-dot :upper-upper-octave-symbol } 
                  (first x))))

(defn is-ornament?[x]
  (and (vector? x)
       (contains? #{:ornament }  (first x))))

(defn is-dot?[x]
  (contains? #{:upper-octave-dot :upper-upper-octave-symbol 
               :lower-octave-dot :lower-lower-octave-symbol}
             (first x)))

(defn is-upper-line?[x]
  (and (vector? x)
       (contains? #{:upper-octave-line } (first x))))

(defn is-lower-line?[x]
  (and (vector? x)
       (contains? #{:lower-octave-line :lyrics-line} (first x))))

(defn is-lyrics-line?[x]
  (and (vector? x)
       (contains? #{:lyrics-line} (first x))))

(defn is-main-line?[x]
  (and (vector? x)
       (contains? #{:sargam-line} (first x))))


;;(use 'clojure.stacktrace) 
(defn lines->column-map[lines]
  (when false (println "entering lines->column-map, ") (pprint lines) (println "lines->column-map"))
  (reduce (fn[accum item] 
            (line-column-map accum item)) {}
          (remove keyword? (remove #(or (is-sargam-line? %) (is-lyrics-line? %)) lines))))



(defn assign-ornament-octaves[my-section]
  {;; :pre [(is-sargam-section? my-section)] 
   :post [(is-sargam-section? %)] }
  (when false (println "entering assign-ornament-octaves:")
    (pprint my-section))
  (let [column-map (lines->column-map 
                     (filter is-upper-line? (items my-section)))
        _ (when false (println "****assign-ornament-octaves: column-map is")(pprint column-map))
        _ (when false (println "****Entering assign-ornament-octaves: my-section is")(pprint my-section))
        ]
    (into [] 
          (map 
            (fn map-fn[line] 
              (cond (is-upper-line? line)
                    (postwalk 
                      (fn assign-dots-postwalk-fn[item] 
                        (when false (println "assign-dots-postwalk-fn*** item is" item))
                        (cond 
                          (is-pitch? item)
                          ;; Look for dot in this column from lines above and below this one. But not in the lower lines
                          (let [column (- (start-index item) (start-index line))
                                _ (when false (println "column is:" column))
                                _ (when false (do (println "column-map is ")
                                                  (pprint column-map)))
                                _ (when false (println "column-map column:") (pprint (column-map column)))
                                dots-from-upper-lines (remove (partial in-assigned? @assigned)
                                                              (filter is-upper-dot?
                                                                      (column-map column [])))
                                ;;     _ (when false (println "dots-from-upper-lines") (pprint dots-from-upper-lines)))
                                ]
                            (if debug (println "is-pitch case"))
                            (if (empty? dots-from-upper-lines)
                              item
                              (let [
                                    fixed-dots (map (fn[x] 
                                                      {:pre [(is-upper-dot? x)]
                                                       :post [(is-dot? x)] }
                                                      (if (> (start-index x) (start-index item))
                                                        ({[:upper-octave-dot] [:lower-octave-dot]
                                                          [:upper-upper-octave-symbol] [:lower-lower-octave-symbol]}
                                                         x "wtf")
                                                        x))  dots-from-upper-lines)
                                    ] 
                                (reset! assigned (apply conj @assigned
                                                        (map unique-for-assigned dots-from-upper-lines)))
                                (apply conj item fixed-dots))
                              )
                            )
                          true
                          item)) line)
                    true
                    line 
                    )) my-section)))) 


(defn assign-syllables[section]
  (when false (println "assign-syllables") (pprint section))
  (let [ syls-to-apply (atom (mapcat items (filter is-lyrics-line? section)))
        _ (when false (println @syls-to-apply))
        in-slur (atom false)
        ]
    (map (fn[line] (if-not (is-sargam-line? line) line
                     ;; else
                     (postwalk (fn walk-fn[item]
                                 (cond  (empty? @syls-to-apply)
                                       item
                                       (is-pitch? item)
                                       (let [syl (if (not @in-slur) (first @syls-to-apply))
                                             ret-val (if syl (conj item syl) item)
                                             has-begin-slur (some is-begin-slur? item)
                                             has-end-slur (some is-end-slur? item)
                                             ]
                                         (if syl (swap! syls-to-apply rest))
                                         (cond (and has-begin-slur has-end-slur)
                                               nil
                                               has-begin-slur
                                               (reset! in-slur true)
                                               has-end-slur
                                               (reset! in-slur false))
                                         ret-val)
                                       true
                                       item
                                       ))     line)
                     )) section)
    ))

;; (print-stack-trace *e)

(defn handle-slurs[section]

  (map (fn[line]
         (if-not (is-sargam-line? line) 
           line
           (let [
                 pitch-positions 
                 (map start-index (filter is-pitch? (tree-seq vector? identity line)))
                 end-slur-positions (map start-index (filter is-end-slur? (tree-seq vector? identity line)))
                 begin-slur-positions (map start-index (filter is-begin-slur? (tree-seq vector? identity line)))

                 pitches-needing-begin-slurs 
                 (into #{}  (map (fn[slur-position]
                                   (first (filter 
                                            (partial < slur-position) pitch-positions)))
                                 begin-slur-positions))
                 pitches-needing-end-slurs 
                 (into #{}  (map (fn[slur-position]
                                   (last (filter 
                                           (partial > slur-position) pitch-positions)))
                                 end-slur-positions))
                 _ (when false
                     (pprint line) 
                     (println "pitch-positions" pitch-positions)
                     (println "begin-slur-positions" begin-slur-positions)
                     (println "end-slurs" end-slur-positions)
                     (println "pitches-needing-end-slurs" pitches-needing-end-slurs))
                 ]
             (postwalk (fn walk-line[item] 
                         (cond (is-pitch? item)
                               (let [ items-to-conj  
                                     (remove nil? 
                                             [
                                              (if (contains? pitches-needing-end-slurs (start-index item)) [:end-slur])
                                              (if (contains? pitches-needing-begin-slurs (start-index item)) [:begin-slur])
                                              ])]
                                 (if (not-empty items-to-conj)
                                   (apply conj item items-to-conj)
                                   item))
                               (is-begin-slur? item)
                               nil
                               (is-end-slur? item)
                               nil
                               (is-beat? item)
                               (into [] (remove nil? item))
                               (is-measure? item)
                               (into [] (remove nil? item))
                               true
                               item))
                       line 
                       ))))
       section)
  )

(defn assign-to-main-line[section]
  {
   :pre [(is-sargam-section? section)]
   :post [(is-sargam-section? %)
          ;; (do (println (start-index %)) (pprint %) true) 
          ]
   }
  (when false (println "assign-to-main-line") (pprint section))
  "collapse"
  (let [ column-map (lines->column-map (items section))
        _ (when false (println "column-map:\n" column-map))
        main-line (first (filter is-main-line? (items section)))     
        main-line-start-index (start-index main-line)
        _ (if debug (println "main-line is" main-line))
        line-starts (map start-index (items section))
        _ (when false (println "line-starts=") (pprint line-starts))
        line-start-for-position  (fn line-start-for-position-fn[position] 
                                   (last (filter (fn[x] (>= position x)) line-starts)))
        line-start-for-node (fn line-start-for-node[node]
                              (line-start-for-position (start-index node)))
        column-for-node (fn[node] (let [my-start-index (start-index node)]
                                    (- my-start-index (line-start-for-position my-start-index))))
        ]
    (into [] (map (fn[line] 
                    (if-not (is-sargam-line? line)
                      line
                      (postwalk (fn[item] 
                                  (when false (println "**** item" item))
                                  (when false (println "**** start-index item" (start-index item)))
                                  (cond 
                                    (and (vector? item) (is-pitch? item) (< (start-index item) main-line-start-index))
                                    item
                                    (and (vector? item) 
                                         (takes-values-from-column? (first item)))
                                    (let [column (column-for-node item)
                                          nodes-in-this-column 
                                          (filter (fn[x] (#{:upper-octave-dot :upper-upper-octave-symbol
                                                            :lower-octave-dot 
                                                            :lower-lower-octave-symbol :ornament 
                                                            :chord
                                                            :tala
                                                            :alternate-ending-indicator 
                                                            :mordent} (first x))) 

                                                  (remove (partial in-assigned? @assigned) (column-map column [])))
                                          _ (when false (println "nodes-in-this-column" nodes-in-this-column))
                                          ]
                                      (if (not-empty nodes-in-this-column)
                                        (do
                                          (when false (println "*********conjing"))
                                          (reset! assigned (apply conj @assigned (map unique-for-assigned nodes-in-this-column)))
                                          (apply conj item nodes-in-this-column) 
                                          )
                                        item))
                                    true
                                    item
                                    )) line)))
                  section 
                  ))))

(defn collapse-section[section]
  {:post [(do (when false (println "leaving collapse-section, returns")
                (pprint %))
              true)]}
  (when false (println "collapse-section, section=") (pprint section))
  (reset! assigned #{})
  ;;  (first (filter is-main-line?
  (into [] (->> section 
                handle-slurs
                assign-ornament-octaves
                assign-to-main-line
                assign-syllables
                (remove (fn[x] (and (vector? x) (#{:upper-octave-line :lower-octave-line} (first x)))))
                )))

;;))

(defn remove-ids[obj saved]
  {
   :pre [(is-composition? obj)]
   :post [
          (is-composition? %)
          ]
   }
  (postwalk (fn[x] 
              ;; (println "x is" x)
              (cond (and (vector? x) (keyword? (first x)) (number? (second x)) (> (count x) 2))
                    (into [] (concat [(first x)] (rest (rest x))))
                    (and (vector? x) (keyword? (first x)) (number? (second x)) (= (count x) 2))
                    [(first x)]
                    ;;  (list? x)
                    ;; (into [] x)
                    true                    
                    x
                    )) obj))





(defn zip-test[composition]
  (pprint composition)
  )

;;(-> "S- -R\nHi" experiment)






(defn tree-edit [zipper]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (do
        (pprint (zip/node loc))
        (recur (zip/next loc))))))

;;(-> "S- -R" experiment zip/vector-zip tree-edit)

(def runtest false)
(def t "Title:test\n\n.\nS\n.\nHi\n\nSRG" )
;;(def c (doremi-text->parse-tree 
;;        t         
;;       ))

;; ".\n.\nS#\n.\nHi"))
(def z
  "Hi: John\n\n.\nR GmPD\n.\n:\n*\n-S--\nYes-")



;;;;    #(ly:set-option 'midi-extension "mid")
;;;;    \version "2.12.3"
;;;;    \include "english.ly"
;;;;    \header{ 
;;;;    title = ""
;;;;    composer = ""
;;;;      tagline = ""  % removed 
;;;;    }
;;;;    %{
;;;;     Key: D
;;;;    Mode: phrygian          
;;;;    
;;;;                                    i            IV         . 
;;;;             3              n       +            2         DSnDn
;;;;    1)|: (Sr | n) S   (gm <P  d)> | P - P  P   | P - D    n     |
;;;;               .
;;;;          ban-    su-  ri          ba- ja ra-   hi  dhu- na 
;;;;     %}
;;;;      
;;;;    melody = {
;;;;    \once \override Staff.TimeSignature #'stencil = ##f
;;;;    \clef treble
;;;;    \key c \phrygian
;;;;    \autoBeamOn  
;;;;    \cadenzaOn
;;;;    \bar "|:" c'8([ df'8] \bar "|" bf4) c'4 ef'8([ f'8] \afterGrace g'8 { bf'32 } af'8) \bar "|" g'2^"i" g'4 g'4 \bar "|" g'2^"IV" a'4 \afterGrace bf'4 { a'32[ c''32 bf'32 a'32 bf'32] } \bar "|"  \break        
;;;;    
;;;;    }
;;;;    
;;;;    text = \lyricmode {
;;;;      ban- su- ri ba- ja ra- hi dhu- na
;;;;    }
;;;;    
;;;;    \score{
;;;;    \transpose c' d'
;;;;    <<
;;;;      \new Voice = "one" {
;;;;        \melody
;;;;      }
;;;;      \new Lyrics \lyricsto "one" \text
;;;;    >>
;;;;    \layout {
;;;;      \context {
;;;;           \Score
;;;;        \remove "Bar_number_engraver"
;;;;      } 
;;;;      }
;;;;    \midi { 
;;;;      \context {
;;;;        \Score
;;;;        tempoWholesPerMinute = #(ly:make-moment 200 4)
;;;;       }
;;;;     }
;;;;    }
;;;;    


(defn map-even-items [f coll]
  (map-indexed #(if (zero? (mod %1 2)) (f %2) %2) coll))

(defn handle-source[accum source]
  (assoc accum :source (last source))
  )

(defn lilypond-escape[x]
  ;; (println "x is " x)
  ;;      # Anything that is enclosed in %{ and %} is ignored  by lilypond
  ;;      composition-data.source="" if !composition-data.source?
  ;;      src1= composition-data.source.replace /%\{/gi, "% {"
  ;;      src= src1.replace /\{%/gi, "% }"
  (clojure.string/replace x #"\{%" "% {")
  ) 

(defn print-headers[accum composition]
  (let [ atts (first (filter is-attribute-section? composition))
        ;; _ (pprint atts) 
        my-map (apply array-map (map-even-items lower-case (rest atts)))
        source (second (first (filter #(= :source (first %)) (rest composition))))
        _ (when false (println "Source is\n" source))
        ;; _ (pprint my-map)
        ] 
    (update-in accum [:output] str 
               (join "\n"
                     [
                      (str "title = \"" (get my-map "title" ) "\"")
                      (str "composer = \"" (get my-map "composer" ) "\"")
                      "}" ;; see ....
                      "%{"
                      (lilypond-escape (:source accum))
                      "%}"
                      "melody = {"
                      "\\once \\override Staff.TimeSignature #'stencil = ##f"
                      "\\clef treble"
                      (str "\\key c " (str "\\" (get my-map "mode" "major")))
                      "\\autoBeamOn"
                      "\\cadenzaOn"
                      "\n"
                      ] 
                     ) "\n")))



(def pitch->lilypond-pitch
  ;; TODO: double sharps and flats, half-flats ??"
  ;; includes dash (-) -> r "
  {
   "-" "r", "S" "c", "S#" "cs", "Sb" "cf", "r" "df", "R" "d", "R#" "ds",
   "g" "ef", "G" "e", "G#" "es", "m" "f", "mb" "ff", "M" "fs", "Pb" "gf",
   "P" "g", "P#" "gs", "d" "af", "D" "a", "D#" "as", "n" "bf", "N" "b",
   "N#" "bs", })


(defn pitch->octave[pitch]
  {
   :pre [ (is-pitch? pitch)] 
   :post [ (do (when false (println "pitch->octave" %)) true) ] }
  ;; OMG
  (->> pitch (filter vector?) 
       (map first) 
       (map {:upper-octave-dot 1
             :upper-upper-octave-symbol 2
             :lower-octave-dot -1
             :lower-lower-octave-symbol -2 }
            )
       (remove nil?)
       (apply +))
  )

(defn beat->beat-divisions[beat]
  {:pre [(is-beat? beat)]
   :post [ (integer? %)]
   }
  ;; OMG
  (->> beat (filter vector?) (map first) (filter #{:pitch :dash}) count) 
  )

(defn start-beat[accum beat]
  (assoc accum 
         :divisions (beat->beat-divisions beat)
         :beat-pitches []
         )
  )

(defn barline->lilypond-barline[
                                [_ [barline-type] ] ;; destructuring fun
                                ]
  " maps barline-type field for barlines"
  (let [my-map
        {
         :reverse-final-barline "\\bar \".|\""
         :final-barline "\\bar \"|.\" "
         :double-barline "\\bar \"||\" " 
         :single-barline "\\bar \"|\"" 
         :left-repeat "\\bar \"|:\"" 
         :right-repeat "\\bar \":|\"" 
         } ]
    (str (get my-map barline-type (:single-barline my-map)) " ")
    ))

(defn octave-number->lilypond-octave[num]
  (let [tick "'"
        comma ","]
    ;; Middle c is c'
    (cond (nil? num)
          tick
          (>= num 0)
          (apply str (take (inc num) (repeat tick)))
          true
          (apply str (take (dec (- num)) (repeat comma))))))

(defn start-pitch[accum pitch]
  (when false (println "start-pitch, pitch is") (pprint pitch))
  (let [pitch-and-octave 
        (str (pitch->lilypond-pitch (second pitch))
             (->> pitch pitch->octave octave-number->lilypond-octave)                                     )
        ]
    (update-in (assoc accum :state :collecting-pitch-in-beat
                      :current-pitch  { :obj pitch  :micro-beats 1}
                      :lilypond-pitch-and-octave pitch-and-octave

                      ) [:output] str " " ) 
    ))

(defn start-line[accum obj]
  ;; TODO
  accum
  )

(defn emit-barline[accum barline]
  (update-in accum [:output] str  " "
             (barline->lilypond-barline barline)
             )
  )
(defn lilypond-headers[accum composition]
  (update-in accum [:output] str 
             (join "\n" [
                         "#(ly:set-option 'midi-extension \"mid\")"
                         "\\version \"2.12.3\""
                         "\\include \"english.ly\""
                         "\\header{" 
                         ] ) "\n" ))


(defn ratio->lilypond-durations[my-numerator subdivisions-in-beat]
  { :pre [ (integer? my-numerator)
          (integer? subdivisions-in-beat)]
   :post [ (vector? %)] 
   }
  " ratio->lilypond-durations(3 4) => ["8."]   Ratio is ratio of "
  "quarter note" 

  (let [my-ratio (/ my-numerator subdivisions-in-beat)]
    ;; In the case of beats whose subdivisions aren't powers of 2, we will
    ;; use a tuplet, which displays, for example, ---3---  above the beat
    ;; if the beat has 3 microbeats.
    ;; Take the case of  S---R. beat is subdivided into 5.  Use sixteenth notes. 4 for S and 1 for R. 5/16 total.  
    ;; For subdivision of 3, use 3 1/8 notes.
    ;; For subdivision of 5 use 5 1/16th notes.
    ;; For 6 use   16th notes
    ;; etc
    ;; For over 32 use 32nd notes, I guess.
    ;; confusing, but works
    ;; Things like S---r should map to quarter note plus sixteenth note in a 5-tuple
    ;; Take the case of S----R--   
    ;; S is 5 microbeats amounting to 5/32nds. To get 5 we have to tie either
    ;; 4/8th of a beat plus 1/32nd  or there are other approaches.
    (if (not (ratio? my-ratio))
      ({ 1 ["4"]
        2 ["2"]
        3 ["2."]
        4 ["1"]  ;; review
        8 ["1" "1"]
        } my-ratio
       (into [] (repeat my-numerator "4"))
       )
      ;; else
      (let [ 
            my-table
            { 1 ["4"] ;; a whole beat is a quarter note
             (/ 1 2) ["8"] ;; 1/4 of a beat is 16th
             (/ 1 4) ["16"] ;; 1/4 of a beat is 16th
             (/ 1 8) ["32"] ;; 1/8th of a beat is a 32nd. 
             (/ 1 16) ["64"] ;; 1/16th of a beat is 64th. 16/64ths=beat
             (/ 1 32) ["128"] ;; 32nd of a beat is 128th note
             (/ 3 4) ["8."] ;; 3/4 of a beat is  dotted eighth
             (/ 3 8) ["16."] ;; 
             (/ 3 16) ["32."] ;;  1/32 + 1/64 = 3/64 =3/16th of beat = 3/64 dotted 32nd
             (/ 3 32) ["64."]
             (/ 3 64) ["128."]
             (/ 5 4) ["4" "16"] ;; 1 1/4 beats= quarter tied to 16th
             (/ 5 8) ["8" "32"]
             (/ 5 16) ["16" "64"];;
             (/ 5 32) ["32" "128"];;
             (/ 5 64) ["64" "256"];;
             (/ 5 128) ["128" "512"];;
             (/ 7 4) ["4" "8."] ;;
             (/ 7 8) ["8.."] ;; 1/2 + 1/4 + 1/8  
             (/ 7 16) ["16" "32."] ;; 1/4+ 3/16   
             (/ 7 32) ["64" "128."] ;;   
             (/ 11 16) ["8" "64."] ;; 1/2 + 

             } 
            ;;  
            new-denominator 
            (cond (#{1 2 4 8 16 32 64 128 256 512} subdivisions-in-beat)
                  subdivisions-in-beat
                  (= 3 subdivisions-in-beat) 
                  2 
                  (<  subdivisions-in-beat 8)
                  4 
                  (< subdivisions-in-beat 16)
                  8 
                  (< subdivisions-in-beat 32)
                  16 
                  (< subdivisions-in-beat 64)
                  32 
                  true
                  32 
                  )
            new-ratio (/ my-numerator new-denominator)
            ]
        (get my-table new-ratio 
             [
              (str "unsupported: " my-numerator "/" new-denominator)
              ]) 
        ))))

(defn tuplet-numerator-for-odd-subdivisions[subdivisions-in-beat]
  ;; fills in numerator for times. For example
  ;; \times ???/5 {d'16 e'8 d'16 e'16 }
  ;; The ??? should be such that 5/16 *  ???/5 =  1/4
  ;; So ??? = 4
  ;; TODO: dry with duration code
  (cond (= 3 subdivisions-in-beat) 
        2 
        (<  subdivisions-in-beat 8)
        4 
        (< subdivisions-in-beat 16)
        8 
        (< subdivisions-in-beat 32)
        16 
        (< subdivisions-in-beat 64)
        32 
        true
        32 
        )
  )

(defn enclose-beat-in-times[beat-str subdivisions]
  {
   :pre [(string? beat-str)
         (integer? subdivisions)]
   :post [(string? %)]
   }
  (if (not (#{0 1 2 4 8 16 32 64 128} subdivisions))
    (str "\\times "
         (tuplet-numerator-for-odd-subdivisions 
           subdivisions)
         "/" 
         subdivisions
         "{ "
         beat-str " } ")
    beat-str))


(defn finish-beat[accum]
  (when false (println "finish-beat"))
  (assoc accum
         :output (str  (:output accum) " " 
                      (enclose-beat-in-times (join " " (:beat-pitches accum))
                                             (:divisions accum)))
         :beat-pitches []
         :divisions 0))



(defn finish-dashes[accum]
  (when false
    (println "finish-dashes")
    (pprint (remove :output accum)))

  (let [divisions (accum :divisions)
        _ (when false (println "divisions=" divisions))
        micro-beats (get-in accum [:dash-microbeats])
        _ (when false (println ":dash-microbeats=" micro-beats))
        ary (ratio->lilypond-durations micro-beats divisions)
        pitch-and-octave (:lilypond-pitch-and-octave accum)
        rests (join " " (map (partial str "r") ary))
        ]
    (when false  
      (println "finish-dashes  divisions=" divisions "; micro-beats= " micro-beats)
      (pprint ary))
    (update-in accum [:beat-pitches] conj rests )
    ))


(defn start-dash[accum dash]
  (assoc accum :state :collecting-dashes-in-beat
         :dash-microbeats 1
         ))


(defn finish-pitch[accum]
  (when false (println "finish-pitch"))
  (when false (pprint (remove :output accum)))
  (let [divisions (accum :divisions)
        micro-beats (get-in accum [:current-pitch :micro-beats])
        ary (ratio->lilypond-durations micro-beats divisions)
        pitch-and-octave (:lilypond-pitch-and-octave accum)
        pitches (join " " (map (partial str pitch-and-octave) ary))
        ]
    (when false 
      (println "finish-pitch  divisions=" divisions "; micro-beats= " micro-beats)
      (pprint ary))
    (when false (println "leaving finish-pitch:" (update-in accum [:beat-pitches] conj pitches )))
    (update-in accum [:beat-pitches] conj pitches )
    ))

(defn finish-line[accum]
  (update-in accum [:output] str (join "\\break" "\n"))
  )

(defn lilypond-at-eof[accum]
  (update-in accum [:output] str  
             (join "\n" [
                         "\n}\n\n" ;; end of melody
                         "text = \\lyricmode {"
                         (join " "

                               (->> (:composition accum)  (tree-seq vector? identity)  (filter is-pitch?) (filter #(> (count %) 2)) (map last) (filter string?))
                               ) 
                         "}\n"
                         "\\score{"
                         "\\transpose c' d'"
                         "<<"
                         "\\new Voice = \"one\" {"
                         "\\melody"
                         "}"
                         "\\new Lyrics \\lyricsto \"one\" \\text"
                         ">>"
                         "\\layout {"
                         "\\context {"
                         "\\Score"
                         "\\remove \"Bar_number_engraver\""
                         "}"
                         "}"
                         "\\midi {"
                         "\\context {"
                         "\\Score"
                         "tempoWholesPerMinute = #(ly:make-moment 200 4)"
                         "}"
                         "}"
                         "}"
                         ]))

  )

(defn lilypond-transition[accum obj]
  { :pre [ (do (when false (println "first obj=" (first obj))) true)
          (map? accum)
          (#{:lyrics-section :lyrics-line :sargam-section :pitch :barline :measure :sargam-line :line-number :composition
             :beat :dash :output :eof :attribute-section :source} (first obj))
          (:output accum)
          (:state accum)
          (keyword? (:state accum))
          (vector obj)]}
  (let [token (first obj)
        cur-state (:state accum)
        _ (when false (println "Entering lilypond-transition\n" "state,token=" cur-state token))
        ]
    (case [cur-state token]
      [:start :composition]
      (-> accum (lilypond-headers obj) (assoc :state :looking-for-source))

      [:looking-for-source :source]
      (-> accum (handle-source obj) (assoc :state :looking-for-attribute-section))

      [:in-sargam-section :sargam-line]
      (-> accum (assoc  :state :in-sargam-line)) ;; review:lyrics (rest (last obj)))

      [:looking-for-attribute-section :lyrics-section]
      accum

      [:looking-for-attribute-section :sargam-section]
      (-> accum (print-headers (:composition accum)) (assoc
                                                       :state :in-sargam-section))

      [:looking-for-attribute-section :attribute-section]
      (assoc accum :state :looking-for-attribute-section) ;; support multiple attribute sections 

      [:in-sargam-line :measure]
      (assoc accum :current-pitch nil) ;; maybe need to save current pitch

      [:in-sargam-line :beat]
      (-> accum (start-beat obj) (assoc :state :in-beat))

      [:in-sargam-line :barline]
      (-> accum (emit-barline obj) (assoc :state :in-sargam-line))

      [:in-sargam-line :line-number]
      accum ;; no lilypond mapping yet

      [:in-sargam-line :sargam-section]
      (-> accum finish-line (assoc :state :in-sargam-section))

      [:looking-for-sargam-section :eof]
      (-> accum lilypond-at-eof  (assoc :state :eof))

      [:looking-for-sargam-section :sargam-section]
      (-> accum (assoc :state :in-sargam-section))

      [:in-sargam-line :lyrics-line]
      (assoc accum :state :looking-for-sargam-section)

      [:collecting-pitch-in-beat :lyrics-line] ;; needed?
      accum

      [:collecting-pitch-in-beat :eof]
      (-> accum finish-pitch finish-beat finish-line lilypond-at-eof)

      [:in-sargam-line :eof]
      (-> accum finish-line lilypond-at-eof (assoc :state :eof))

      [:in-beat :eof]
      (-> accum finish-beat  (assoc :state :eof))

      [:in-beat :pitch]
      (start-pitch accum obj)

      [:in-beat :dash]  ;; dash at beginning of beat
      (start-dash accum obj)

      [:collecting-dashes-in-beat :eof]
      (-> accum finish-dashes finish-beat lilypond-at-eof)

      [:collecting-dashes-in-beat :beat]
      (-> accum finish-dashes finish-beat (start-beat obj) (assoc :state :in-beat))

      [:collecting-dashes-in-beat :dash]
      (-> accum (update-in [:dash-microbeats] inc))

      [:collecting-dashes-in-beat :pitch]
      (-> accum finish-dashes (start-pitch obj) (assoc :state :collecting-pitch-in-beat)) 



      [:collecting-dashes-in-beat :sargam-section]
      (-> accum finish-dashes finish-beat finish-line (assoc :state :in-sargam-section)) 



      [:collecting-dashes-in-beat :barline]
      (-> accum finish-dashes finish-beat (emit-barline obj) (assoc :state :in-sargam-line))

      [:collecting-pitch-in-beat :barline]
      (-> accum finish-pitch finish-beat (emit-barline obj)
          (assoc :state :in-sargam-line))

      [:collecting-pitch-in-beat :pitch]
      (-> accum finish-pitch (start-pitch obj)
          (assoc :state :collecting-pitch-in-beat))

      [:collecting-pitch-in-beat :dash]
      (-> accum (update-in [:current-pitch :micro-beats] inc))

      [:collecting-pitch-in-beat :beat]
      (-> accum finish-pitch finish-beat (start-beat obj) (assoc :state :in-beat))
      ;; tie previous note if new one is a dash!!!
      )))



(defn new-to-lilypond[composition]
  (when false (println "new-to-lilypond") (pprint composition))
  (let [headers-printed? (atom false)
        started-pitch? (atom false) 
        ]
    (->> (conj composition [:eof]) (tree-seq  #(and (vector? %)
                                                    (#{:sargam-section :lyrics-line :composition :sargam-line :measure :beat} (first %)))
                                             identity)
         (filter vector?)
         (reduce lilypond-transition
                 {:state :start 
                  :output ""
                  :composition composition}
                 ))))


(defn experiment[txt]
  (when false 
    (println "parsing") 
    (println txt)
    )
  ;;  (pprint (doremi-text->parse-tree "  S S\nHello john"))
  ;;(println "\n\n\n")
  (let [
        parsed  (doremi-text->parse-tree txt)
        _ (if nil (do (println "parsed:") (pprint parsed)))
        ]
    (if (map? parsed)  ;; error
      (pprint parsed)
      (let [
            [saved parse-tree] (add-ids-and-create-map parsed)
            collapsed-parse-tree
            (remove-ids (into [] (map #(if (is-sargam-section? %)
                                         (collapse-section %)
                                         %)
                                      parse-tree)) saved)
            collapsed2 (into [] (concat (subvec collapsed-parse-tree 0 1)
                                        [[:source txt]]
                                        (subvec collapsed-parse-tree 1)))
            _ (when false;; (pprint )
                (println "****collapsed-parse-tree")
                (pprint collapsed2) (println "\n\n"))
            ]
        nil 
        (:output (new-to-lilypond collapsed2))
        )))) 

;;(->  "fixtures/bansuriv3.txt" resource slurp experiment)


;;  (println (:output (new-to-lilypond collapsed-parse-tree))))))) 
;;(println (experiment "Title:John\n\nS"))
(when false
  (pprint (experiment ;;doremi-text->parse-tree 
                      (join "\n" [
                                  ;; " ..   ..." 
                                  " SR. .GmP"
                                  "(  G m    ) m-(P d)-  | S :|"
                                  "\n"
                                  "1) SS"
                                  ])
                      )))



(defn test-aux[file-spec]
  (->> file-spec slurp experiment (spit (str "./test/test_results/" (.getName file-spec) ".ly")))
  (.getName file-spec)) 



(defn test-all2-process-file[my-file]
  (let [ basename (.getName my-file) 
        filename (str "test/test_results/" basename)
        ]
    (io/copy my-file (io/file  filename))
    (->> my-file slurp 
         experiment (spit (str filename ".ly")))
    ))


(defn test-all2[resource-names]
  { :pre [(vector? resource-names)
          (every? string? resource-names) ]
   }
  "Takes a list of resource names, as in "
  ;; ["resources/fixtures/foo.txt" "resources/fixtures/bar.txt"]
  ;; Will try and correct the paths to find the resource.
  (let [resource-paths
        (map (fn get-fixture-resource-path[my-str]
               (if (.contains my-str "fixtures")
                 (clojure.string/replace my-str #".*fixtures" "fixtures")
                 (str "fixtures/"
                      (last  (clojure.string/split my-str #"\/")))))
             resource-names) 
        _ (when false (pprint resource-paths))
        file-specs (map 
                     #(-> % io/resource io/file) resource-paths)
        _ (when false pprint file-specs)
        ]
    (dorun (map test-all2-process-file file-specs))
    resource-paths
    ))

;; (test-all2 ["bansuriv3.txt"])
(defn test-all[dir-str match-str]
  (println "test-all " dir-str " " match-str)
  (map  test-aux
       (filter #(.matches (.getName %) match-str)(file-seq (clojure.java.io/file dir-str)))))


(when false (pprint  (test-all "resources/fixtures" ".*.txt")))
(when false (pprint  (test-all "resources/fixtures" "you_can_add.*.txt")))


;;(pprint (experiment (slurp (resource "fixtures/no_syls_yesterday.txt"))))
(when nil (pprint (experiment (slurp (resource "fixtures/aeolian_mode_in_specific_key.txt")))))
;;no_syls_yesterday.txt"))))
;;(pprint (experiment (slurp (resource "fixtures/bansuriv3.txt"))))
;;;
;;(->  "fixtures/bansuriv3.txt" resource slurp doremi-text->parse-tree pprint)
;;

(when nil 
  ;;(pprint (experiment (slurp (resource "fixtures//georgia.doremiscript.txt"))))
  (pprint (experiment (slurp (resource "fixtures/bansuriv3.txt"))))
  (println (experiment ".\nS\n")))
;; (join "\n" ["(Sr | n)"
;;        "      ."
;;       "HI"])
