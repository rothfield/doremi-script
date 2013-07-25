(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [clojure.pprint :refer [pprint]] 
            ;; [clojure.string :refer [lower-case]]
            [instaparse.core :as insta]
            ))

(comment
  ;; to use in repl:
  (use 'doremi_script_clojure.semantic-analyzer :reload)
  (ns doremi_script_clojure.semantic-analyzer)
  (use 'clojure.stacktrace)
  (print-stack-trace *e)
  )

(def debug false)

(def sargam-line-tag 
  "set of tags for lines in sargam section"
  #{ :SARGAM_LINE  :LYRICS_LINE :UPPER_OCTAVE_LINE  :LOWER_OCTAVE_LINE}
  )

(defn start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. Note that instaparse adds the metadata when it 
  parses."
  (let [x 
        (instaparse.core/span z)    

        ]
    (first x)))

(defn my-throw[x]
  (throw (Exception. (str x))))

(defn all-nodes[tree]
  (tree-seq  #(or (vector? %) (list? %)) rest tree)
  )


(defn line-column-map [my-map,line]
  "given a line, return a map of column number -> list of nodes
  my-map is the existing map"
  (let [beginning-of-line (start-index line)
        fct   (fn [accumulator node]
                (let [node-position (start-index node)
                      ]
                  (if (nil? node-position)
                    accumulator
                    ;; otherwise
                    (let [
                          key (- (start-index node) beginning-of-line)
                          ]
                      (assoc accumulator
                             key 
                             (conj (get accumulator key) node )
                             )
                      ))))
        ]
    (reduce fct my-map (all-nodes line))
    )) 


(defn section-column-map [section-content]
  "Given a section returns a map ,  column-number ==> list of nodes at that column"
  (reduce line-column-map (hash-map)  section-content)
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
    ;;(println "upper dots**" upper-dots)
    ;;(println "has-sargam-pitch***" has-sargam-pitch)
    [:SARGAM_ORNAMENT_PITCH
     content
     ]
    ))



(defn- update-sargam-pitch-node [content nodes]
  " Use the list of nodes to update a :SARGAM_PITCH node.
  Returns the new node. This is using hiccup style.
  This is for the instaparse/transform function"
  ;;(println "$$$$$ nodes!!")
  ;;(pprint nodes)
  ;;(println "$$$$$")
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
      :pitch
      (first content)
      :source
      (second content)
      :syllable
      (second (last syls))
      :octave
      octave
      :chord
      (last chords)
      ;; :all-chords
      ;; chords
      :ornament   ;; just the pitches
      (into (vector) (rest (last ornaments)))
      ;; :all-ornaments
      ;; (map rest ornaments)
      :tala
      tala
      :mordent
      (second mordent)
      }
     ]
    ))


(defn assign-attributes-to-section [lines]
  "Assign attributes from the lower and upper lines using 
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
        ]
    (insta/transform {:SARGAM_PITCH transform-sargam-pitch 
                      :SARGAM_ORNAMENT_PITCH transform-sargam-ornament-pitch
                      } lines)
    ))


(defn transform-sargam-section[& content]
  [:SARGAM_SECTION (assign-attributes-to-section content)]
  )

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
  (assert (= :COMPOSITION (first composition)))
  (insta/transform {:SARGAM_SECTION transform-sargam-section
                    :ATTRIBUTE_SECTION transform-attribute-section
                    } composition)
  )


(defn parse2[txt]
  "parse and run through semantic analyzer. Txt is doremi-script"
  (assign-attributes (get-parser2 txt))
  )

;;(pprint (parse2 " .\nSr"))
(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nRG.\nS\nhe-llo"))
;;(pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (get-parser2 (slurp-fixture "yesterday.txt")))

