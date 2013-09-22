(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk-demo postwalk postwalk-replace keywordize-keys]]
    [clojure.string :refer [lower-case]]
    [instaparse.core :as insta]
    [clojure.data.json :as json]
    ))
(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.semantic_analyzer :reload) (ns doremi_script_clojure.semantic_analyzer) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )


(defn p[] (println "************************"))

(def doremi-script-parser  
  (insta/parser
    (slurp (io/resource "doremiscript.ebnf"))))

(defn start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. instaparse adds the metadata in the parsing process"
  "Returns the character position in the source code where the object starts"
  (first (instaparse.core/span z))    
  )


(def unit-of-rhythm
  #{ :pitch  :dash}
  )


(def sargam-pitch-to-source
  (array-map
    :Sb "Sb" 
    :Ssharp "S#"
    :Rsharp "R#" 
    :Gsharp "G#" 
    :Psharp "P#" 
    :Pb "Pb" 
    :Dsharp "D#" 
    :Nsharp "N#" 
    :Pb "Pb" 
    :S "S" 
    :r "r" 
    :R "R" 
    :g "g" 
    :G "G" 
    :m "m" 
    :M "M" 
    :P "P" 
    :d "d" 
    :D "D" 
    :n "n" 
    :N "N" 
    ))

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




(defn my-seq[x]
  "seq through the data structure, which is like"
  " {:items [ {:items [1 2 3]} 2 3]}"
  "Don't include items like {:items [1 2 3]} "
  "just [1 2 3]"
  (filter #(not (:items %))
          (tree-seq
            (fn branch?[x] (or (vector? x) (map? x))) 
            (fn children[y] (cond 
                              (and (map? y) (:items y)) 
                              (:items y)
                              (vector? y)
                              (rest y))) 
            x)))

