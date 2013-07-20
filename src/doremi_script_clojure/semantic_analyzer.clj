(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [doremi_script_clojure.semantic-analyzer :refer :all ]
            [clojure.pprint :refer :all ]
            [instaparse.core :as insta]
            ))



(comment
  ;; to use in repl:
  (use 'doremi_script_clojure.semantic-analyzer :reload)
  (ns doremi_script_clojure.semantic-analyzer)
  (use 'clojure.stacktrace)
  (print-stack-trace *e)
  )


(def sargam-line-tag 
  "set of tags for lines in sargam section"
  #{ :SARGAM_LINE  :LYRICS_LINE :UPPER_OCTAVE_LINE  :LOWER_OCTAVE_LINE}
  )


(defn- is-line? [x]
  (if (not (map? x))
    false
    (not= nil (sargam-line-tag (:tag x)))
    ))

(defn start-index[x]
  "Looks up starting index of the node from the node's 
  metadata. Note that instaparse add the metadata when it 
  parses."
  (:instaparse.gll/start-index (meta x)
                               ))
(defn my-throw[x]
  (throw (Exception. (str x))))

(defn all-nodes[tree]
  (tree-seq map? :content tree)
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


(defn section-column-map [section]
  "Given a section returns a map ,  column-number ==> list of nodes at that column"
  (reduce line-column-map (hash-map)  (filter is-line? (:content section)))
  )

(defn set-node-attributes [content nodes]
  (let [            ;; TODO: DRY
        mordent (last (filter #(and (map? %) (= (:tag %) :MORDENT)) nodes))
        syls (filter #(and (map? %) (= (:tag %) :SYLLABLE)) nodes)
        upper-upper-dots (filter #(and (map? %) (=(:tag %) :UPPER_UPPER_)) nodes)
        upper-dots (filter #(and (map? %) (=(:tag %) :UPPER_OCTAVE_DOT)) nodes)
        upper-upper-dots (filter #(and (map? %) (=(:tag %) :UPPER_UPPER_OCTAVE_SYMBOL)) nodes)
        lower-dots (filter #(and (map? %) (=(:tag %) :LOWER_OCTAVE_DOT)) nodes)
        lower-lower-dots (filter #(and (map? %) (=(:tag %) :LOWER_LOWER_OCTAVE_SYMBOL)) nodes)
        chords (filter #(and (map? %) (=(:tag %) :CHORD)) nodes)
        tala (:content (last (filter #(and (map? %) (=(:tag %) :TALA)) nodes)))
        ornaments (filter #(and (map? %) (=(:tag %) :SARGAM_ORNAMENT)) nodes)

        octave  (+ (count upper-dots) 
                   (- (count lower-dots))
                   (* 2 (count upper-upper-dots))
                   (* -2 (count lower-lower-dots))
                   )
        ]
    { :tag :SARGAM_PITCH
     :content 
     (assoc content 
            :content
            (apply str (:content content))
            :syllable
            (first (:content (last syls)))
            :octave
            octave
            :chord
            (first (:content (last chords)))
            :chords
            (map first (map :content chords))
            :ornaments
            ornaments
            :tala
            tala
            :mordent
            (first (:content mordent))

            )
     }
    ))


(defn assign-attributes [section]
  "Assign attributes from the lower and upper lines using 
  column numbers. Returns
  modified sargam line"
  (let [
        debug false
        lines  (filter is-line?  (:content section))
        sargam-line (first(filter (fn[x] (= :SARGAM_LINE (:tag x))) lines))
        beginning-of-sargam-line (start-index sargam-line)
        column-map (section-column-map section)
        get-column-of-sargam-node (fn[node]
                                    (- (start-index node) beginning-of-sargam-line)
                                    )
        transform-sargam-pitch (fn[content] 
              (let [
                    column (- (start-index content) beginning-of-sargam-line)
                    nodes (get column-map column) 
                  ]
                  (set-node-attributes content nodes)
                ))
        ]
    (assert beginning-of-sargam-line)
    (assert sargam-line)
    (insta/transform {:SARGAM_PITCH transform-sargam-pitch } sargam-line)
    ))

(pprint (assign-attributes x))
