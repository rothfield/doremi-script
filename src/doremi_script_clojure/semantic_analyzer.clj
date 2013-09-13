(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk postwalk-replace keywordize-keys]]
    [clojure.string :refer [lower-case]]
    [instaparse.core :as insta]
    [clojure.data.json :as json]
    ))
(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'clojure.stacktrace) (use 'doremi_script_clojure.semantic_analyzer :reload) (ns doremi_script_clojure.semantic_analyzer) 
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
  #{ :sargam_pitch  :dash}
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
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (:_start_index node) (line-start-for (:_start_index node))))
        postwalk-fn (fn postwalker[node]
                      "TODO: in progress- rewrite for new version!!copied from old"
                      (if
                        (not (map? node))
                        node
                        ;; else
                        (let [
                              column (column-for-node node)
                              nodes (get column-map column) 
                              my-type (:_my_type node)
                              source (:_source  node)
                              k :unused
                              ;;x (println "in postwalker")
                              ;;z (pprint node)
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
                            (= :pitch_with_dashes my-type)
                            ; return an array:  [sargampitch dash dash]. 
                            ; Need to flatten it later
                            (assoc-in (:items node) [0 :numerator] 
                                      (count (filter (fn[x] (#{:sargam_pitch :dash} (:_my_type x)))
                                                     (:items node))))
                            true
                            node
                            (= :LINE_NUMBER k)
                            (sorted-map :_my_type "line_number"
                                        :_source source)
                            ;;  { my_type: 'line_number', source: '1)', column: 0 }
                            (= :BARLINE k)
                            (do
                              ;  (println "*****************BARLINE case**")
                              ;  (pprint node)
                              ;; [:BARLINE [:SINGLE_BARLINE "|"]]
                              (sorted-map
                                :_my_type (lower-case (name (get-in node [1 0])))
                                :is_barline true
                                :_source source))
                            (= :SARGAM_PITCH k)
                            node
                            ;;; (update-sargam-pitch-node node nodes)
                            (= :SARGAM_ORNAMENT_PITCH k)
                            node
                            ;; (update-sargam-ornament-pitch-node node nodes)
                            (= :BEAT k)
                            node
                            ;;;  (transform-beat node)
                            true
                            node))))
        ]
    (assert (= :sargam_line (:_my_type sargam-line)))
    (assert (map? column-map))
    (postwalk postwalk-fn sargam-section)
    ))

(defn doremi-script-parse[txt]
  (println "processing")
  (println txt)
  (postwalk (fn[node]
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
                      node2 (if (and (vector? (second node)) 
                                     (.endsWith (name (first (second node))) "ITEMS"))
                              (merge {:items (subvec  (second node) 1) } my-map)
                              ;; else
                              node)
                      ]
                  (cond
                    (#{:UPPER_OCTAVE_LINE :BEGIN_SLUR_SARGAM_PITCH :PITCH_WITH_DASHES} my-key)
                    (merge (sorted-map :items (subvec node 1)) my-map)
                    (= :BARLINE my-key)
                    (merge    my-map (sorted-map
                                       :_my_type (keyword (keyword (lower-case (name (get-in node [1 0])))))
                                       :is_barline true
                                       ))
                    (#{:UPPER_OCTAVE_DOT :LINE_NUMBER :DASH :BEGIN_SLUR :END_SLUR} my-key)
                    my-map
                    (= :SARGAM_PITCH my-key)
                    (let [
                          sarg  (some #(if (= (first %) :SARGAM_MUSICAL_CHAR) (first (second %))) (rest node))
                          ]
                      (merge 
                        (sorted-map 
                          :_my_type "pitch"
                          :normalized_pitch (sarg to-normalized-pitch)
                          :pitch_source (sarg sargam-pitch-to-source)
                          ) my-map))

                    (= :SARGAM_SECTION my-key)
                    (collapse-sargam-section 
                      (merge (sorted-map :items (subvec node 1)) my-map)
                      txt)
                    true
                    node2)))) 
            (run-through-parser txt)))

(pprint (doremi-script-parse "*\n(S--"))
(defn doremi-script-to-json[txt]
  (my-to-json (doremi-script-parse txt)))

