(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [doremi_script_clojure.semantic-analyzer :refer :all ]
            [clojure.pprint :refer :all ]
            [clojure.string :refer [lower-case]]
            ;;;  [clojure.string/lower-case :refer :only [lower-case]] ; [lower-case ]]
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


(defn- is-line? [x]
  (and (vector? x) (sargam-line-tag (first x))))

(defn start-index[x]
  "Looks up starting index of the node from the node's 
  metadata. Note that instaparse add the metadata when it 
  parses."
  (:instaparse.gll/start-index (meta x)
                               ))
(defn my-throw[x]
  (throw (Exception. (str x))))

(defn all-nodes[tree]
  (tree-seq  #(or (vector? %) (list? %)) rest tree)
  )


(defn line-column-map [my-map,line]
  "given a line, return a map of column number -> list of nodes
  my-map is the existing map"
  (if debug (do
              (println "@@@@ line-column-map. line is: ")
              (pprint line)
              (println "@@@@")
              ))
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
  Returns the new node. This is using enlive style.
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
      :chords
      chords
      :ornaments
      ornaments
      :tala
      tala
      :mordent
      (second mordent)
      }
     ]
    ))


(defn assign-attributes-to-section [section-content]
  "Assign attributes from the lower and upper lines using 
  column numbers. Returns
  modified section"
  (if debug
    (do
      (println "ENTERING assign-attributes-to-section")
      (println "section-content<<<<<")
      (pprint section-content)
      (println "section<<<<<")
      ))
  (let [
        sargam-line (first (filter #(= :SARGAM_LINE (first %)) section-content))
        column-map (section-column-map section-content)
        lines section-content
        line-starts (map start-index lines)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node)))
                          )
        transform-sargam-pitch (fn[content] 
                                 (if debug (println "transform-sargam-pitch"))
                                 (let [
                                       column (column-for-node content)
                                       nodes (get column-map column) 
                                       ]
                                   (if debug (do
                                               (println "column" column)
                                               (pprint nodes)
                                               (println "ending transform-sargam-pitch")
                                               ))
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
    (assert line-starts)
    (assert lines)
    (assert (>= (count lines) 0))
    (assert sargam-line)
    (if debug (do
                (println "***")
                (pprint sargam-line)
                (println "***")
                ))
    (insta/transform {:SARGAM_PITCH transform-sargam-pitch 
                      :SARGAM_ORNAMENT_PITCH transform-sargam-ornament-pitch
                      } section-content)
    ))

(defn tag-is[obj tag]
  (and (vector? obj)
       (= (first obj) tag)))

(defn find-first-tag[obj tag]
  (first (filter #(tag-is % tag) (all-nodes obj))))

(defn assign-attributes-to-composition[composition]
  (assert (= :COMPOSITION (first composition)))
  (let [
        transform-sargam-section 
        (fn [& content]
          (if debug (do
                      (println "transform-sargam-section (fn [& content]
                               >>>>>")
                      (pprint content)
                      (println ">>>>>")
                      ))
          [:SARGAM_SECTION
           (assign-attributes-to-section  content)
           ]
          )
        transform-attribute-section
        (fn [& content]
          (let [attribute-lines content] 
            (println "attribute-lines are " attribute-lines)
            [ :ATTRIBUTE_SECTION
             :attributes
             (reduce (fn[accum obj]
                       (let [
           ;; obj=>  [:ATTRIBUTE_LINE [:KEY "Title"] [:VALUE "test semantic analyzer"]]
                             x (rest obj)
                             my-key (keyword (lower-case (second (first x))))
                             value (second (second x))
                             ]
                         (assoc accum  my-key value))) {} attribute-lines)
             ]))
        ]
    (insta/transform {:SARGAM_SECTION transform-sargam-section
                      :ATTRIBUTE_SECTION transform-attribute-section
                      } composition)
    ))

(defn remove-last-char[txt]
  (.substring txt 0 (- (count txt) 2)))

(def x1
  ":\n~\n+\nSNRSNS\n*\nDm7\nS\n*\n*\n:\n:\nhi-")

(def x2
  "remove-last-char is hack because parser doesn't like trailing newline for sargam section"
  (remove-last-char (slurp-fixture "semantic_analyzer_test.txt")))


(def x 
  (get-parser2 x2
               :start :COMPOSITION))
(def x3
  (get-parser2 "Title: test1\nAuthor:  John Rothfield\n\n Yes-ter-day all my\n\n*\nS\n\nr\n*"
               :start :COMPOSITION))

(def x4
  (get-parser2 "Title: test1\n\nS"
               :start :COMPOSITION))

(def x5
  (get-parser2 "S | r | g\nhe-llo"
               :start :COMPOSITION))
(def x7
  (get-parser2 "S\nhi"
               :start :COMPOSITION))

(def x6
  '[:SARGAM_SECTION
    ([:SARGAM_LINE
      [:MEASURE [:BEAT [:SARGAM_PITCH [:S "S"]]] " "]
      :content
      [:BARLINE [:SINGLE_BARLINE "|"]]
      [:MEASURE [:BEAT [:SARGAM_PITCH [:r "r"]]] " "]
      [:BARLINE [:SINGLE_BARLINE "|"]]
      [:MEASURE [:BEAT [:SARGAM_PITCH [:g "g"]]]]]
     [:LYRICS_LINE [:SYLLABLE "he-"] [:SYLLABLE "llo"]])]
  )  

(def x8 '[:SARGAM_LINE [:MEASURE [:BEAT [:SARGAM_PITCH [:S "S"]]]]])
;; (pprint (assign-attributes-to-section x))
;;(println (count (assign-attributes-to-composition x3)))
;;(pprint x3)
;;(println "\n\n\n")
;; (pprint (find-first-tag x3 :KEY))

;; (println (lower-case "XXX"))
;; (pprint (assign-attributes-to-composition x7))
(pprint (assign-attributes-to-composition x))
