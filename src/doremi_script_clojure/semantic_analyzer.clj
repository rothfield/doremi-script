(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [clojure.pprint :refer [pprint]] 
            [clojure.walk :refer [postwalk]]
            [instaparse.core :as insta]
            ))
(comment
  ;; to use in repl:
  (use 'doremi_script_clojure.semantic-analyzer :reload)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (ns doremi_script_clojure.semantic-analyzer)
  (use 'clojure.stacktrace)
  (print-stack-trace *e)
  (pst)
  )


(comment
  "postwalk will process the leaves first "
  "In this case the function I applied conj's a counter to the node
  if the node is a vector"
  [:COMPOSITION
   [:SARGAM_SECTION
    [:SARGAM_LINE [:MEASURE [:BEAT [:SARGAM_PITCH [:S "S" 1] 2] 3] 4] 5]
    6]
   7]
  )
(comment
  "prewalk start at root and works its way down"
  [:COMPOSITION
   [:SARGAM_SECTION
    [:SARGAM_LINE [:MEASURE [:BEAT [:SARGAM_PITCH [:S "S" 7] 6] 5] 4] 3]
    2]
   [:SARGAM_SECTION
    [:SARGAM_LINE
     [:MEASURE [:BEAT [:SARGAM_PITCH [:R "R" 13] 12] 11] 10]
     9]
    8]
   1]
  )



(def debug false)

(def sargam-line-tag 
  "set of tags for lines in sargam section"
  #{ :SARGAM_LINE  :LYRICS_LINE :UPPER_OCTAVE_LINE  :LOWER_OCTAVE_LINE}
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



(defn assign-attributes-to-section [lines]
  "Assign attributes to the main line(sargam_line) from the lower and upper lines using 
  column numbers. Returns
  modified section"
  (let [
        sargam-line (first (filter #(= :SARGAM_LINE (first %)) lines))
        column-map (section-column-map lines)
        line-starts (map start-index lines)
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
                                                    lines) 
                                      ]
                                  ;; (my-raise nodes)
                                  (update-sargam-line-node content nodes)
                                  ))
        lines2 (insta/transform {:SARGAM_PITCH transform-sargam-pitch 
                                 :SARGAM_ORNAMENT_PITCH transform-sargam-ornament-pitch
                                 :SARGAM_LINE transform-sargam-line
                                 } lines)
        ]
    ;; (println "lines")
    ;;(pprint lines)    
    ;; (println "lines")
    ;;(println "lines2")
    ;; (pprint lines2)
    (insta/transform {
                      :BEAT transform-beat
                      } lines2)
    ))


(defn transform-sargam-section[& content]
  (into [] (concat [:SARGAM_SECTION]
                   (collapse-sargam-section content))))

(defn reduce-attribute-line[accum attribute_line]
  "accum is a hash map. attribute_line looks like
  [:ATTRIBUTE_LINE [:KEY 'Title'] [:VALUE 'Help']]
  Add a key/value pair to the hashmap and return the modified
  hashmap.
  "
  (assert (= :ATTRIBUTE_LINE (first attribute_line)))
  (let [
        my-hash (into {} (rest attribute_line))
        my-key (keyword (:KEY my-hash))
        value (:VALUE my-hash)
        ]
    (assoc accum  my-key value)))

(defn transform-attribute-section[& attribute-lines]
  ;;attribute-lines looks like ( [:KEY "Title"] [:VALUE "Help"]
  ;;;  [:ATTRIBUTE_SECTION [:ATTRIBUTE_LINE [:KEY "Title"] [:VALUE "Help"]]]
  [ :ATTRIBUTE_SECTION
   (reduce reduce-attribute-line {} attribute-lines)
   ]
  )


(defn assign-attributes[composition]
  "Takes parse tree and processes it. Assigns object based on column position
  and other factors. Returns modified tree"
  (assert (= :COMPOSITION (first composition)))
  (insta/transform {:SARGAM_SECTION transform-sargam-section
                    :ATTRIBUTE_SECTION transform-attribute-section
                    } composition)
  )



(defn extract_sargam_line_from_sargam_section[sargam-section]
  ;; (println "sargam-section:::")
  ;;(pprint sargam-section)
  ;;(my-raise "z")
  (assert (= (:SARGAM_SECTION (first sargam-section))))
  (first (filter #(= :SARGAM_LINE (first %)) (rest sargam-section)))
  )

(defn parse2[txt]
  "parse and run through semantic analyzer. Txt is doremi-script"
  (let [
        parse-tree (get-parser2 txt) ;; original parse-tree
        ;; parse-tree2
        ;;   (insta/transform {:SARGAM_PITCH transform-sargam-pitch1} parse-tree)
        ;; z (my-raise parse-tree2)
        x1 (assign-attributes parse-tree)
        sargam-sections (filter #(= :SARGAM_SECTION (first %))  (rest x1))
        attribute-sections (filter #(= :ATTRIBUTE_SECTION (first %))  (rest x1))
        lyrics-sections  (filter #(= :LYRICS_SECTION (first %))  (rest x1))
        lines  (map  extract_sargam_line_from_sargam_section sargam-sections)
        ]
    x1
    { :attributes attribute-sections
     ;;  :lines1 sargam-sections
     :lines lines
     :lyrics lyrics-sections
     :source txt
     }
    ))

;(pprint (parse2 "n\nS-RG"))
;;(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nThese are all the lyrics\nSecond lyric line\n\nRG.\nS | R | G\nhe-llo\n\n |m P D - |\n\nMore:attributes"))
;; (pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (get-parser2 (slurp-fixture "yesterday.txt")))


;;(get-parser2 "S--R--" :start :BEAT)


;;(pprint (get-parser2 "S-RG"))

(def z (get-parser2 "S\n\nR"))

(def counter (atom 0))
(defn walk-test-helper[x]
  (cond (vector? x)
        (do
          (swap! counter inc)
          (conj x @counter)
          )
        true
        x        
        ))

(defn postwalk-test[tree]
  (reset! counter 0)
  (postwalk walk-test-helper tree)
  ;; (prewalk walk-test-helper tree)
  )

(if false
  (do
    (println "postwalk-----")
    (pprint (postwalk-test z))
    (println "postwalk-----\n\n")
    ))

(defn zadd-source [node txt]
  "txt is the original parse source"
  (cond (not (vector? node))
        node
        true
        (into node [:source (apply subs txt 
                                   (instaparse.core/span node))]
              )    
        ))

(defn main[]
  (let [txt  "SRG\n\nmPD"
        line-starts 
        (keep-indexed (fn [index value](if  (= value \newline) index)) txt)
        add-source (fn [node]
                     (cond (not (vector? node))
                           node
                           true
                           (into node [:source (apply subs txt 
                                                      (instaparse.core/span node))]
                                 )    
                           ))
        ]
    (pprint (postwalk add-source (get-parser2 txt)))
    (pprint line-starts)
    ))
(main)
