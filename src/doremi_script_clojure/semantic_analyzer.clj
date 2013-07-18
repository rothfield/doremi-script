(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	[doremi_script_clojure.test-helper :refer :all ]
            [doremi_script_clojure.semantic-analyzer :refer :all ]
            [clojure.walk :refer :all ]
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

(defn- is-line? [x]
  ;(println "x is " x)
  (let [tag (:tag x)]
    (if (not tag)
      false
      (or (= :SARGAM_LINE tag)
          (= :LYRICS_LINE tag)
          (= :UPPER_OCTAVE_LINE tag)
          (= :LOWER_OCTAVE_LINE tag)
          )
      )
    )
  )



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

(defn add-syls [sargam-line line]
  ;; (reduce fct init-val sequence)
  ;;(reduce add-syls sargam-line lines) 
  )

(defn apply-syls [sargam-line lyric-line]
  ;; add each syllable in lyric line to the corresponding sargam-pitch
  (let [lyric-positions (:positions lyric-line)
        sargam-positions (:positions sargam-line) 

        ]
    (assert sargam-positions)
    (assert lyric-positions)
    (println "apply-syls, lyric-line is") (pprint lyric-line)
    ; (insta/transform {:switch (fn [x y] [:switch y x])}
    ; my-tree)
    ;  (pprint (instaparse.core/transform {:SARGAM_PITCH (fn [&rest] {:tag :SARGAM_PITCH :content &rest})}      z))
    sargam-line
    ))

(defn test-line-positions[]
  (let [
        local-fn  (fn [x]
                    ;; (println "local-fn")
                    (assoc x :positions (line-positions x)))
        local-fn2 (fn [x]
                    (let [ z (:tag x)]
                      ; (println "local-fn2, x is " x)
                      ; (println "local-fn2, z is " z)
                      (= z :LYRICS_LINE)))
        section (get-parser2 "| S-- - R |\nhe-llo" :start :SARGAM_SECTION)
        lines (filter is-line? (all-nodes section))
        lines2  (map local-fn lines)
        ;; only one sargam-line per section
        sargam-line (first (filter #(= (:SARGAM-LINE (:tag %))) lines2))
        lyrics-lines (filter local-fn2 lines2)
        sargam-line2 (reduce apply-syls sargam-line lyrics-lines)
        ]
    sargam-line2
    ;(pprint lines2)
    ;(pprint (map :tag lines2))
    ; (println "sargam-line is") (pprint sargam-line)
    ;(println "lyrics-lines is") (pprint lyrics-lines)

    ;(println "section is ")
    ;(pprint section)
    ;;(println "lines is ")
    ;;(pprint lines)
    ;;(println "1st line_positions is")
    ;;(pprint (line-positions  (first lines)))
    ;;(println "2nd line_positions is")
    ;;(pprint (second lines))
    ;;(pprint (line-positions  (second lines)))
    ))

(defn outer [x]
  ;(println "in outer, x is " x)
  x
  )

(defn positionable-tag? "test whether x should have position added to it" [x]
  (#{:SYLLABLE :SARGAM-PITCH} x)
  )

;;; Have to declare mutually recursive functions
(declare inner outer add_positions)
(defn inner [x]
  ;(println "in inner, x is " x)
  ;(println "in inner, (class x) is " (class x))
  ;(println "in inner, (seq? x) is " (seq? x))
  ;(println "in inner, (map? x) is " (map? x))
  ;(println "in inner, (vector? x) is " (vector? x))
  (cond
    (and (map? x) (positionable-tag? (:tag x)))
      (hash-map :tag (:tag x)
                :content  (add_positions (:content x))
                :position (my-get-index x) 
                )

    (map? x)
    (do
      ;(println "map? case")
      (assert (:tag x))
      (assert (:content x))
      (hash-map :tag (:tag x)
                :content  (add_positions (:content x))
                )
      )
    (seq? x)
    (do
      ;(println "seq? case")
      (conj (add_positions (rest x)) (add_positions (first x)))
      )
    (vector? x)
    (do
      ;(println "vector? case")
      ;(println "x is")
      ;(pprint x)
      (vec (map add_positions x))
      )
    true
    (do
      ;(println "fall through case")
      x) 
    ))

(defn add_positions[x]
    (walk inner outer x)
)


(def x (get-parser2 "S\nhi" :start :SARGAM_SECTION))

(defn test-walk[tree]
  "Walk an enlive tree"
  (let [result (add_positions tree)]
    (println "Input is")
    (pprint tree)
    (println "Result is")
    (pprint result)
    (assert (not= result tree))
    nil
    ))

(test-walk x)



