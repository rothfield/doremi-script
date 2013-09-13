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

(defn seqable?
  "Returns true if (seq x) will succeed, false otherwise."
  [x]
  (or (seq? x)
      (instance? clojure.lang.Seqable x)
      (nil? x)
      (instance? Iterable x)
      (-> x .getClass .isArray)
      (string? x)
      (instance? java.util.Map x)))

(defn flatten-things 
  [x fct]
  "Flattens items where (fct (head z)) is true"
  "Flattens only the first level"
  (if (or (not (seqable? x)) (nil? x))
    x ; if x is nil or not a sequence, don't do anything
    (loop [acc [] [elt & others] x]
      (if (nil? elt) acc
        (recur
          (if (and (seqable? elt) (fct (first elt)))
            (apply conj acc (rest elt)) ; if elt is a sequence, add each element of elt
            (conj acc elt))      ; if elt is not a sequence, add elt itself directly
          others)))))


(def bar [1 [:pitch-with-dashes 3 4 ] [ 5 6] 7 8 [9 [10 11 12]]])
;;(pprint (flatten-things bar #{:pitch-with-dashes}))
(def yesterday 
  (slurp (io/resource "fixtures/yesterday.txt")))


(def debug false)

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

(defn unit-of-rhythm?[x]
  (unit-of-rhythm (:_my_type x))
  )


(defn- update-sargam-pitch-node [sargam-pitch nodes]
  ;; (S  is causing problems because the column is off by 1
  ;; same with things like <foo>
  ;; perhaps have parser produce
  ;; (pitch-with-left paren (sargam-pitch "S")) and then
  ;; unwrap pitch-with-left-paren ???. Or change parser.
  ;;  (S --> left-paren S
  ;; TODO: breaks with begin/end slur
  (if false
    (do
      ;(println "update-sargam-pitch-node********")
      ;(pprint sargam-pitch)
      ;(println "nodes")
      ;(pprint nodes)
      ;(println "end nodes")
      ;(println "********")
      ))
  (comment [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:R]]])
  (comment [:SARGAM_PITCH [:BEGIN_SLUR "("] [:SARGAM_MUSICAL_CHAR [:S]]])
  " Use the list of nodes to update a sargam-pitch.
  Returns the new node."
  ;; nodes should get transformed into attributes--
  ;;attributes: 
  ;;                    [ { _my_type: 'begin_slur', source: '(' },
  ;;                     { _my_type: 'chord_symbol', source: 'F', column: 40 },
  ;;                    { _my_type: 'ending', source: '1_____', num: 1, column: 40 } ],                       
  ;; 
  (let [
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
        ;;      sarg (first (second (second sargam-pitch)))
        ;; sarg (first (second (second sargam-pitch)))
        sarg  (some #(if (= (first %) :SARGAM_MUSICAL_CHAR) (first (second %))) (rest sargam-pitch))
        ]
    ; (pprint z)
    ;; Note that we support multiple syllables, ornaments, and chords per note. But only return the last (for now)
    ;;   (pprint "in update-sargam-pitch-node")
    ;;  (pprint "sargam-pitch is")
    ;;(println "sarg" sarg)
    (sorted-map 
      :_my_type "pitch"
      :normalized_pitch
      (sarg to-normalized-pitch)
      :attributes []
      :pitch_source (sarg sargam-pitch-to-source)
      :_source (:_source (meta sargam-pitch))
      ;; :source (sarg sargam-pitch-to-source)
      :column_offset 0
      :octave
      octave
      :numerator 1
      :denominator 1
      ;; :fraction_array  [ { :numerator , denominator: 1 } ]nil ;; TODO: review
      :syllable
      (second (last syls))
      :chord
      (last chords)
      :ornament   ;; just the pitches
      (into (vector) (rest (last ornaments)))
      :tala
      tala
      :mordent
      (second mordent)
      )))


(defn divisions-in-beat[beat]
  "for example SRG has 3 divisions. S--- has 4   - has one"
  "TODO"
  (count (filter #(and (vector? %) (unit-of-rhythm (first %))) 
                 (tree-seq vector? rest beat))))

(defn transform-beat [beat]
  "Add divisions attribute to beat. Set denominator for every unit of rhythm to divisions"
  "TODO: assign denominator to every pitch or dash in the beat. Can use tree-walk"
  (pprint beat)
  (pprint "******")
  (let [ 
        unit-of-time #{"pitch" "dash"}
        my-map (array-map :_source (:_source (meta beat)))
        beat2 (merge (sorted-map 
                       :_my_type "beat"
                       :items (subvec (second beat) 1)
                       ) my-map)
        ;;  x (pprint beat2)
        ;; y (pprint "*****")
        subdivisions 
        (count (filter (fn[x] (and (map? x) unit-of-time (:_my_type x))) 
                       (tree-seq map? :items beat2)))

        beat3 (assoc beat2 :subdivisions subdivisions)
        postwalk-fn (fn postwalk-fn[node]
                      (cond
                        (and (map? node) (= "pitch" (:_my_type node)))
                        (assoc node :denominator subdivisions
                               :fraction_array 
                               [ {:numerator (:numerator node)  
                                  :denominator subdivisions}])
                        true
                        node 
                        ))]
    (postwalk postwalk-fn beat3)))

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
  (if false 
    (do
  (pprint "entering line-column-map: line:")
  (pprint line)
  (pprint "****")
 ;; (pprint "*** my-seq line")
;(;  (pprint (my-seq line))
  (pprint "****")
      )
    )
  (reduce (fn [accumulator node]
            (let [column (- (:_start_index node) 
                            (:_start_index line))]

              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(:_start_index %) (my-seq line))
          )) 


(def sample3
  {:_my_type :sargam_section,
   :_source "S--",
   :items
   [{:_source "S--",
     :_my_type :sargam_line,
     :items
     [{:_source "S--",
       :_my_type :measure,
       :items
       [{:_source "S--",
         :_my_type :beat,
         :items
         [{:_my_type :pitch_with_dashes,
           :_source "S--",
           :items
           [{:_my_type :sargam_pitch,
             :_source "S",
             :normalized_pitch "C",
             :pitch_source "S"}
            {:_my_type :dash, :_source "-"}
            {:_my_type :dash, :_source "-"}]}]}]}]}]}

  )


(comment
(pprint "***************")
(pprint (my-seq sample3))
(pprint "***************")
(pprint (my-seq  {:items [ 99 {:items [1 2 3]} 2 3]}))
(pprint (my-seq  {:items [ 1 2 3]}))
(pprint "***************")
)





(defn fix-items-keywords[parse-tree]
  "replace keywords like :COMPOSITION_ITEMS with :items"  
  (let [
        make-low '(:COMPOSITION
                    :SARGAM_LINE
                    :BEAT
                    :MEASURE
                    :LYRICS_LINE
                    :BARLINE
                    :SARGAM_PITCH
                    :SARGAM_SECTION
                    :SARGAM_MUSICAL_CHAR
                    :SYLLABLE)
        map2 (into {} (map (fn[x] [x (keyword (lower-case (name x)))]) make-low))
        ;; map2 
        ;;(apply hash-map (m 
        my-map 
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
    (postwalk-replace (merge my-map map2) parse-tree)
    ))

(defn zfix-items-keywords[parse-tree]
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
  ;    [:ATTRIBUTE_SECTION [:ATTRIBUTE_SECTION_ITEMS "title" "Hello"]]
  (assoc x 1 (keywordize-keys (apply array-map (rest (second x))))))
(comment
  "TODO - convert output of parser to map ???"
  "walk should do it"
  [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]
  )


(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )
(defn get-source[node txt]
  (if  (instaparse.core/span node)
    (apply subs txt (instaparse.core/span node))
    node
    ))

(defn add-source[parse-tree txt]
  ;;(println parse-tree)
  "Add source to the nodes we are interested in"
  (postwalk (fn[node] (cond
                        (not (vector? node))
                        node
                        true
                        (vary-meta node assoc :_source 
                                   (get-source node txt)                                     
                                   )))
            parse-tree))


(defn run-through-parser[txt]
  (doremi-script-parser txt))

;;(pprint (main "  He-llo this is john\n\nS\nhe-llo\n\nMore ly-rics\n\nG  "))
(defn unused-add-source-to-nodes[parse-tree txt]
  (let [my-fn
        (fn[node]
          ;;(println "node is " node)
          (cond 
            (and (vector? node) (instaparse.core/span node))
            (conj node [:_source (apply subs txt (instaparse.core/span node))])
            true
            node
            ))] 
    ;;(println "add-source-to-nodes")
    ;;(println "txt is ")
    ;;(println txt)
    (postwalk my-fn parse-tree)))




(defn to-doremi-script[parse-tree txt]
  "converts parse tree to json to be compatible with "
  "javascript version of doremi-script"
  "latest attempt"
  (let [

        ;;;;        line-starts 
        ;;;;        (into [0] (keep-indexed #(if (= \newline %2) %1)  txt))
        ;;;;
        ;;;;        line-start-for  (fn[column] 
        ;;;;                          (last (filter (fn[x] (>= column x)) line-starts)) )
        ;;;;        column-for-node (fn[node]
        ;;;;                          ;(println line-starts "node is " node "start-index" (start-index node))
        ;;;;                          (- (start-index node) (line-start-for (start-index node))))
        ;;;;        line-map 
        ;;;;        (into {} (map-indexed (fn[a b] [b a]) line-starts)) 
        ;;;;        line-number-for-node (fn[node]
        ;;;;                               (second (last (filter (fn[x] (>= (start-index node) (first x)))
        ;;;;                                                     line-map)) )
        ;;;;                               )
        ]
    ;; (pprint line-map)
    (postwalk 
      (fn my-fn[node] 
        (cond
          (not (vector? node))
          node
          true
          (let [k (first node)
                my-map (array-map :_source (:_source (meta node)))
                ]
            (cond
              (= k :COMPOSITION)
              (let [ 
                    att-section-items 
                    (some (fn[x] (if (and (vector x) (= (first x) :ATTRIBUTE_SECTION_ITEMS)) x)) 
                          (tree-seq vector? rest node))
                    att-list (if (nil? att-section-items) []

                               (subvec att-section-items 1))
                    ] 
                ;(println "***node-->")
                ;(pprint node)
                ;  (println "att-section-items")
                ;  (pprint att-section-items)
                ;;   warnings: [],
                ;;   source: '|S\n',
                ;;   toString: [Function],
                ;;   id: 1378307158367,
                ;;   notes_used: '',
                ;;   force_sargam_chars_hash: {},
                ;;   time_signature: '4/4',
                ;;   mode: 'major',
                ;;   key: 'C',
                ;;   author: '' }
                ;;   apply_hyphenated_lyrics: false,
                ;;   title: '',
                ;;   filename: 'untitled',
                (merge  (sorted-map 
                          :_my_type "composition"
                          :lines (filter #(not (vector? %)) (subvec (second node) 1))   ;; should filter out attributes TODO
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

                          :attributes (sorted-map
                                        :_source (:_source (meta att-section-items))
                                        :_my_type "attributes"
                                        :items (into [] (map (fn[[a b]] {:key a :value b}) (partition 2 att-list)) )
                                        ))
                       my-map))
              (= k :MEASURE)
              (let [ 
                    obj
                    (merge  (sorted-map 
                              :_my_type "measure"
                              :id 999 ;; TODO
                              :beat_count  1 ;; TODO
                              :is_partial false ;; TODO
                              :items (subvec (second node) 1)
                              ) my-map )]
                (assoc obj :beat_count
                       (count (filter (fn[x] (and (map? x) (#{"beat"} (:_my_type x)))) (tree-seq map? :items obj)))))
              (= k :zBEAT)  ;; moving to transform-beat
              (let [ 
                    obj
                    (merge (sorted-map 
                             :_my_type "beat"
                             :items (subvec (second node) 1)
                             ) my-map)]
                (assoc obj :subdivisions 
                       (count (filter (fn[x] (and (map? x) (#{"pitch" "dash"} (:_my_type x)))) (tree-seq map? :items obj)))
                       )
                )
              (= k :ZATTRIBUTE_SECTION)
              (process-attribute-section node)
              (= k :zPITCH_WITH_DASHES)
              (let [
                    items  (subvec node 1)
                    ]
                (merge (sorted-map 
                         :_my_type "pitch_with_dashes"
                         :items (assoc-in items [0 :numerator] (count items))
                         ) my-map ))
              (= k :zSARGAM_PITCH)
              (let [
                    sarg (first (second (second node)))
                    ]
                ;; (println "sargam pitch********")
                (merge (sorted-map 
                         :_my_type "pitch" 
                         :normalized_pitch
                         (sarg to-normalized-pitch)
                         :attributes {}
                         :pitch_source (sarg sargam-pitch-to-source)
                         ) 
                       my-map
                       ))
              (= k :DASH)
              (merge (sorted-map :_my_type "dash") my-map  )
              true
              node
              ))))
parse-tree)))


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

;;(pprint (main6 "S"))
;;(pprint (main6 "S"))

(comment
  ((postwalk (fn[x] (cond
                      (and (vector? x) (= :sargam_section (first x)))
                      x
                      true
                      x)))))

(def z (doremi-script-parser "title:Hello\nAuthor: John\n\nS\n\nR"))
(def z2 " 1.__\n Gm7\n +\n(S\n hi")

;(pprint (main5 "+\n(S)\nhi"))
;;(pprint (main5 yesterday))

(def yyy
  ;; "with parser input"
  ;; "hi:john\n\nS\n\nR" 
  [:COMPOSITION
   [:COMPOSITION_ITEMS
    [:ATTRIBUTE_SECTION [:ATTRIBUTE_SECTION_ITEMS "title" "hi"]]
    [:SARGAM_SECTION
     [:UPPER_OCTAVE_LINE [:TALA "+"]]
     [:SARGAM_LINE
      [:SARGAM_LINE_ITEMS
       [:BARLINE [:SINGLE_BARLINE "|"]]
       [:MEASURE
        [:MEASURE_ITEMS
         [:BEAT
          [:BEAT_UNDELIMITED_ITEMS
           [:PITCH_WITH_DASHES
            [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]
            [:DASH "-"]]]]
         [:BEAT
          [:BEAT_UNDELIMITED_ITEMS
           [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]]]
         [:BEAT [:BEAT_UNDELIMITED_ITEMS [:DASH "-"] [:DASH "-"]]]
         [:BEAT [:BEAT_UNDELIMITED_ITEMS [:DASH "-"]]]]]]]
     [:LYRICS_LINE [:LYRICS_LINE_ITEMS [:SYLLABLE "hi"]]]]
    [:SARGAM_SECTION
     [:SARGAM_LINE
      [:SARGAM_LINE_ITEMS
       [:MEASURE
        [:MEASURE_ITEMS
         [:BEAT
          [:BEAT_UNDELIMITED_ITEMS
           [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:R]]]]]]]]]]]]
  )
(def xxx
  ;;comment
  ;;"output of instaparse parser for "
  "S"
  [:COMPOSITION
   [:COMPOSITION_ITEMS
    [:SARGAM_SECTION
     [:SARGAM_LINE
      [:SARGAM_LINE_ITEMS
       [:MEASURE
        [:MEASURE_ITEMS
         [:BEAT
          [:BEAT_UNDELIMITED_ITEMS
           [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]]]]]]]]]]
  )


;;(println (main6 "S-"))
;(pprint (main5 yesterday))
;; (pprint (main5 "hi:john\n\n   Gm7\n1) S | r r r r |\n   hi"))
;(pprint (main5 "hi:john\n\n   Gm7\n   S | r r r r |\n   hi"))
;(pprint (main5 "Gm7\nS"))
;(pprint (main5 "1) S-"))
;;(pprint (main5 "S-- r-"))
;;
;;
;;
(def sample-parser-output
  [:COMPOSITION
   [:COMPOSITION_ITEMS
    [:SARGAM_SECTION
     [:SARGAM_LINE
      [:SARGAM_LINE_ITEMS
       [:MEASURE
        [:MEASURE_ITEMS
         [:BEAT
          [:BEAT_UNDELIMITED_ITEMS
           [:PITCH_WITH_DASHES
            [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]
            [:DASH "-"]
            [:DASH "-"]]
           [:PITCH_WITH_DASHES
            [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:r]]]
            [:DASH "-"]
            [:DASH "-"]]
           [:PITCH_WITH_DASHES
            [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:g]]]
            [:DASH "-"]]]]]]]]]]]
  )  


(def sample-sargam-section
{:_my_type :sargam_section,
 :_source "*\nS--",
 :_start_index 0,
 :items
 [{:_my_type :upper_octave_line,
   :_source "*",
   :_start_index 0,
   :items
   [{:_my_type :upper_octave_dot, :_source "*", :_start_index 0}]}
  {:_start_index 2,
   :_source "S--",
   :_my_type :sargam_line,
   :items
   [{:_start_index 2,
     :_source "S--",
     :_my_type :measure,
     :items
     [{:_start_index 2,
       :_source "S--",
       :_my_type :beat,
       :items
       [{:_my_type :pitch_with_dashes,
         :_source "S--",
         :_start_index 2,
         :items
         [{:_my_type :sargam_pitch,
           :_source "S",
           :_start_index 2,
           :normalized_pitch "C",
           :pitch_source "S"}
          {:_my_type :dash, :_source "-", :_start_index 3}
          {:_my_type :dash, :_source "-", :_start_index 4}]}]}]}]}]}
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
  (if false (do
  (pprint "**entering collapse-sargam-section**")
  (pprint "sargam-section is:")
  (pprint sargam-section)
  (pprint "****")))
  ;;(pprint (map start-index (my-seq sargam-section)))
  ;;(pprint "(map start-index (my-seq sargam-section)))****")

  ;;(assert (= :sargam_section (:_my_type sargam-section)))
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
                            (update-sargam-pitch-node node nodes)
                            (= :SARGAM_ORNAMENT_PITCH k)
                            node
                           ;; (update-sargam-ornament-pitch-node node nodes)
                            (= :BEAT k)
                            (transform-beat node)
                            true
                            node))))
        ]
    (assert (= :sargam_line (:_my_type sargam-line)))
    (assert (map? column-map))
    (if false (do
    (pprint "sargam-line:")
    (pprint sargam-line)
    (pprint "line-starts:")
    (pprint line-starts)
    (pprint "column-map")
    (pprint column-map)))
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

(defn main6[txt]
  (my-to-json (doremi-script-parse txt)))

(defn main7[txt]
  (pp-to-json (doremi-script-parse txt)))

