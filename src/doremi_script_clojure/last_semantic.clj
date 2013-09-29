(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk-demo postwalk postwalk-replace keywordize-keys]]
    [clojure.string :refer [lower-case]]
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


(defn start-index[z]
  "Looks up starting index of the node from the node's 
  metadata. instaparse adds the metadata in the parsing process"
  "Returns the character position in the source code where the object starts"
   (:instaparse.gll/start-index (meta z))
  )

(def unit-of-rhythm
  #{ :pitch  :dash}
  )

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


(defn my-seq[x]
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


(defn get-source[node txt]
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
    (pprint nodes)
    (merge pitch 
           {
            ;; TODO: review which nodes gets added to attributes
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

(defn collapse-sargam-section [sargam-section txt]
  ;;  (pprint sargam-section) (println "************^^^^")
  "main logic related to lining up columns is here"
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
  (if false (do 
              (println "entering collapse-sargam-section, sargam-section is")
              (pprint sargam-section)))

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
                              ]
                          (cond
                            (= my-type :pitch)
                            (update-sargam-pitch-node node nodes)
                            true
                            node))))
        ]
    (assert (= :sargam_line (:_my_type sargam-line)))
    (assert (map? column-map))
    (postwalk postwalk-fn sargam-section)
    ))
(defn make-sorted-map[node]
  (cond 
    ;;    true
    ;;   node
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

;; Notes on dash, dashes handling
;;
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
;;  S --R
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
(defn handle-sargam-line-in-main-walk[line]
  (let [
        pitch-counter (atom -1)
        significant? (fn significant2[x]
                       "don't number dashes such as the last 2 of S---"
                       (and (map? x) (#{:pitch :dash} (:_my_type x))
                            (not (:ignore x))))
        line2 (postwalk (fn add-pitch-counters[z] (cond
                                                    (not (significant? z))
                                                    z
                                                    true
                                                    (do
                                                      (swap! pitch-counter inc)
                                                      (assoc z :pitch-counter @pitch-counter))))
                        line)
        pitches (into []  (filter 
                            significant? (my-seq line2) ))
        line3 (postwalk (fn line3-postwalk[z]
                          ;;  (println "**z ===> " z)
                          (cond  
                            (= :beat (:_my_type z))
                            (let [ 
                                  ;; z1  (println "**z ===> " z)
                                  beat-counter (atom -1)
                                  pitches-in-beat (into []  (filter 
                                                              significant? (my-seq z) ))
                                  ]
                              (postwalk (fn[a]
                                          (cond (significant? a)
                                                (do
                                                  (swap! beat-counter inc)
                                                  (assoc a :beat-counter @beat-counter))
                                                true
                                                a 
                                                ))  z)
                              ;;(println "beat case")
                              ;;(println "d is" pitches-in-beat)
                              )
                            true
                            z)) line2)]

    (postwalk (fn walk-line-tieing-dashes-and-pitches[node-in-line] 
                "if item is dash at beginning of line, set :dash_to_tie false and :rest true
                if item is dash (not at beginning of line) and line starts with a pitch"
                ;; (println "walk-line-tieing-dashes-and-pitches")
                ;; (println "node-in-line -->") (pprint node-in-line)
                ;;; (println "***node-in-line, significant?" node-in-line (significant? node-in-line))
                (cond 
                  (not (significant? node-in-line))
                  node-in-line
                  true
                  (let
                    [
                     ;;xyz (pprint node-in-line)
                     my-key (:_my_type node-in-line) 
                     prev-item (last (filter #(and (significant? %)
                                                   (< (:pitch-counter %) (:pitch-counter node-in-line))) pitches))
                     next-item  (first (filter #(and (significant? %)
                                                     (> (:pitch-counter node-in-line) (:pitch-counter %))) pitches))
                     ]
                    (cond (and (= 0 @pitch-counter)
                               (= my-key :dash))  ;; dash is first item in line
                          (do
                            (assoc node-in-line              ;; it becomes a rest.
                                   :dash_to_tie false
                                   :rest true ))
                          (and (= :pitch my-key)   ;; pitch and next item is a dash. 
                               (not (nil? next-item))
                               (= :dash (:_my_type next-item)))
                          (assoc node-in-line :tied true)   ;; tie to next dash
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

(defn handle-beat-in-main-walk[ node2]
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
                ;;(println "postwalk-in-beat -z ---------->")
                ;;(println "subdivisions are " subdivisions)
                ;;(if (= 0 subdivisions)
                ;; (do
                ;; (println "0 subdivisions. my-beat is : ")
                ;; (pprint my-beat)))
                ;;(pprint z)
                ;;(assert (not (= 0 subdivisions)))
                ;;(println "z is")
                ;;(pprint z)
                ;; (pprint "z is")
                ;; (println "postwalk-in-beat -z ---------->")
                ;; (pprint z)
                ;;(println "\n\n")
                (cond 
                  (= :beat (:_my_type z))
                  z
                  ;; (assoc z :items (into [] (apply concat (:items z))))
                  (not (#{:pitch :dash} (:_my_type z)))
                  z
                  (:ignore z)
                  z
                  (not (:numerator z))
                  z
                  true 
                  (do
                    ;;(println "post-walk in beat, z is --->")
                    ;;  (pprint z)
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
                             [ frac ])))))
              my-beat)))
(defn handle-pitch-with-dashes-in-main-walk[[my-key pitch & rest]]
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

(defn main-walk[node txt]
  ;; (pprint node)
  (cond
    (not (vector? node))
    node
    true
    (let [
          my-key (first node)
          ;; zzz (println "main-walk, my-key =>" my-key)
          my-map (array-map :_my_type (keyword (lower-case (name my-key)))
                            :_source (get-source node txt)
                            :_start_index (start-index node) 
                            )
          ;;zz (pprint "&&&&&&&&")
          ;;z (pprint my-key)
          node2 (if (and (vector? (second node)) 
                         (keyword? (first (second node)))
                         (.endsWith (name (first (second node))) "ITEMS"))
                  (merge {:items (subvec  (second node) 1) } my-map)
                  ;; else
                  node)
          ]
      (cond
        (= :TALA my-key)
        my-map
        (= :CHORD_SYMBOL my-key)
        my-map
        (#{:BEGIN_SLUR_SARGAM_PITCH} my-key)
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
        (#{:UPPER_OCTAVE_LINE} my-key)
        (merge  my-map (array-map :items (subvec node 1)))
        (= :SYLLABLE my-key)
        my-map 
        (= :COMPOSITION my-key)
        (let [ sections 
              (filter #(= :sargam_section (:_my_type %))  (:items node2))
              lines
              (into [] (map  (fn[x] (some #(if (= :sargam_line (:_my_type %)) %) 
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
        (#{:PITCH_WITH_DASHES :DASHES} my-key)
        (handle-pitch-with-dashes-in-main-walk node)

        ;; Handles things like S--  ---   
        ;; --
        ;; The first item is significant and will get counted rhythmically
        ;; returns [ pitch dash dash ] or [dash dash dash]
        (= :BEAT my-key)
        (handle-beat-in-main-walk node2)
        (#{:MORDENT :UPPER_UPPER_OCTAVE_SYMBOL :LOWER_OCTAVE_DOT :LOWER_LOWER_OCTAVE_SYMBOL :UPPER_OCTAVE_DOT :LINE_NUMBER :BEGIN_SLUR :END_SLUR} my-key)
        my-map
        (= :DASH my-key)
        (if false (do (println ":DASH case")
                      (pprint my-map))
          ;; TODO: I think not needed now
          my-map)
        ;; (merge my-map (sorted-map :numerator 1))
        (= :BARLINE my-key)
        (merge  my-map (sorted-map :_my_type (keyword (keyword (lower-case (name (get-in node [1 0])))))
                                   :is_barline true))
        (= :SARGAM_LINE my-key)
        (handle-sargam-line-in-main-walk node2)
        ;; Here is a good place to handle ties/dashes/rests
        ;; Number the significant pitches and dashes in this line, starting with 0
        ;; NEEDS WORK
        ;; Given S- -R
        ;; we want to tie the dash at beginning of beat 2 to S
        ;; In general, a dash at the beginning of a beat will always be tied to the previous dash or
        ;; pitch, except if the very first beat starts with a dash
        ;;  S- -- --  
        ;;  |  |  |    
        (= :SARGAM_SECTION my-key)
        (let [collapsed
              (collapse-sargam-section 
                (merge (sorted-map :items (subvec node 1)) my-map)
                txt)]
          ;;(pprint collapsed)
          collapsed
          )
        (= :SARGAM_PITCH my-key)
        (let [
              sarg  (some #(if (= (first %) :SARGAM_MUSICAL_CHAR) (first (second %))) (rest node))
              ]
          ;; (println ":SARGAM_PITCH case")
          ;; (swap! pitch-counter inc)
          (merge 
            my-map
            (sorted-map  
              ;; :_pitch_counter @pitch-counter
              :_my_type :pitch
              :numerator 1  ;;; numerator and denominator may get updated later!
              :denominator 1
              :column_offset 0  ;; may get updated
              :normalized_pitch (sarg to-normalized-pitch)
              :pitch_source (sarg sargam-pitch-to-source)
              )))
        true
        node2
        )))) ;; end main-walk



(defn my-seq2[x]
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


(def t1 "Dm7\nS")
;; (def t2 "S--S --R-")
;;(def t2 "Srgm")
;(def t2 "---S r-")
(def t2 "S--r-g-- -S")
(def t2 "S--r-g-- -S")
(def t2 "---- S--R  --G-")
(def t3 "---- S- -- ----")
(def t3 "S\nHi")
;;\n\n-R\n\nS- -R")
;;(pprint (run-through-parser t2))

;;(pprint (doremi-script-parse t3))
;;(pprint (doremi-script-parse t2))

;;(def z1 (doremi-script-parse t2))
;;(pprint (run-through-parser ":\n*\nS\n.\n:\nHi"))
