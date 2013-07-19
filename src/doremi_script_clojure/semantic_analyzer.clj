(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [doremi_script_clojure.semantic-analyzer :refer :all ]
            [clojure.walk :refer :all ]
            [clojure.pprint :refer :all ]
            [clojure.stacktrace]
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
  ;(println "x is " x)
  (if (not (map? x))
    false
    (not= nil (sargam-line-tag (:tag x)))
    ))

(defn position-map-for-sargam-section
  "I don't do a whole lot."
  [x]
  (print (meta (first x)))
  (let [ lines (filter is-line? x)
        ]
    (print lines)
    )
  )

;;; (reduce f val coll)
;; (assoc map key value)
;; (reduce fct init-val sequence)

(defn my-get-index[x]
  (:instaparse.gll/start-index (meta x)
                               ))
(defn my-throw[x]
  (throw (Exception. (str x))))

(defn all-nodes[tree]
  (tree-seq map? :content tree)
  )

(defn line-positions [line]
  "Add line position to every item"
  ;;(println "entering line-positions")
  (let [
        line-start-index (my-get-index line)	
        ]

    ))

(defn line-positions-old [line]
  "Add line position to every item"
  ;;(println "entering line-positions")
  (let [
        line-start-index (my-get-index line)	
        ;;; inner function here!
        add-to-map (fn [my-map node] 
                     (let [index  (- (my-get-index node)  line-start-index )
                           existing-entry (my-map index)
                           ]
                       (assoc my-map
                              index
                              (conj existing-entry node))))
        ;;;; end inner function	
        all-nodes (tree-seq map? :content line)

        ]
    ;;(my-throw line-start-index)        
    (reduce add-to-map (sorted-map) (filter map? all-nodes))
    ))

; (conj coll x)
; (conj coll x & xs)
; conj[oin]. Returns a new collection with the xs
; 'added'. (conj nil item) returns (item). The 'addition' may
; happen at different 'places' depending on the concrete type.




;;; (reduce f val coll)
; If val is supplied, returns the
; result of applying f to val and the first item in coll, then
; applying f to that result and the 2nd item, etc. 
;; Create a word frequency map out of a large string s.