(defn line-column-map [my-map line]
  "my-map is a map from column number -> list of nodes
  Add nodes from line to the map"
  (reduce (fn [accumulator node]
            (let [column (- (:_start_index node) 
                            (:_start_index line))]

              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(:_start_index %) (my-seq line))
          )) 




(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )

(defn get-source[node txt]
  (if  (instaparse.core/span node)
    (apply subs txt (instaparse.core/span node))
    node
    ))

(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn json-key-fn[x]
  (let [y (name x)]
    (if (= \_ (first y))
      (subs y 1)
      y)))

(defn pp-to-json[x]
  "For debugging, pretty print json output. Not useable"
  (json/pprint :key-fn json-key-fn)) 

(defn- my-to-json[x]
  "returns json/text version of parse tree. It is a string"
  (json/write-str x :key-fn json-key-fn))

(defn- update-sargam-pitch-node [pitch nodes]
  (if false
    (do
      (println "update-sargam-pitch-node********")
      (pprint pitch)
      (println "nodes")
      (pprint nodes)
      (println "end nodes")
      (println "********")
      ))
  ;; nodes should get transformed into attributes--
  ;;attributes: 
  ;;                    [ { _my_type: 'begin_slur', source: '(' },
  ;;                     { _my_type: 'chord_symbol', source: 'F', column: 40 },
  ;;                    { _my_type: 'ending', source: '1_____', num: 1, column: 40 } ],                       
  ;; 
  (if false
    (let [
          content (rest pitch)
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
      ))


  (let [octave 0 
        syllable nil 
        chord nil
        ornament nil
        mordent nil
        syls []
        chords []
        ornaments []
        tala nil]
    (merge pitch 
           {
            :attributes []
            ;; :pitch_source (sarg sargam-pitch-to-source)
            ;; :source (sarg sargam-pitch-to-source)
            :column_offset 0
            :octave octave
            ;; :numerator 1
            ;; :denominator 1
            ;; :fraction_array  [ { :numerator , denominator: 1 } ]nil ;; TODO: review
            :syllable syllable
            :chord chord
            :ornament  ornament ;; just the pitches ornament
            :tala tala
            :mordent mordent
            }
           ))
  )

(defn collapse-sargam-section [sargam-section txt]
  "main logic is here"
  "Deals with the white-space significant aspect of doremi-script"
  "given a section like

  .
  S
  Hi

  "
  "Returns the sargam-line with the associated objects in the same column attached
  to the corresponding pitches/items on main line. The dot becomes an upper octave of S and 
  Hi becomes a syllable for S"
  "Assign attributes to the main line(sargam_line) from the lower and upper lines using 
  column numbers. Returns a sargam-line"
  (assert (= (:_my_type sargam-section) :sargam_section))
  (assert (string? txt))
  (let [
        sargam-line (some #(if (= (:_my_type %) :sargam_line) %)
                          (:items sargam-section))
        column-map (reduce line-column-map {}  (:items sargam-section))
        line-starts (map :_start_index (:items sargam-section))
        line-start-for  (fn line-start-for-fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (:_start_index node) (line-start-for (:_start_index node))))
        postwalk-fn (fn sargam-section-postwalker[node]
                      "TODO: in progress- rewrite for new version!!copied from old"
                      (cond
                        (not (map? node))
                        node
                        (not (:_my_type node))
                        node
                        true
                        (let [
                              column (column-for-node node)
                              nodes (get column-map column) 
                              my-type (:_my_type node)
                              source (:_source  node)
                              k :unused
                              ;x (println "in sargam-section-postwalker. my-type =>")
                              ;z (pprint my-type)
                              ]
                          (cond
                            (= :begin_slur_sargam_pitch my-type)
                            ;; unwrap the sargam-pitch, setting begin-slur attribute 
                            (let [
                                  sargam-pitch (second (:items node) )
                                  ]
                              ;;(println "sargam-pitch**** --> " sargam-pitch)
                              ;;   { _my_type: 'begin_slur', source: '(' }
                              (assoc-in sargam-pitch [:attributes (count (:attributes sargam-pitch))] 
                                        (sorted-map :_my_type "begin_slur" 
                                                    :_source source
                                                    )))
                            (= :zpitch_with_dashes my-type)
                            (do
                              ;; (println "***************" my-type)
                              ; return an array:  [sargampitch dash dash]. 
                              ; Need to flatten it later
                              (assoc-in (:items node) [0 :numerator] 
                                        (count (filter (fn[x] (#{:pitch :dash} (:_my_type x)))
                                                       (:items node))))
                              )
                            (= :LINE_NUMBER k)
                            (sorted-map :_my_type "line_number"
                                        :_source source)
                            ;;  { my_type: 'line_number', source: '1)', column: 0 }
                            (= my-type :pitch)
                            (update-sargam-pitch-node node nodes)
                            (= :SARGAM_ORNAMENT_PITCH k)
                            node
                            ;; (update-sargam-ornament-pitch-node node nodes)
                            true
                            node))))
        ]
    (assert (= :sargam_line (:_my_type sargam-line)))
    (assert (map? column-map))
    (postwalk postwalk-fn sargam-section)
    ))

(def unit-of-rhythm
  #{:pitch :dash})

(defn make-sorted-map[node]
  (cond 
    true
    node
    (and (map? node) (= (into (hash-set) (keys node)) #{:numerator :denominator}))
    node
    (map? node)
    (into (sorted-map) node)
    true
    node))

(defn make-maps-sorted[x]
  (into (sorted-map) (postwalk make-sorted-map x)))

(defn backwards-comparator[k1 k2]
  (compare k2 k1))

(defn main-walk[node txt]
  ;; (pprint node)
  (cond
    (not (vector? node))
    node
    true
    (let [my-key (first node)
          my-map (array-map :_my_type (keyword (lower-case (name my-key)))
                            :_source (get-source node txt)
                            :_start_index (start-index node) 
                            )
          ;;zz (pprint "&&&&&&&&")
          ;;z (pprint node)
          node2 (if (and (vector? (second node)) 
                         (keyword? (first (second node)))
                         (.endsWith (name (first (second node))) "ITEMS"))
                  (merge {:items (subvec  (second node) 1) } my-map)
                  ;; else
                  node)
          ]
      (cond
        (#{:UPPER_OCTAVE_LINE :BEGIN_SLUR_SARGAM_PITCH} my-key)
        (merge  my-map (array-map :items (subvec node 1)) )

        (= :COMPOSITION my-key)
        (let [ sections 
              (filter #(= :sargam_section (:_my_type %))  (:items node2))
              lines
              (into [] (map  (fn[x] (some #(if (= :sargam_line (:_my_type %))
                                             %) 
                                          (:items x))) sections))
              ] 
          (merge {:lines  lines 
                  :warnings []
                  :id 999
                  :notes_used ""
                  :force_sargam_chars_hash {}
                  :time_signature "4/4"
                  :mode "major"
                  :key "C"
                  :author ""
                  :apply_hyphenated_lyrics false
                  :title ""
                  :filename "untitled"
                  } 
                 my-map))
        (= :PITCH_WITH_DASHES my-key)
        ;; returns [ pitch dash dash ]
        (let [my-items (subvec node2 1)
              ]
          (if false (do
                      (pprint "my-items -->")
                      (pprint my-items)
                      (pprint ":PITCH_WITH_DASHES case")))
          (assoc-in my-items [0 :numerator] 
                    (count 
                      (filter (fn[x] (#{:pitch :dash} (:_my_type x)))
                              my-items))))
        (= :BEAT my-key)
        (let [
              ;; [ [pitch dash dash] pitch pitch ] => [pitch dash dash pitch pitch]
              ;; apply concat to get rid of pitch with dashes' array
              my-fun (fn[z]
                       (apply concat (into [] (map (fn[x] (if (vector? x) x (vector x))) z))))
              items2 (into [] (my-fun (:items node2)))  ;; TODO: ugly
              subdivisions 
              (count (filter (fn[x] (unit-of-rhythm (:_my_type x))) 
                             items2))
              my-beat (assoc node2 :items items2 :_subdivisions subdivisions)
              ]
          ;;;
          ;;;(pprint my-beat)
          (postwalk (fn postwalk-in-beat[z] 
                      ;; (println "postwalk-fn2 -z ---------->")
                      ;; (pprint z)
                      (assert (not (= 0 subdivisions)))
                      ;;(println "z is")
                      ;;(pprint z)
                      (cond 
                        (= :beat (:_my_type z))
                        z
                        ;; (assoc z :items (into [] (apply concat (:items z))))
                        (not (#{:pitch :dash} (:_my_type z)))
                        z
                        true
                        (let [my-ratio (/ (:numerator z) subdivisions)
                              ;; zz (println "my-ratio is" my-ratio)
                              frac 
                              (if (= (class my-ratio) java.lang.Long)
                                (sorted-map-by backwards-comparator  :numerator 1 
                                               :denominator 1) 
                                ;; else 
                                (sorted-map-by  backwards-comparator 
                                               :numerator (numerator my-ratio)
                                               :denominator (denominator my-ratio)))
                              ]
                          (assoc z 
                                 :denominator subdivisions
                                 :fraction_array 
                                 [ frac ]))))
                    my-beat))
        (#{:UPPER_OCTAVE_DOT :LINE_NUMBER :BEGIN_SLUR :END_SLUR} my-key)
        my-map
        (= :DASH my-key)
        (merge my-map (sorted-map :numerator 1))
        (= :BARLINE my-key)
        (merge  my-map (sorted-map :_my_type (keyword (keyword (lower-case (name (get-in node [1 0])))))
                                   :is_barline true))
        (= :SARGAM_SECTION my-key)
        (collapse-sargam-section 
          (merge (sorted-map :items (subvec node 1)) my-map)
          txt)
        (= :SARGAM_PITCH my-key)
        (let [
              sarg  (some #(if (= (first %) :SARGAM_MUSICAL_CHAR) (first (second %))) (rest node))
              ]
          (merge 
            my-map
            (sorted-map  
              :_my_type :pitch
              :numerator 1  ;;; numerator and denominator may get updated later!
              :denominator 1
              :normalized_pitch (sarg to-normalized-pitch)
              :pitch_source (sarg sargam-pitch-to-source)
              )))
true
node2
)))) 

(defn doremi-script-parse[txt]
  "parse the text"
  ;;(println "processing")
  ;;(println txt)
  (make-maps-sorted      
    (postwalk (fn[node] (main-walk node txt)) 
              (run-through-parser txt))
    ))
;;(pprint (run-through-parser "S"))
;;(pprint (doremi-script-parse "|S"))
(defn doremi-script-to-json[txt]
  (my-to-json (doremi-script-parse txt)))

