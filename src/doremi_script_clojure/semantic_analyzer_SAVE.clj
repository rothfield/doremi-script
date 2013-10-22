(ns doremi_script_clojure.semantic-analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk postwalk-replace keywordize-keys]]
    [instaparse.core :as insta]
    ))


(defn slurp-fixture [file-name]
  (slurp (clojure.java.io/resource 
           (str "resources/fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))


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

(defn- unused-update-sargam-ornament-pitch-node [sargam-ornament-pitch nodes]
  "UNFINISHED"
  " TODO: Apply the upper and lower dots found in the upper lines.
  Dots will be in same column. If positioned afterward, becomes lower dot.
  If positioned before, becomes upper dot
  If there is a sargam pitch in this column then things get more complicated. 
  For now, don't apply dots that are 'under' the ornament pitch if there is a
  sargam pitch there.
  "
  (let [
        content (rest sargam-ornament-pitch)
        has-sargam-pitch (first  (filter #(and (vector? %) (=(first %) :SARGAM_PITCH)) nodes))
        upper-dots (if has-sargam-pitch
                     ()  ;; return no dots if has sargam-pitch
                     (filter #(and (vector? %) (=(first %) :UPPER_OCTAVE_DOT)) nodes)
                     )

        ]
    sargam-ornament-pitch
    ))

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

(defn unusedtransform-sargam-pitch1 [sargam-pitch-content]
  "TODO: maybe- do this"
  "Initial transformation, make second entry a hash and add 
  normalized pitch"
  (into [] (concat [:SARGAM_PITCH]
                   {
                    :pitch_source (second sargam-pitch-content)
                    :source (second sargam-pitch-content)
                    :normalized_pitch (to-normalized-pitch (first sargam-pitch-content))
                    }))
  )



(defn transform-beat [beat]
  "Add divisions attribute to beat"
  (conj beat [:divisions 
              99]))
;;;(divisions-in-beat beat)]))

(defn- update-sargam-pitch-node [sargam-pitch nodes]
  (comment [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:R]]])
  " Use the list of nodes to update a sargam-pitch.
  Returns the new node."
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
        sarg (first (second (second sargam-pitch)))
        ]
    ; (pprint z)
    ;; Note that we support multiple syllables, ornaments, and chords per note. But only return the last (for now)
    ;;   (pprint "in update-sargam-pitch-node")
    ;;  (pprint "sargam-pitch is")
    ;; (pprint sargam-pitch)
    (array-map 
      :my_type "pitch"
      :normalized_pitch
      (sarg to-normalized-pitch)
      :attributes {}
      :pitch_source (sarg sargam-pitch-to-source)
      :source (sarg sargam-pitch-to-source)
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
      ) 
    ))