;; `s` is a long string containing a lot of words :)
; (reduce #(assoc %1 %2 (inc (%1 %2 0)))
;         {}
;         (re-seq #"\w+" s))

; (This can also be done using the `frequencies` function.)


; tree-seq clojure.core
; (tree-seq branch? children root)
; Returns a lazy sequence of the nodes in a tree, via a depth-first walk.
; branch? must be a fn of one arg that returns true if passed a node
; that can have children (but may not). children must be a fn of one
; arg that returns a sequence of the children. Will only be called on
; nodes for which branch? returns true. Root is the root node of the
; tree.




; assoc clojure.core
; (assoc map key val)
; (assoc map key val & kvs)
; assoc[iate]. When applied to a map, returns a new map of the
; same (hashed/sorted) type, that contains the mapping of key(s) to
; val(s). When applied to a vector, returns a new vector that
; contains val at index. Note - index must be <= (count vector).



(def z (get-parser2 "SRG\nhe-llo" :start :SARGAM_SECTION))

(defn positionable-tag? "test whether x should have position added to it" [x]
  (#{:SYLLABLE :SARGAM_PITCH} x)
  )

(defn assign-positions[tree]
  "Return the tree with positions added to syllables and pitches"
  (println "assign-positions-to-tree-- tree is " tree "*****")
  (let [
        debug false
        ]
    (letfn [ (fn1[x] (list 
                       (hash-map 
                         :tag :SARGAM_PITCH
                         :position (my-get-index x)
                         :content x
                         )
                       ))
            (fn2 [x]
              (if debug
                (do
                  (println "***fn2, x is ")
                  (pprint x)
                  ))
              (list 
                (hash-map 
                  :tag :SYLLABLE
                  :position (my-get-index x)
                  :content x
                  ))
              )
            ]

      (insta/transform {:SARGAM_PITCH fn1
                        :SYLLABLE fn2 } tree)
      )))




(defn assign-syllables[tree]
  tree
  )



(defn assign-syllables [section]
  "Assign syllables to the appropriate note base on the column.
  Return the modified tree"
  (let [
        debug false 
        sargam-line (filter (fn[x] 
                              (and (map? x)
                                   (= (:tag x) :SARGAM_LINE)))
                            (:content section))
        lyric-lines (filter (fn[x] 
                              (and (map? x)
                                   (= (:tag x) :LYRICS_LINE)))
                            (:content section))
        fn2   (fn [section lyric-line]
                (let [
                      ]
                  section
                  ))
        fn1  (fn [section lyrics-line]
               (let [debug true]
                 (if debug
                   (println "fn1-section,lyrics-line tags" (:tag section) (:tag lyrics-line))
                   )
                 (reduce fn2 section lyrics-line) 

                 ))
        ]
    (if debug (pprint lyric-lines))
    (reduce fn1 section lyric-lines) 
    ))



(defn line-column-map [my-map,line]

  "given a line, return a map of column number -> list of nodes
  my-map is the existing map"
  (let [beginning-of-line (my-get-index line)
        fct   (fn [accumulator node]
                (let [node-position (my-get-index node)
                      ]
                  (if (nil? node-position)
                    accumulator
                    ;; otherwise
                    (let [
                          key (- (my-get-index node) beginning-of-line)
                          ]
                      (assoc accumulator
                             key 
                             (conj (get accumulator key) node )
                             )
                      ))))
        ]
    ;;(pprint line)
    ;; (println beginning-of-line)
    (reduce    fct   my-map  (all-nodes line))
    )) 


(defn section-column-map [section]
  "Given a section returns a map ,  column-number ==> list of nodes at that column"
  (reduce line-column-map (hash-map)  (filter is-line? (:content section)))
  )


(defn assign-attributes [section]
  "Assign attributes by using column numbers. Returns
  modified section"
  (let [
        debug false
        lines  (filter is-line?  (:content section))
        sargam-line (first(filter (fn[x] (= :SARGAM_LINE (:tag x))) lines))
        beginning-of-sargam-line (my-get-index sargam-line)
        column-map (section-column-map section)
        get-column-of-sargam-node 
        (fn[node]
          (- (my-get-index node) beginning-of-sargam-line)
          )
        fn1 (fn[content] 
              (let [
                    column (get-column-of-sargam-node content)
                    nodes (get column-map column) 
                    syl (last (filter #(and (map? %) (= (:tag %) :SYLLABLE)) nodes))
                    upper-dots (filter #(and (map? %) (=(:tag %) :UPPER_OCTAVE_DOT)) nodes)
                    lower-dots (filter #(and (map? %) (=(:tag %) :LOWER_OCTAVE_DOT)) nodes)
                    octave  (+ (count upper-dots) 
                               (- (count lower-dots))
                               )
                    ]

                (println "*** in fn1;syl" syl ";" "****")
                (println "*** in fn1;(:content syl)" (:content syl) ";" "****")
                (if debug
                  (do
                    (println "*** in fn1;key,count(nodes)" column ";" (count nodes) "****")
                    ))
                { :tag :SARGAM_PITCH
                 :content (assoc content 
                                 :syllable 
                                 (:content syl))
                 :octave
                 octave
                 }
                ))
        ]
    (assert beginning-of-sargam-line)
    (assert sargam-line)
    ;; (println "***" (my-get-index (:content sargam-line)))
    ;;(println "***" (meta sargam-line))
    (if debug
      (do
        (println "****beginning-of-sargam-line is" beginning-of-sargam-line "*****")
        (println "***" "sargam-line" sargam-line "***")
        (println "************assign-attributes****")
        (println "lines" lines)
        (println "lines" lines)
        (println "sargam-line")
        (pprint sargam-line)
        ))
    (insta/transform {:SARGAM_PITCH fn1 } section)
    ))

(def x (get-parser2 
         "*\nDm7\nS\n*\n*\nhi-"
         :start :SARGAM_SECTION))

;;(pprint (section-column-map x))
(pprint (assign-attributes x))
;;(pprint (line-column-map (hash-map) (first (:content x))))
;(println "running assign-syllables")
;(pprint (assign-syllables x))
