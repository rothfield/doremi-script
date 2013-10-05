(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk-demo postwalk postwalk-replace keywordize-keys]]
    [clojure.string :refer [lower-case]]
    ))


;; controlling scope:
;; srruby: defn- is just defn + ^:private, which can be put on any def
;;  vars are public ("exported") by default; putting ^:private on the var's name prevents this (but does not make it truly inaccessible.)


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


(defn- start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. instaparse adds the metadata in the parsing process"
  "Returns the character position in the source code where the object starts"
  (:instaparse.gll/start-index (meta z))
  )

(def unit-of-rhythm #{:pitch :dash} )

(def sargam-pitch-to-source
  (array-map
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
    :Sb "Sb" 
    :Ssharp "S#"
    :Rsharp "R#" 
    :Gsharp "G#" 
    :Psharp "P#" 
    :Dsharp "D#" 
    :Nsharp "N#" 
    :Pb "Pb" 
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
   :Sb "Cb" 
   :Ssharp "C#"
   :Rsharp "R#" 
   :Gsharp "E#" 
   :Psharp "G#" 
   :Pb "Gb" 
   :Dsharp "A#" 
   :Nsharp "B#" 
   })


(defn- my-seq[x]
  "seq through the data structure, which is like"
  " {:items [ {:items [1 2 3]} 2 3]}"
  "Don't include items like {:items [1 2 3]} "
  "just [1 2 3]"
  (filter #(not (:items %))
          (tree-seq
            (fn branch?[x] (or (vector? x) (map? x))) 
            (fn children[y] 
              (cond 
                (and (map? y) (:items y)) 
                (:items y)
                (vector? y)
                (rest y)))
            x)))

(defn- line-column-map [my-map line]
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


(defn- get-source[node txt]
  (let [ s (:instaparse.gll/start-index (meta node))
        e (:instaparse.gll/end-index (meta node))]
    (if (and s e)
      (subs txt s e))))

(defn- update-sargam-pitch-node [pitch nodes]
  (let [
        upper-dots (count (filter #(= (:_my_type %) :upper_octave_dot) nodes))
        lower-dots (count (filter #(= (:_my_type %) :lower_octave_dot) nodes))
        upper-upper-dots (count (filter #(= (:_my_type %) :upper_upper_octave_symbol) nodes))
        lower-lower-dots (count (filter #(= (:_my_type %) :lower_lower_octave_symbol) nodes))
        octave (+ upper-dots (- lower-dots) (* -2 lower-lower-dots) (* 2 upper-upper-dots))
        ]
    (merge pitch 
           {
            :attributes 
            (into [] (concat (:attributes pitch) 
                             (filter #(#{:begin_slur 
                                         :end_slur
                                         :chord_symbol
                                         :ending
                                         :mordent} (:_my_type %)) nodes)))
            :octave octave
            :syllable (some #(if (= (:_my_type %) :syllable)  (:_source %)) nodes)
            :chord (some #(if (= (:_my_type %) :chord_symbol)  (:_source %)) nodes)
            :ornament nil 
            :tala (some #(if (= (:_my_type %) :tala)  (:_source %)) nodes)
            }
           )))

(defn- collapse-sargam-section [sargam-section txt]
  ;;  (pprint sargam-section) (println "************^^^^")
  "main logic related to lining up columns is here"
  "Deals with the white-space significant aspect of doremi-script"
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
                      (let [ my-type (:_my_type node)
                            column (if my-type (column-for-node node))
                            nodes (if my-type (get column-map column)) 
                            ]
                        (case my-type
                          :pitch
                          (update-sargam-pitch-node node nodes)
                          ;; default case
                          node)))
        ]
    (assert (= :sargam_line (:_my_type sargam-line)))
    (assert (map? column-map))
    (postwalk postwalk-fn sargam-section)
    ))

(defn- make-sorted-map[node]
  (cond 
    (and (map? node) (= (into (hash-set) (keys node)) #{:numerator :denominator}))
    node
    (map? node)
    (into (sorted-map) node)
    true
    node))

(defn- make-maps-sorted[x]
  (into (sorted-map) (postwalk make-sorted-map x)))

(defn- backwards-comparator[k1 k2]
  (compare k2 k1))




;; Here is a good place to handle ties/dashes/rests
;; Number the significant pitches and dashes in this line, starting with 0
;; NEEDS WORK
;; Given S- -R
;; we want to tie the dash at beginning of beat 2 to S
;; In general, a dash at the beginning of a beat will always be tied to the previous dash or
;; pitch, except if the very first beat starts with a dash
;;  S- -- --  
;;  |  |  |    

;; Notes on dash, dashes handling
;jjjjjjjjjjjjjjjj;
;; Rule 1: Dashes at beginning of beat should be tied to previous note in current line
;; the previous pitch should be marked :tied => true
;; The dash should be marked
;; dash_to_tie: true,
;; pitch_to_use_for_tie: <pitch> 
;;
;;
;;
;; example 1:  S---
;; dashes will look like
;; { my_type: 'dash', source: '-' } { my_type: 'dash', source: '-' }
;;
;; example 2: -S
;; In this case the dash isn't tied to any previous note
;; dashes will look like
;; { my_type: 'dash',numerator: 1, denominator: 2, dash_to_tie: false,rest: true }
;; example 3:
;;  S --t
;                  [ { my_type: 'dash',
;                      source: '-',
;                      numerator: 2,
;                      denominator: 3,
;                      dash_to_tie: true,
;                      pitch_to_use_for_tie: 
;                       { my_type: 'pitch',
;                         normalized_pitch: 'C',
;                         attributes: [],
;                         pitch_source: 'S',
;                         source: 'S',
;                         column_offset: 0,
;                         octave: 0,
;                         numerator: 1,
;                         denominator: 1,
;                         tied: true,
;                         fraction_array: 
;                          [ { numerator: 1, denominator: 1 },
;                            { numerator: 2, denominator: 3 } ],
;                         fraction_total: { numerator: 5, denominator: 3 },
;                         column: 0 },
;                      column: 2 },
;                    { my_type: 'dash', source: '-', column: 3 },
;;
(defn- handle-sargam-line-in-main-walk[line]
  (let [
        pitch-counter (atom -1)
        significant? (fn significant2[x]
                       "don't number dashes such as the last 2 of S---"
                       (and (map? x) (#{:pitch :dash} (:_my_type x))
                            (not (:ignore x))))
        line2 (postwalk (fn add-pitch-counters[z] 
                          (if-not (significant? z)
                            z
                            (do
                              (swap! pitch-counter inc)
                              (assoc z :pitch-counter @pitch-counter))))
                        line)

        pitches (into []  (filter 
                            significant? (my-seq line2) ))
        line3 (postwalk (fn line3-postwalk[node-in-line]
                          (case (:_my_type node-in-line)
                            :beat
                            (let [ 
                                  ;; z1  (println "**z ===> " z)
                                  beat-counter (atom -1)
                                  pitches-in-beat 
                                  (into []  (filter 
                                              significant? (my-seq node-in-line) ))
                                  ]
                              (postwalk (fn[a] (if-not (significant? a)
                                                 a
                                                 ;; else
                                                 (do
                                                   (swap! beat-counter inc)
                                                   (assoc a :beat-counter @beat-counter))
                                                 ))  node-in-line)
                              )
                            ;; default
                            node-in-line)) line2)]

    (postwalk (fn walk-line-tieing-dashes-and-pitches[node-in-line] 
                "if item is dash at beginning of line, set :dash_to_tie false and :rest true
                if item is dash (not at beginning of line) and line starts with a pitch"
                (if-not (significant? node-in-line)
                  node-in-line
                  ;; else
                  (let
                    [
                     ;;xyz (pprint node-in-line)
                     my-key (:_my_type node-in-line) 
                     prev-item (last (filter #(and (significant? %)
                                                   (< (:pitch-counter %) (:pitch-counter node-in-line))) pitches))
                     next-item  (first (filter #(and (significant? %)
                                                     (> 
                                                       (:pitch-counter %)
                                                       (:pitch-counter node-in-line))) 
                                               pitches))
                     ]
                    (cond (and (= my-key :dash)  ;; dash is first item in line
                            (not prev-item))  ;; No previous item 
                          (do
                            (assoc node-in-line              ;; it becomes a rest.
                                   :dash_to_tie false
                                   :rest true ))
                          (and (= :pitch my-key)   ;; pitch and next item is a dash. 
                               (not (nil? next-item))
                               (= :dash (:_my_type next-item)))
                          (do ;(println "next-item is" next-item)
                          (assoc node-in-line :tied true))   ;; tie to next dash
                          (and (= :dash my-key)    ;; dash at beginning of beat
                               (= 0 (:beat-counter node-in-line)))
                          (let [prev-pitch         ;; previous pitch in this line 
                                (last (filter #(and (= :pitch (:_my_type %))
                                                    (< 
                                                      (:pitch-counter %)
                                                      (:pitch-counter node-in-line)))
                                              pitches))
                                ]
                            ;;  (assert prev-pitch)
                            (assoc node-in-line 
                                   :dash_to_tie true
                                   :tied (and next-item (= :dash (:_my_type next-item))) ;; #TODO
                                   :pitch_to_use_for_tie prev-pitch))
                          (= :pitch my-key) 
                          (let []
                            ;;(println "pitch case")
                            ;;(println "prev-pitch" prev-item)
                            (assoc node-in-line :tied true)
                            )
                          true
                          node-in-line
                          )
                    )
                  ))
              line3)
    ))

(defn- handle-beat-in-main-walk[ node2]
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
    (postwalk (fn postwalk-in-beat[node-in-beat] 
                (if (and (#{:pitch :dash} (:_my_type node-in-beat))
                         (not (:ignore node-in-beat))
                         (:numerator node-in-beat)  ;; probably don't need this
                         )
                  (let [my-ratio (/ (:numerator node-in-beat) subdivisions)
                        frac 
                        (if (= (class my-ratio) java.lang.Long)
                          (sorted-map-by backwards-comparator  :numerator 1 
                                         :denominator 1) 
                          ;; else 
                          (sorted-map-by  backwards-comparator 
                                         :numerator (numerator my-ratio)
                                         :denominator (denominator my-ratio)))
                        ]
                    (assoc node-in-beat 
                           :denominator subdivisions
                           :fraction_array 
                           [ frac ]))
                  ;; else
                  node-in-beat 
                  )
                )
              my-beat)))

(defn- handle-pitch-with-dashes-in-main-walk[[my-key pitch & rest]]
  "Handle  S--  and  ---"
  ;; set :ignore true for all the dashes
  ;;  and set numerator for pitch
  (let [micro-beats (inc (count (filter #(= :dash (:_my_type %)) rest)))]
    (assert (#{:PITCH_WITH_DASHES :DASHES} my-key))
    (into [] (concat [ (assoc pitch
                              :numerator micro-beats)] 
                     (map (fn[x] (if (= :dash (:_my_type x))
                                   (assoc x :ignore true)
                                   ;; else
                                   x)) rest)))))

(defn- handle-composition-in-main-walk[node2]
  (let [ sections 
        (filter #(= :sargam_section (:_my_type %))  (:items node2))
        lines
        (into [] (map  (fn[x] (some #(if (= :sargam_line (:_my_type %)) %) 
                                    (:items x))) sections))
        ] 
    (merge (dissoc node2 :items) {:lines  lines 
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
                                  })))


(defn- main-walk[node txt]
  (if-not (vector? node) node
    ;; else
    (let [
          my-key (first node)
          my-map (array-map :_my_type (keyword (lower-case (name my-key)))
                            :_source (get-source node txt)
                            :_start_index (start-index node) 
                            )
          node2 (if (and (vector? (second node)) 
                         (keyword? (first (second node)))
                         (.endsWith (name (first (second node))) "ITEMS"))
                  (merge {:items (subvec  (second node) 1) } my-map)
                  ;; else
                  node)
          ]
      (case my-key 
        :TALA 
        my-map
        :CHORD_SYMBOL
        my-map
        :BEGIN_SLUR_SARGAM_PITCH
        (let [
              [_ begin-slur my-pitch2] node
              my-pitch (merge my-pitch2
                              {:column_offset 1
                               :_source (:_source my-map)
                               })
              ]
          ;; add begin slur to attributes
          (assoc my-pitch 
                 :attributes
                 (conj (into [] (:attributes my-pitch)) begin-slur)))
        :UPPER_OCTAVE_LINE
        (merge  my-map (array-map :items (subvec node 1)))
        :SYLLABLE
        my-map 
        :COMPOSITION 
        (handle-composition-in-main-walk node2)
        :PITCH_WITH_DASHES
        (handle-pitch-with-dashes-in-main-walk node)
        :DASHES
        (handle-pitch-with-dashes-in-main-walk node)
        :BEAT
        (handle-beat-in-main-walk node2)
        :MORDENT
        my-map
        :UPPER_UPPER_OCTAVE_SYMBOL
        my-map
        :LOWER_OCTAVE_DOT
        my-map
        :LOWER_LOWER_OCTAVE_SYMBOL
        my-map
        :UPPER_OCTAVE_DOT
        my-map 
        :LINE_NUMBER  
        my-map
        :BEGIN_SLUR 
        my-map
        :END_SLUR 
        my-map
        :DASH
        my-map
        :BARLINE
        (merge  my-map 
               (sorted-map 
                 :_my_type 
                 (keyword (keyword (lower-case (name (get-in node [1 0])))))
                 :is_barline true))
        :SARGAM_LINE
        (handle-sargam-line-in-main-walk node2)
        :SARGAM_SECTION
        (let [collapsed
              (collapse-sargam-section 
                (merge (sorted-map :items (subvec node 1)) my-map)
                txt)]
          collapsed
          )
        :SARGAM_PITCH
        (let [
              sarg  (some #(if (= (first %) :SARGAM_MUSICAL_CHAR) (first (second %))) (rest node))
              ]
          (merge 
            my-map
            (sorted-map  
              :_my_type :pitch
              :numerator 1  ;;; numerator and denominator may get updated later!
              :denominator 1
              :column_offset 0  ;; may get updated
              :normalized_pitch (sarg to-normalized-pitch)
              :pitch_source (sarg sargam-pitch-to-source)
              )))
        ;; default
        node2
        )
      ))) ;; end main-walk



(defn- my-seq2[x]
  (tree-seq (fn branch?[node]
              true)
            (fn children[node]
              (cond 
                (and (map? node) (:items node))
                (:items node)
                (and (map? node) (:lines node))
                (:lines node)
                (vector? node)
                identity))
            x))

(defn transform-parse-tree[parse-tree txt]
  "Transform parse-tree into doremi-script json style format"
  (make-maps-sorted (postwalk 
                      (fn[node] (main-walk node txt)) 
                      parse-tree)))


