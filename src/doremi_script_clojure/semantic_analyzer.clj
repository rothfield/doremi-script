(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [clojure.pprint :refer [pprint]] 
            [clojure.walk :refer [postwalk postwalk-replace keywordize-keys]]
            [instaparse.core :as insta]
            ))


(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.semantic-analyzer :reload)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (ns doremi_script_clojure.semantic-analyzer)
  (use 'clojure.stacktrace)
  (print-stack-trace *e)
  (pst)
  )
(def debug false)
(defn start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. instaparse adds the metadata in the parsing process"
  "Returns the character position in the source code where the object starts"
  (first (instaparse.core/span z))    
  )


(defn my-raise[x]
  (pprint x)
  (throw (Exception. (str x))))

(defn line-column-map [my-map line]
  "my-map is a map from column number -> list of nodes
  Add nodes from line to the map"
  (reduce (fn [accumulator node]
            (let [column (- (start-index node) 
                            (start-index line))]

              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(not= nil (start-index %)) (tree-seq vector? rest line))
          )) 


(defn section-column-map [sargam-section]
  "For a sargam section, maps column-number --> list of nodes in that column"
  "Note that section-content consists of something like
  [:SARGAM_SECTION upper-line sargam-line lyrics-line ]"

  (reduce line-column-map {}  sargam-section)
  )

(defn- update-sargam-ornament-pitch-node [sargam-ornament-pitch nodes]
  "UNFINISHED"
  " TODO: Apply the upper and lower dots found in the upper lines.
  Dots will be in same column. If positioned afterward, becomes lower dot.
  If positioned before, becomes upper dot
  If there is a sargam pitch in this column then things get more complicated. 
  For now, don't apply dots that are 'under' the ornament pitch if there is a
  sargam pitch there.
  "
  (let [
        content (rest sargam-ornament-pitch)
        has-sargam-pitch (first  (filter #(and (vector? %) (=(first %) :SARGAM_PITCH)) nodes))
        upper-dots (if has-sargam-pitch
                     ()  ;; return no dots if has sargam-pitch
                     (filter #(and (vector? %) (=(first %) :UPPER_OCTAVE_DOT)) nodes)
                     )

        ]
    sargam-ornament-pitch
    ))

(def sample-beat
  "S-RG"
  '[:BEAT [:PITCH_WITH_DASHES [:SARGAM_PITCH [:S "S"]] [:DASH "-"]] [:SARGAM_PITCH [:R "R"]] [:SARGAM_PITCH [:G "G"]]]
  )

(def unit-of-rhythm
  #{ :SARGAM_PITCH  :DASH}
  )

(def to-normalized-pitch
  {
   :S "C"
   :r "Db"
   :R "D"
   :g "Eb"
   :G "E"
   :m "F"
   :M "F#"
   :P "G"
   :d "Ab"
   :D "A"
   :n "Bb"
   :N "B"
   })

(defn unit-of-rhythm?[x]
  ;; (println "unit-of-rhythm")
  ;;(pprint x)
  ;;(println "unit-of-rhythm") 
  (unit-of-rhythm (first x))
  )

(defn unusedtransform-sargam-pitch1 [sargam-pitch-content]
  "TODO: maybe- do this"
  "Initial transformation, make second entry a hash and add 
  normalized pitch"
  (into [] (concat [:SARGAM_PITCH]
                   {
                    :pitch_source (second sargam-pitch-content)
                    :source (second sargam-pitch-content)
                    :normalized_pitch (to-normalized-pitch (first sargam-pitch-content))
                    }))
  )

(defn transform-beat [beat]
  ;;(my-raise "tranform-beat")
  (if false
    (do
      (println "entering transform-beat beat ")
      (pprint beat)
      (println "entering transform-beat")
      ))
  (let [beat-content (rest beat)
        divisions (count (filter #(unit-of-rhythm %) (flatten beat-content)))
        transform-sargam-pitch-with-dashes
        (fn [sargam-pitch-with-dashes]
          sargam-pitch-with-dashes)
        ]
    (conj beat [:divisions divisions])
    ))

(defn- update-sargam-pitch-node [sargam-pitch nodes]
  " Use the list of nodes to update a sargam-pitch.
  Returns the new node."
  (let [;; TODO: DRY
        content (rest sargam-pitch)
        mordent (last (filter #(and (vector? %) (= (first %) :MORDENT)) nodes))
        syls (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
        upper-upper-dots (filter #(and (vector? %) (=(first %) :UPPER_UPPER_)) nodes)
        upper-dots (filter #(and (vector? %) (=(first %) :UPPER_OCTAVE_DOT)) nodes)
        upper-upper-dots (filter #(and (vector? %) (=(first %) :UPPER_UPPER_OCTAVE_SYMBOL)) nodes)
        lower-dots (filter #(and (vector? %) (=(first %) :LOWER_OCTAVE_DOT)) nodes)
        lower-lower-dots (filter #(and (vector? %) (=(first %) :LOWER_LOWER_OCTAVE_SYMBOL)) nodes)
        chords (map second (filter #(= :CHORD (first %)) nodes))
        tala (second (last (filter #(and (vector? %) (=(first %) :TALA)) nodes)))
        ornaments (filter #(and (vector? %) (=(first %) :SARGAM_ORNAMENT)) nodes)
        octave  (+ (count upper-dots) 
                   (- (count lower-dots))
                   (* 2 (count upper-upper-dots))
                   (* -2 (count lower-lower-dots))
                   )
        ]
    ;; Note that we support multiple syllables, ornaments, and chords per note. But only return the last (for now)
    (assoc sargam-pitch 1
           {
            :pitch_source
            (second content)
            :source
            (second content)
            :normalized_pitch
            (to-normalized-pitch (first content))
            :syllable
            (second (last syls))
            :octave
            octave
            :chord
            (last chords)
            :ornament   ;; just the pitches
            (into (vector) (rest (last ornaments)))
            :tala
            tala
            :mordent
            (second mordent)
            }
           )))

(defn update-sargam-line-node[sargam-line nodes]
  "add syllables attribute to sargam line"
  (if debug
    (do
      (pprint "---------update-sargam-line-node -----------------")
      (pprint "---------nodes:-----------------")
      (pprint nodes)
      (pprint "-------------------")
      ))
  (let [ syllables (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
        ]
    (if debug (do (println "syllables" syllables)))
    (conj sargam-line [ :syllables  syllables])))


(defn extract_sargam_line_from_sargam_section[sargam-section]
  (assert (= (:SARGAM_SECTION (first sargam-section))))
  (first (filter #(= :SARGAM_LINE (first %)) (rest sargam-section)))
  )

(comment parse2[txt]
         "parse and run through semantic analyzer. Txt is doremi-script"
         ;;(let [
         ;;x1 ( (comp assign-attributes get-parser2) txt)
         ;; parse-tree2
         ;;   (insta/transform {:SARGAM_PITCH transform-sargam-pitch1} parse-tree)
         ;; z (my-raise parse-tree2)
         ;;  x1 (assign-attributes parse-tree)
         ;;sargam-sections (filter #(= :SARGAM_SECTION (first %))  (rest x1))
         ;; attribute-sections (filter #(= :ATTRIBUTE_SECTION (first %))  (rest x1))
         ;; lyrics-sections  (filter #(= :LYRICS_SECTION (first %))  (rest x1))
         ;;lines  (map  extract_sargam_line_from_sargam_section sargam-sections)
         ;;     ]
         ; { :attributes attribute-sections
         ;;  :lines1 sargam-sections
         ;  :lines lines
         ;  :lyrics lyrics-sections
         ;  :source txt
         ;  }
         ; x1
         )

;;(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nThese are all the lyrics\nSecond lyric line\n\nRG.\nS | R | G\nhe-llo\n\n |m P D - |\n\nMore:attributes"))
;; (pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (get-parser2 (slurp-fixture "yesterday.txt")))


;;(get-parser2 "S--R--" :start :BEAT)


;;(pprint (get-parser2 "S-RG"))

(def z (get-parser2 "title:Hello\nAuthor: John\n\nS\n\nR"))



(defn old-main[]
  ;; (into {} (for [i line-start j (range 0 (count line-start))] [i j]))
  ;; Use above to turn list of line starts to map of linestart --> line number
  (let [txt1  "title:yesterday\n\nSRG\r\n\nmPD"
        txt (clojure.string/replace txt1 #"\r\n" "\n")
        line-starts 
        (concat '(0) (keep-indexed (fn [index value](if  (= value \newline) index)) txt))


        parse-tree3 (merge (get-parser2 txt)
                           {:source txt :line-starts line-starts})
        ]
    ;; (pprint parse-tree3)
    ;; add source
    (apply sorted-map parse-tree3)
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;  New Code  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn collapse-sargam-section[sargam-section]
  "Deals with the white-space significant aspect of doremi-script"
  "given a section like

  .
  S
  Hi

  "
  "Returns the sargam-line with the associated objects in the same column attached
  to the corresponding pitches/items on main line"
  "Assign attributes to the main line(sargam_line) from the lower and upper lines using 
  column numbers. Returns a sargam-line"
  (let [
        sargam-section-content (rest sargam-section)
        sargam-line (first (filter #(and (vector? %) (= :SARGAM_LINE (first %))) sargam-section-content))
        lyrics-line (first (filter #(and (vector? %) (= :LYRICS_LINE (first %))) sargam-section-content))
        lyrics  (filter #(and (vector? %) (= :SYLLABLE (first %))) (tree-seq vector? rest lyrics-line))
        column-map (section-column-map sargam-section)
        line-starts (map start-index sargam-section-content)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node))))
        my-fn (fn[x]
                (if
                  (not (vector? x))
                  x
                  ;; else
                  (let [
                        column (column-for-node x)
                        nodes (get column-map column) 
                        ]
                    (cond
                      (= :SARGAM_PITCH (first x))
                      (update-sargam-pitch-node x nodes)
                      (= :SARGAM_ORNAMENT_PITCH (first x))
                      (update-sargam-ornament-pitch-node x nodes)
                      (= :BEAT (first x))
                      (transform-beat x)
                      true
                      x))))
        sargam-section-content2 (postwalk my-fn sargam-section-content)
        modified-sargam-line (first (filter #(= :SARGAM_LINE (first %)) sargam-section-content2))
        ]
    (conj modified-sargam-line (into [:lyrics] lyrics))
    ))



(defn fix-items-keywords[parse-tree]
  "replace keywords like :COMPOSITION_ITEMS with :items"  
  (let [my-map 
        (apply hash-map (mapcat vector 
                                '(:COMPOSITION_ITEMS
                                   :SARGAM_LINE_ITEMS
                                   :ATTRIBUTE_SECTION_ITEMS
                                   :BEAT_DELIMITED_ITEMS
                                   :BEAT_UNDELIMITED_ITEMS
                                   :MEASURE_ITEMS
                                   :LYRICS_LINE_ITEMS
                                   :SARGAM_ORNAMENT_ITEMS
                                   )
                                (repeat :items))) 
        ]
    (postwalk-replace my-map parse-tree)
    ))


(defn process-attribute-section[x]
  (comment "x looks like"
           [:ATTRIBUTE_SECTION [:ATTRIBUTE_SECTION_ITEMS "title" "Hello"]]
           )
  (comment 
    "returns"
    [:attributes {:title "Hello"}]
    )
  (assoc x 1 (keywordize-keys (apply array-map (rest (second x))))))

(defn my-transforms[parse-tree]
  (let [my-fn
        (fn[x]
          (cond
            (not (vector? x))
            x
            (= :ATTRIBUTE_SECTION (first x))
            (process-attribute-section x)
            (= :SARGAM_SECTION (first x))
            (collapse-sargam-section x)
            true
            x))
        ]
    (postwalk my-fn parse-tree))) 

;;  (my-transforms (get-parser2 "n\nS\nhello"))
;(pprint (my-transforms (get-parser2 "title:hi\n\nSRG-\nhello\n\nmPD")))
;; (pprint (my-transforms (get-parser2 "S\n\nR")))
;;(pprint (get-parser2 "S\n\nR"))
;;(pprint  (postwalk items-fix (get-parser2 "title:hi\n\nSRG-")))


;;(pprint (parse2 "title:hi\n\nSR\nS"))
;;
(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )

(defn add-source[txt parse-tree]
  (conj parse-tree [:source txt])
  )

(defn run-through-parser[txt]
  (get-parser2 txt)
  )

(defn main[txt]
  (let [
        txt2  (rationalize-new-lines txt)

        parse-tree (  (comp my-transforms fix-items-keywords (partial add-source txt2) run-through-parser) txt2) 
        ;;((my-transforms (fix-items-keywords (add-source (run-through-parser (rationalize-new-lines txt2)) txt2)))
        ]
    parse-tree
    ))
(pprint (main "title:hi\n\nSR\nS"))
;;(pprint (run-through-parser "S"))
;;(pprint (rationalize-new-lines "\nhi\n\r\n"))
;;
;;  Pseudo code:
;;
;;  given a string:
;;
;;  rationalize new lines => txt
;;  run through parser => parse-tree
;;  add source
;;  add column numbers
;;  fix items => parse-tree  tree-walk ???
;;  process sargam sections  tree-walk ?? 
;;  turn into hash equivalent of the javascript version

; generate json
;
;