(defn update-sargam-line-node[sargam-line nodes]
  "add syllables attribute to sargam line"
  (conj sargam-line [ :syllables  
                     (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
                     ])) 

"parse and run through semantic analyzer. Txt is doremi-script"
;;(let [
;;x1 ( (comp assign-attributes doremi-script-parser) txt)
;; parse-tree2
;;   (insta/transform {:SARGAM_PITCH transform-sargam-pitch1} parse-tree)
;; z (my-raise parse-tree2)
;;  x1 (assign-attributes parse-tree)
;;sargam-sections (filter #(= :SARGAM_SECTION (first %))  (rest x1))
;; attribute-sections (filter #(= :ATTRIBUTE_SECTION (first %))  (rest x1))
;; lyrics-sections  (filter #(= :LYRICS_SECTION (first %))  (rest x1))
;;lines  (map  extract_sargam_line_from_sargam_section sargam-sections)
;;     ]
; { :attributes attribute-sections
;;  :lines1 sargam-sections
;  :lines lines
;  :lyrics lyrics-sections
;  :source txt
;  }
; x1

;;(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nThese are all the lyrics\nSecond lyric line\n\nRG.\nS | R | G\nhe-llo\n\n |m P D - |\n\nMore:attributes"))
;; (pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (doremi-script-parser (slurp-fixture "yesterday.txt")))


;;(doremi-script-parser "S--R--" :start :BEAT)


;;(pprint (doremi-script-parser "S-RG"))

(def z (doremi-script-parser "title:Hello\nAuthor: John\n\nS\n\nR"))


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
        line-starts (map start-index sargam-section-content)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node))))
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



(defn fix-items-keywords[parse-tree]
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
  (assoc x 1 (keywordize-keys (apply array-map (rest (second x))))))
(comment
  "TODO - convert output of parser to map ???"
  "walk should do it"
  [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]
  )
(defn normalize-pitch[sargam-pitch]
  "sargam-pitch looks like"
  "[:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]"
 ;; (println "entering normalize-pitch")
  ;;(pprint sargam-pitch)
  ;;  (conj sargam-pitch [:normalized-pitch (])
  sargam-pitch
  ;;   (conj x [:normalized_pitch (
  )
(defn unused-my-transforms[parse-tree]
  (let [my-fn
        (fn[x]
          (cond
            (not (vector? x))
            x
            (= :ATTRIBUTE_SECTION (first x))
            (process-attribute-section x)
            (= :SARGAM_SECTION (first x))
            (collapse-sargam-section x)
            ;; (= :SARGAM_PITCH (first x))
            ;;(normalize-pitch x)
            true
            x))
        ]
    (postwalk my-fn parse-tree))) 


(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )

(defn add-source[parse-tree txt]
  (conj parse-tree [:source txt])
  )


(defn run-through-parser[txt]
  (doremi-script-parser txt))


(defn main-aux[txt]
  (->  txt 
      run-through-parser
      (add-source txt) 
      fix-items-keywords 
      unused-my-transforms 
      ))

(defn main[txt]
  (main-aux (clojure.string/trim (rationalize-new-lines txt))))


(defn get-source[node txt]
  (if  (instaparse.core/span node)
    (apply subs txt (instaparse.core/span node))
    node
    ))
;;(pprint (main "  He-llo this is john\n\nS\nhe-llo\n\nMore ly-rics\n\nG  "))
(defn add-source-to-nodes[parse-tree txt]
  (let [my-fn
        (fn[node]
          ;;(println "node is " node)
          (cond 
            (and (vector? node) (instaparse.core/span node))
            (conj node [:source (apply subs txt (instaparse.core/span node))])
            true
            node
            ))] 
    ;;(println "add-source-to-nodes")
    ;;(println "txt is ")
    ;;(println txt)
    (postwalk my-fn parse-tree)))




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

(defn- unused-update-sargam-ornament-pitch-node [sargam-ornament-pitch nodes]
  "UNFINISHED"
  " TODO: Apply the upper and lower dots found in the upper lines.
  Dots will be in same column. If positioned afterward, becomes lower dot.
  If positioned before, becomes upper dot
  If there is a sargam pitch in this column then things get more complicated. 
  For now, don't apply dots that are 'under' the ornament pitch if there is a
  sargam pitch there.
  "
  (let [
        content (rest sargam-ornament-pitch)
        has-sargam-pitch (first  (filter #(and (vector? %) (=(first %) :SARGAM_PITCH)) nodes))
        upper-dots (if has-sargam-pitch
                     ()  ;; return no dots if has sargam-pitch
                     (filter #(and (vector? %) (=(first %) :UPPER_OCTAVE_DOT)) nodes)
                     )

        ]
    sargam-ornament-pitch
    ))

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

(defn unusedtransform-sargam-pitch1 [sargam-pitch-content]
  "TODO: maybe- do this"
  "Initial transformation, make second entry a hash and add 
  normalized pitch"
  (into [] (concat [:SARGAM_PITCH]
                   {
                    :pitch_source (second sargam-pitch-content)
                    :source (second sargam-pitch-content)
                    :normalized_pitch (to-normalized-pitch (first sargam-pitch-content))
                    }))
  )


(defn divisions-in-beat[beat]
  "for example SRG has 3 divisions. S--- has 4   - has one"
  (count (filter #(and (vector? %) (unit-of-rhythm (first %))) 
                 (tree-seq vector? rest beat))))

(defn transform-beat [beat]
  "Add divisions attribute to beat"
  (conj beat [:divisions 
              (divisions-in-beat beat)]))

(defn- update-sargam-pitch-node [sargam-pitch nodes]
  (comment [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:R]]])
  " Use the list of nodes to update a sargam-pitch.
  Returns the new node."
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
        sarg (first (second (second sargam-pitch)))
        ]
    ; (pprint z)
    ;; Note that we support multiple syllables, ornaments, and chords per note. But only return the last (for now)
    ;;   (pprint "in update-sargam-pitch-node")
    ;;  (pprint "sargam-pitch is")
    ;; (pprint sargam-pitch)
    (array-map 
      :my_type "pitch"
      :normalized_pitch
      (sarg to-normalized-pitch)
      :attributes {}
      :pitch_source (sarg sargam-pitch-to-source)
      :source (sarg sargam-pitch-to-source)
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
      ) 
    ))

(defn update-sargam-line-node[sargam-line nodes]
  "add syllables attribute to sargam line"
  (conj sargam-line [ :syllables  
                     (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
                     ])) 

"parse and run through semantic analyzer. Txt is doremi-script"
;;(let [
;;x1 ( (comp assign-attributes doremi-script-parser) txt)
;; parse-tree2
;;   (insta/transform {:SARGAM_PITCH transform-sargam-pitch1} parse-tree)
;; z (my-raise parse-tree2)
;;  x1 (assign-attributes parse-tree)
;;sargam-sections (filter #(= :SARGAM_SECTION (first %))  (rest x1))
;; attribute-sections (filter #(= :ATTRIBUTE_SECTION (first %))  (rest x1))
;; lyrics-sections  (filter #(= :LYRICS_SECTION (first %))  (rest x1))
;;lines  (map  extract_sargam_line_from_sargam_section sargam-sections)
;;     ]
; { :attributes attribute-sections
;;  :lines1 sargam-sections
;  :lines lines
;  :lyrics lyrics-sections
;  :source txt
;  }
; x1

;;(pprint (parse2 "Author: John Rothfield\nTitle: untitled\n\nThese are all the lyrics\nSecond lyric line\n\nRG.\nS | R | G\nhe-llo\n\n |m P D - |\n\nMore:attributes"))
;; (pprint (parse2 (slurp-fixture "yesterday.txt")))
;; (pprint (doremi-script-parser (slurp-fixture "yesterday.txt")))


;;(doremi-script-parser "S--R--" :start :BEAT)


;;(pprint (doremi-script-parser "S-RG"))

(def z (doremi-script-parser "title:Hello\nAuthor: John\n\nS\n\nR"))


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
        line-starts (map start-index sargam-section-content)
        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (start-index node) (line-start-for (start-index node))))
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



(defn fix-items-keywords[parse-tree]
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
  (assoc x 1 (keywordize-keys (apply array-map (rest (second x))))))
(comment
  "TODO - convert output of parser to map ???"
  "walk should do it"
  [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]
  )
(defn normalize-pitch[sargam-pitch]
  "sargam-pitch looks like"
  "[:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]"
  (println "entering normalize-pitch")
  (pprint sargam-pitch)
  ;;  (conj sargam-pitch [:normalized-pitch (])
  sargam-pitch
  ;;   (conj x [:normalized_pitch (
  )
(defn unused-my-transforms[parse-tree]
  (let [my-fn
        (fn[x]
          (cond
            (not (vector? x))
            x
            (= :ATTRIBUTE_SECTION (first x))
            (process-attribute-section x)
            (= :SARGAM_SECTION (first x))
            (collapse-sargam-section x)
            ;; (= :SARGAM_PITCH (first x))
            ;;(normalize-pitch x)
            true
            x))
        ]
    (postwalk my-fn parse-tree))) 


(defn rationalize-new-lines[txt]
  (clojure.string/replace txt #"\r\n" "\n")
  )

(defn add-source[parse-tree txt]
  (conj parse-tree [:source txt])
  )


(defn run-through-parser[txt]
  (doremi-script-parser txt))


(defn main-aux[txt]
  (->  txt 
      run-through-parser
      (add-source txt) 
      fix-items-keywords 
      unused-my-transforms 
      ))

(defn main[txt]
  (main-aux (clojure.string/trim (rationalize-new-lines txt))))

;;(pprint (main "  He-llo this is john\n\nS\nhe-llo\n\nMore ly-rics\n\nG  "))
;;    (postwalk my-fn parse-tree))) 


(defn main3[txt]
  (->  txt 
      run-through-parser
      (add-source txt) 
      ;;(add-source-to-nodes txt)
      fix-items-keywords 
      (my-transforms2 txt) 
      ))

(defn to-doremi-script-json[parse-tree txt]
  "converts parse tree to json to be compatible with "
  "javascript version of doremi-script"
  "latest attempt"
  (println "txt is " txt)
  (let [

        line-starts 
        (into [0] (keep-indexed #(if (= \newline %2) %1)  txt))

        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          ;(println line-starts "node is " node "start-index" (start-index node))
                          (- (start-index node) (line-start-for (start-index node))))
        line-map 
        (into {} (map-indexed (fn[a b] [b a]) line-starts)) 
        line-number-for-node (fn[node]
                               (second (last (filter (fn[x] (>= (start-index node) (first x)))
                                                     line-map)) )
                               )
        ]
   ;; (pprint line-map)
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

              (= k :COMPOSITION)
            (let [ atts 
                  (or (some (fn[x] (if (and (map? x) 
                                            (= "attributes" (:my_type x)))
                                     x))
                            (rest (second node))) {:my_type "attributes" :items {} :source ""})
                  ]
            ;;  (println "^^^^^^^^^^^^^^")
             ;; (pprint node)
              (pprint atts)
              ;; (println "^^^^^^^^^^^^^^")
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
                (println "sargam pitch********")
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

(defn main4[txt]
  (-> txt
      run-through-parser
      (to-doremi-script-json txt)))


(defn zzto-doremi-script-json[parse-tree txt]
  "converts parse tree to json to be compatible with "
  "javascript version of doremi-script"
  "latest attempt"
  (println "txt is " txt)
  (let [

        line-starts 
        (into [0] (keep-indexed #(if (= \newline %2) %1)  txt))

        line-start-for  (fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          ;(println line-starts "node is " node "start-index" (start-index node))
                          (- (start-index node) (line-start-for (start-index node))))
        line-map 
        (into {} (map-indexed (fn[a b] [b a]) line-starts)) 
        line-number-for-node (fn[node]
                               (second (last (filter (fn[x] (>= (start-index node) (first x)))
                                                     line-map)) )
                               )
        ]
   ;; (pprint line-map)
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
              (= k :ATTRIBUTE_SECTION)
              (merge my-map {
                       :my_type "attributes"
                       :items (subvec (second node) 1)
                       } )

              (= k :COMPOSITION)
            (let [ atts 
                  (or (some (fn[x] (if (and (map? x) 
                                            (= "attributes" (:my_type x)))
                                     (second x))) 
                            (rest (second node))) {})
                  ]
            ;;  (println "^^^^^^^^^^^^^^")
             ;; (pprint node)
              ;; (println "^^^^^^^^^^^^^^")
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
                (println "sargam pitch********")
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


(defn main4[txt]
  (-> txt
      run-through-parser
      (to-doremi-script-json txt)
      )
  )



(defn my-transforms2[parse-tree txt]
  (let [my-fn
        (fn[x]
          (cond
            (not (vector? x))
            x
            (= :COMPOSITION (first x))
            (let [ atts 
                  (or (some (fn[x] (if (and (sequential? x) 
                                            (= :ATTRIBUTE_SECTION (first x)))
                                     (second x))) 
                            (rest (second x))) {})


                  ]
              (array-map
                :my_type "composition"  
                :attributes atts 
                :lines (into [] 
                             (filter (fn[x] (and (sequential? x) (= :SARGAM_LINE (first x))))
                                     (rest (second x))))
                ) 
              )
            (= :ATTRIBUTE_SECTION (first x))
            (process-attribute-section x)
            (= :SARGAM_SECTION (first x))
            (collapse-sargam-section x)
            (= :DASH (first x))
            (with-meta (array-map :my_type "dash" 
                                  :source (get-source x txt)
                                  ) (meta x))
            ;; (= :SARGAM_PITCH (first x))
            ;; (normalize-pitch x)
            true
            x))
        ]
    (postwalk my-fn parse-tree))) 
;; (def z "hi:john\n\nS\n\nR")
(def z "title: hi\n\n+\n | S- S -- - \nhi\n\nR")
(def zz  (doremi-script-parser z))
;;(pprint (main3 (doremi-script-parser z)))
(pprint (main4 z))
;;(pprint (my-transforms2 (doremi-script-parser z)))
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


