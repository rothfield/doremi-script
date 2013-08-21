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

(defn start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. instaparse adds the metadata in the parsing process"
  (first (instaparse.core/span z))    
  )


(defn my-raise[x]
  (pprint x)
  (throw (Exception. (str x))))

(defn all-nodes[tree]
  "returns all nodes in the tree that are not strings"

  (filter #(sequential? %) 
          (tree-seq  #(or (vector? %) (list? %)) rest tree))
  )


(defn line-column-map [my-map,line]
  "my-map is a map from column number -> list of nodes
  Add nodes from line to the map"
  (let [beginning-of-line (start-index line)
        ]
    (reduce (fn [accumulator node]
              (let [column (- (start-index node) beginning-of-line) ]
                (assoc accumulator
                       column 
                       (conj (get accumulator column) node)
                       )))
            my-map (filter #(not= nil (start-index %)) (all-nodes line))
            )
    )) 


(defn section-column-map [section-content]
  "Given a section returns a map ,  column-number ==> list of nodes at that column"
  (reduce line-column-map {}  section-content)
  )

(defn- update-sargam-ornament-pitch-node [content nodes]
  " TODO: Apply the upper and lower dots found in the upper lines.
  Dots will be in same column. If positioned afterward, becomes lower dot.
  If positioned before, becomes upper dot
  If there is a sargam pitch in this column then things get more complicated. 
  For now, don't apply dots that are 'under' the ornament pitch if there is a
  sargam pitch there.
  "
  (let [
        has-sargam-pitch (first  (filter #(and (vector? %) (=(first %) :SARGAM_PITCH)) nodes))
        upper-dots (if has-sargam-pitch
                     ()
                     (filter #(and (vector? %) (=(first %) :UPPER_OCTAVE_DOT)) nodes)
                     )

        ]
    (into [] (concat [:SARGAM_ORNAMENT_PITCH] content))
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

(defn transform-beat [& beat-content]
  ;;(my-raise "tranform-beat")
  (if false
    (do
      (println "entering transform-beat beat content")
      (pprint beat-content)
      (println "entering transform-beat")
      ))
  (let [divisions (count (filter #(unit-of-rhythm %) (flatten beat-content)))
        tree [:BEAT beat-content]
        transform-sargam-pitch-with-dashes
        (fn [& content]
          (into [] (concat [:PITCH_WITH_DASHES] content "johnwashere"))
          )
        tree2 (insta/transform {:PITCH_WITH_DASHES transform-sargam-pitch-with-dashes } tree)
        ]

    (into [] (concat [:BEAT] 
                     (rest tree2)
                     {:divisions divisions}
                     ))  
    ))

(defn- update-sargam-pitch-node [content nodes]
  " Use the list of nodes to update a :SARGAM_PITCH node.
  Returns the new node. This is using hiccup style.
  This is for the instaparse/transform function"
  (let [;; TODO: DRY
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
    (pprint "nodes:*****************")
    (pprint nodes)
    ;; Note that we support multiple syllables, ornaments, and chords per note. But only return the last (for now)
    [ :SARGAM_PITCH
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
     ]
    ))

(defn update-sargam-line-node[content nodes]
  "add syllables attribute to sargam line TODO"
  ;  (concat [:hi] '(1 2))
  ;; (into []  (concat [:hi] '(1 2)))
  ;;[:hi 1 2]
  ;  
  (into [] (concat [:SARGAM_LINE] content
                   { :syllables 
                    (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
                    }
                   )))


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



(defn main[]
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
  "Assign attributes to the main line(sargam_line) from the lower and upper lines using 
  column numbers. Returns a sargam-line
  modified section"
  (let [
        sargam-section-content (rest sargam-section)
        sargam-line (first (filter #(= :SARGAM_LINE (first %)) sargam-section-content))
        column-map (section-column-map sargam-section-content)
        line-starts (map start-index sargam-section-content)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )

        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node)))
                          )
        transform-sargam-pitch (fn[ content & zrest] 
                                 (let [
                                       column (column-for-node content)
                                       nodes (get column-map column) 
                                       ]
                                   (update-sargam-pitch-node content nodes)
                                   ))
        transform-sargam-ornament-pitch (fn[content] 
                                          (let [
                                                column (column-for-node content)
                                                nodes (get column-map column) 
                                                ]
                                            (update-sargam-ornament-pitch-node content nodes)
                                            ))
        transform-sargam-line (fn[& content] 
                                (let [
                                      nodes (reduce (fn[accum line]
                                                      (concat accum 
                                                              (filter #(vector? %) (all-nodes line))))
                                                    () 
                                                    sargam-section-content) 
                                      ]
                                  ;; (my-raise nodes)
                                  (update-sargam-line-node content nodes)
                                  ))
        sargam-section-content2 (insta/transform {:SARGAM_PITCH transform-sargam-pitch 
                                 :SARGAM_ORNAMENT_PITCH transform-sargam-ornament-pitch
                                 :SARGAM_LINE transform-sargam-line
                                                  :BEAT transform-beat
                                 } sargam-section-content)
        modified-sargam-line (first (filter #(= :SARGAM_LINE (first %)) sargam-section-content2))
        ]
    (pprint "column-map")
    (pprint column-map)
    (pprint "sargam-section:")
   (pprint sargam-section)
    (pprint "----------")
    ;; (println "sargam-section-content")
    ;;(pprint sargam-section-content)    
    ;; (println "sargam-section-content")
    ;(println "sargam-section-content2")
    ;(pprint sargam-section-content2)
    ;(println "zzzsargam-section-content2------------------")
    ;;[:sargam-section-content (into []
    modified-sargam-line
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

(pprint (my-transforms (get-parser2 "S\nhello")))
;(pprint (my-transforms (get-parser2 "title:hi\n\nSRG-\nhello\n\nmPD")))
;; (pprint (my-transforms (get-parser2 "S\n\nR")))
;;(pprint (get-parser2 "S\n\nR"))
;;(pprint  (postwalk items-fix (get-parser2 "title:hi\n\nSRG-")))


;;(pprint (parse2 "title:hi\n\nSR\nS"))
;;
(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )

(defn add-source[parse-tree txt]
  (conj parse-tree [:source txt])
  )

(defn run-through-parser[txt]
  (get-parser2 txt)
  )
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
;;  generate json
