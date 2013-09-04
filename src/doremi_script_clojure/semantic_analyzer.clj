(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk postwalk-replace keywordize-keys]]
    [clojure.string :refer [lower-case]]
    [instaparse.core :as insta]
    ))
(defn p[] (println "************************"))

(def yesterday 
     (slurp (io/resource "fixtures/yesterday.txt")))

(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'clojure.stacktrace) (use 'doremi_script_clojure.semantic_analyzer :reload) (ns doremi_script_clojure.semantic_analyzer) 
  (print-stack-trace *e)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )

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



(defn line-column-map [my-map line]
  "my-map is a map from column number -> list of nodes
  Add nodes from line to the map"
  (reduce (fn [accumulator node]
            (let [column (- (start-index node) 
                            (start-index line))]

              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(not= nil (start-index %)) (tree-seq vector? rest line))
          )) 


(defn section-column-map [sargam-section]
  "For a sargam section, maps column-number --> list of nodes in that column"
  (reduce line-column-map {}  sargam-section)
  )


(defn- update-sargam-ornament-pitch-node [sargam-ornament-pitch nodes]
  "UNFINISHED"
  sargam-ornament-pitch
  )

(def unit-of-rhythm
  #{ :SARGAM_PITCH  :DASH}
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
  ;; (println "unit-of-rhythm")
  ;;(pprint x)
  ;;(println "unit-of-rhythm") 
  (unit-of-rhythm (first x))
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

(defn update-sargam-line-node[sargam-line nodes]
  "add syllables attribute to sargam line"
  (conj sargam-line [ :syllables  
                     (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
                     ])) 

"parse and run through semantic analyzer. Txt is doremi-script"
;;(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nThese are all the lyrics\nSecond lyric line\n\nRG.\nS | R | G\nhe-llo\n\n |m P D - |\n\nMore:attributes"))
;; (pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (doremi-script-parser (slurp-fixture "yesterday.txt")))


;;(doremi-script-parser "S--R--" :start :BEAT)

(defn divisions-in-beat[beat]
(defn divisions-in-beat[beat]
  "for example SRG has 3 divisions. S--- has 4   - has one"
  (count (filter #(and (vector? %) (unit-of-rhythm (first %))) 
                 (tree-seq vector? rest beat))))
  "for example SRG has 3 divisions. S--- has 4   - has one"
  (count (filter #(and (vector? %) (unit-of-rhythm (first %))) 
                 (tree-seq vector? rest beat))))

;;(pprint (doremi-script-parser "S-RG"))
(defn transform-beat [beat]
  "Add divisions attribute to beat"
  (conj beat [:divisions 
              (divisions-in-beat beat)]))

(defn collapse-sargam-section[sargam-section]
  ;;;(println "collapse-sargam-section")
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
  ;; (pprint (rest sargam-section))
  ;;(pprint "____")
  (let [
        sargam-section-content (rest sargam-section)
        sargam-line (first (filter #(and (vector? %) (= :SARGAM_LINE (first %))) sargam-section-content))
        lyrics-line (first (filter #(and (vector? %) (= :LYRICS_LINE (first %))) sargam-section-content))
        syllables  (filter #(and (vector? %) (= :SYLLABLE (first %))) (tree-seq vector? rest lyrics-line))
        line-number  (some #(if (and (vector? %) (= :LINE_NUMBER (first %)))
                              %   ) 
                           (tree-seq vector? rest sargam-line))
        column-map (section-column-map sargam-section)
        line-starts (map start-index sargam-section-content)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node))))
        postwalk-fn (fn postwalker[node]
                      (if
                        (not (vector? node))
                        node
                        ;; else
                        (let [
                              column (column-for-node node)
                              nodes (get column-map column) 
                              k (first node)
                              source (:_source (meta node))

                              ]
                          (cond
                            (= :BEGIN_SLUR_SARGAM_PITCH k)
                            ;; unwrap the sargam-pitch, setting begin-slur attribute !!
                            (let [
                                  sargam-pitch (get node 2) 
                                  ]
                              ;;(println "sargam-pitch**** --> " sargam-pitch)
                              ;;   { _my_type: 'begin_slur', source: '(' }
                              (assoc-in sargam-pitch [:attributes (count (:attributes sargam-pitch))] 
                                        (sorted-map :_my_type "begin_slur" 
                                                    :_source source
                                                    )))
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
                            (update-sargam-ornament-pitch-node node nodes)
                            (= :BEAT k)
                            (transform-beat node)
                            true
                            node))))
        sargam-section-content2 (postwalk postwalk-fn sargam-section-content)
        sargam-line2 (some #(if (and (vector? %) (= :SARGAM_LINE (first %))) %) sargam-section-content2)
        items (some #(if (and (vector? %) (= :SARGAM_LINE_ITEMS (first %))) (subvec % 1)) sargam-line2)
        ]
      (assert (= :SARGAM_LINE (first sargam-line2)))
      (assert (not (nil? items)))
    ;(pprint sargam-line2) (p) (p) (p)
    ;(println "items--->")
    ;(pprint items) (p)(p)(p)(p)(p)
   ;; (pprint (first sargam-line2))
    (if false(do
                ;(println "sargam-section-content2")
    ;;            (pprint sargam-section-content2)
                (p)))
    (sorted-map :_my_type "sargam_line"
                :items 
                items
              ;;  (some #(if (and (vector %) (= :SARGAM_LINE_ITEMS (first %))) (second %))  (tree-seq vector? rest (into [] sargam-section-content2)))
                :line_number  (second line-number)
                :syllables syllables
                :_source (:_source (meta sargam-section)))))



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




(defn to-doremi-script-json[parse-tree txt]
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
                (merge  (sorted-map 
                          :_my_type "composition"
                          :items (filter #(not (vector? %)) (subvec (second node) 1))   ;; should filter out attributes TODO
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
                              :items (subvec (second node) 1)
                              ) my-map )]
                (assoc obj :beat_count
                       (count (filter (fn[x] (and (map? x) (#{"beat"} (:_my_type x)))) (tree-seq map? :items obj)))))
              (= k :BEAT)
              (let [ 
                    obj
                    (merge (sorted-map 
                             :_my_type "beat"
                             :items (subvec (second node) 1)
                             ) my-map)]
                (assoc obj :divisions 
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

(defn main4[txt]
  (-> txt
      run-through-parser
      (to-doremi-script-json txt)))


(defn collapse[parse-tree]
  ;;(println "entering collapse")
  ;;(pprint parse-tree)
  (postwalk (fn[x]
              (cond
                (and (vector? x) (= :SARGAM_SECTION (first x)))
                (collapse-sargam-section x)
                true
                x)) parse-tree)) 

(defn main5[txt]
  (if false
  (do (p)
  (println txt)
  (p)))
  (-> txt
      run-through-parser
      (add-source txt)
      collapse
      (to-doremi-script-json txt)
      fix-items-keywords 
      ))
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



(defn main4[txt]
  (-> txt
      run-through-parser
      (to-doremi-script-json txt)
      )
  )



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





;; (pprint (main "- -- SR G---"))
;; (pprint (main (slurp-fixture "yesterday.txt")))
;   (apply sorted-map parse-tree3)

;;  Pseudo code:
;;  given some doremi-script text:
;;  rationalize new lines => txt
;;  run through parser => parse-tree
;;  add source attribute to composition
;;  add column numbers
;;  fix items => parse-tree  tree-walk ???
;;  process sargam sections  tree-walk ?? 
;;  turn into hash equivalent of the javascript version
;;
;;  TODO:
;;  Add tie flags
;;  Add numerator/denominator to pitches. Use divisions from beat.
;;  For each beat, set the denominator of each pitch to the beat's divisions
;;  The numerator will be the count for pitch with spaces
;;  Add normalized pitch
;   Finally, generate json compatible with existing javascript version

;;(pprint (main5 "1) |: S | R |  G |]"))
;(pprint (main5 yesterday))
;; (pprint (main5 "hi:john\n\n   Gm7\n1) S | r r r r |\n   hi"))
;(pprint (main5 "hi:john\n\n   Gm7\n   S | r r r r |\n   hi"))
;(pprint (main5 "Gm7\nS"))
;(pprint (main5 "1) S-"))

