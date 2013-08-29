(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.pprint :refer [pprint]] 
    [clojure.string :refer [lower-case]]
    [clojure.walk :refer [postwalk postwalk-replace keywordize-keys]]
    [instaparse.core :as insta]
    ))


(defn slurp-fixture [file-name]
  (slurp (clojure.java.io/resource 
           (str "resources/fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))

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

(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.semantic-analyzer :reload) (ns doremi_script_clojure.semantic-analyzer) (use 'clojure.stacktrace)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )

(def debug false)

(def doremi-script-parser  
  (insta/parser
    (slurp (clojure.java.io/resource "resources/doremiscript.ebnf")))
  )

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
            (let [column (:column node)
                            ]

              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(not= nil (:column %)) (tree-seq vector? rest line))
          )) 


(defn section-column-map [sargam-section]
  "For a sargam section, maps column-number --> list of nodes in that column"
  (reduce line-column-map {}  sargam-section)
  )

(defn collapse-sargam-section[sargam-section]
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
  (let [
        sargam-section-content (rest sargam-section)
        sargam-line (first (filter #(and (vector? %) (= :SARGAM_LINE (first %))) sargam-section-content))
        lyrics-line (first (filter #(and (vector? %) (= :LYRICS_LINE (first %))) sargam-section-content))
        syllables  (filter #(and (vector? %) (= :SYLLABLE (first %))) (tree-seq vector? rest lyrics-line))
        column-map (section-column-map sargam-section)
        column-for-node (fn[node]
                          (if (:column node)
                            (:column node)
                            999
                            ))
        postwalk-fn (fn[x]
                      (if
                        (not (vector? x))
                        x
                        ;; else
                        (let [
                              column (column-for-node x)
                              nodes (get column-map column) 
                              ]
                          (cond
                            (= :SARGAM_PITCH (first x))
                            (update-sargam-pitch-node x nodes)
                            (= :SARGAM_ORNAMENT_PITCH (first x))
                            (update-sargam-ornament-pitch-node x nodes)
                            (= :BEAT (first x))
                            (transform-beat x)
                            true
                            x))))
        sargam-section-content2 (postwalk postwalk-fn sargam-section-content)
        modified-sargam-line (first (filter #(= :SARGAM_LINE (first %)) sargam-section-content2))
        ]
    (conj modified-sargam-line (into [:syllables] syllables))
    ))

(defn new-collapse-sargam-section[sargam-section]
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
  (let [
        sargam-section-content (rest sargam-section)
        sargam-line (first (filter #(and (vector? %) (= :SARGAM_LINE (first %))) sargam-section-content))
        lyrics-line (first (filter #(and (vector? %) (= :LYRICS_LINE (first %))) sargam-section-content))
        syllables  (filter #(and (vector? %) (= :SYLLABLE (first %))) (tree-seq vector? rest lyrics-line))
        ]
    (pprint "sargam-section is")
    (pprint sargam-section)
    ;; TODO: finish/rewrite old version
  ;;  sargam-line
    sargam-section
    ))



(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn to-doremi-script-json[parse-tree txt]
  "converts parse tree to json to be compatible with "
  "javascript version of doremi-script"
  "latest attempt"
  (let [
        line-starts 
        (into [0] (keep-indexed #(if (= \newline %2) %1)  txt))
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node))))
        line-map 
        (into {} (map-indexed (fn[a b] [b a]) line-starts)) 
        line-number-for-node (fn[node]
                               (second (last (filter (fn[x] (>= (start-index node) (first x)))
                                                     line-map)) )
                               )
        ]
    (postwalk 
      (fn my-fn[node] 
        (cond
          (not (vector? node))
          node
          true
          (let [k (first node)
                src (get-source node txt) 
                col (column-for-node node)
                my-map (array-map 
                         :source (get-source node txt)
                         :line_number (line-number-for-node node)
                         :column  (column-for-node node) 
                         )
                ]
            (cond
              ;;   { my_type: 'attributes',
              ;;  items: 
              ;; [ { my_type: 'attribute',
              ;; key: 'title',
              ;; value: 'example',
              ;;  source: 'todo' } ],
              ;; source: 'TODO' }  
              ;; [:ATTRIBUTE_SECTION [:ATTRIBUTE_SECTION_ITEMS "title" "hi"]]
              (= k :ATTRIBUTE_SECTION)
              (merge my-map {
                             :my_type "attributes"
                             :items (map (fn[[a b]] {:key a :value b})
                                         (partition 2 (subvec (second node) 1)))
                             } )

              (= k :ZZSARGAM_LINE)
              (merge my-map {
                             :my_type "sargam_line"
                             :items (subvec (second node) 1)
                             } )
              (= k :ZZLYRICS_LINE)
              (merge my-map {
                             :my_type "lyrics_line"
                             :items (subvec (second node) 1)
                             } )
              (= k :SARGAM_SECTION)
              (collapse-sargam-section node)
              (= k :COMPOSITION)
              (let [ atts 
                    (or (some (fn[x] (if (and (map? x) 
                                              (= "attributes" (:my_type x)))
                                       x))
                              (rest (second node))) {:my_type "attributes" :items {} :source ""})
                    ]
                (merge my-map {
                               :my_type "composition"
                               :items (subvec (second node) 1)
                               :attributes atts
                               } ))
              (= k :MEASURE)
              (let [ 
                    obj
                    (merge my-map {
                                   :my_type "measure"
                                   :items (subvec (second node) 1)
                                   } )]
                (assoc obj :beat_count
                       (count (filter (fn[x] (and (map? x) (#{"beat"} (:my_type x)))) (tree-seq map? :items obj)))
                       )
                )
              (= k :BEAT)
              (let [ 
                    obj
                    (merge {
                            :my_type "beat"
                            :items (subvec (second node) 1)
                            } my-map)]
                (assoc obj :divisions 
                       (count (filter (fn[x] (and (map? x) (#{"pitch" "dash"} (:my_type x)))) (tree-seq map? :items obj)))
                       )
                )
              (= k :ZATTRIBUTE_SECTION)
              (process-attribute-section node)
              (= k :PITCH_WITH_DASHES)
              (let [
                    items  (subvec node 1)
                    ]
                (merge {
                        :my_type "pitch_with_dashes"
                        :items (assoc-in items [0 :numerator] (count items))
                        } my-map ))
              (= k :SARGAM_PITCH)
              (let [
                    sarg (first (second (second node)))
                    ]
                (merge {
                        :my_type "pitch" 
                        :normalized_pitch
                        (sarg to-normalized-pitch)
                        :attributes {}
                        :pitch_source (sarg sargam-pitch-to-source)
                        } 
                       my-map
                       ))
              (= k :DASH)
              (merge my-map { :my_type "dash"}  )
              true
              node
              ))))
      (fix-items-keywords parse-tree))))

(defn main5[txt]
  (-> txt
      run-through-parser
      fix-items-keywords ))

;; (pprint (main4 "title:hello\n\nSRG-"))
(pprint (main5 "S\nHi"))
