(ns doremi_script_clojure.semantic_analyzer
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
  (:require	
    [clojure.pprint :refer [pprint]] 
    [clojure.java.io :refer [input-stream resource]]
    [clojure.set :refer [union]] 
    [clojure.walk :refer [postwalk
                          postwalk-replace keywordize-keys]]
    [clojure.string :refer [split lower-case]]
    ))

;; controlling scope:
;; srruby: defn- is just defn + ^:private, which can be put on any def
;;  vars are public ("exported") by default; putting ^:private on the var's name prevents this (but does not make it truly inaccessible.)


(defn is-barline?[item]
(:is_barline item)
  )

(def debug false)

(defn my-seqable?
  "Returns true if (seq x) will succeed, false otherwise."
  [x]
  (or (seq? x)
      (instance? clojure.lang.Seqable x)
      (nil? x)
      (instance? Iterable x)
      (-> x .getClass .isArray)
      (string? x)
      (instance? java.util.Map x)))

(def upper-dot-set
  #{ :upper_octave_dot :upper_upper_octave_symbol }
  )

(def upper-dot-magnitude
  {:upper_octave_dot 1 :upper_upper_octave_symbol 2})

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

(def sargams-str "S r R g G m M P d D n N Sb mb Pb Ssharp Rsharp Gsharp Psharp Dsharp Nsharp")

(def normalized-str "C Db D Eb E F F# G Ab A Bb B Cb Fb Gb C# D# E# G# A# B#")

(def sargam-symbols
  (into [] (map keyword (split sargams-str #" "))))

(def to-normalized-pitch
  (zipmap sargam-symbols (split normalized-str #" ")))

(def sargam-sources
  (into [] (map #(clojure.string/replace % "sharp" "#")
                (split sargams-str #" "))))

(def sargam-pitch-to-source
  (zipmap sargam-symbols sargam-sources))

(defn- my-seq[x]
  "seq through the data structure, which is like"
  " {:items [ {:items [1 2 3]} 2 3]}"
  "Don't include items like {:items [1 2 3]} "
  "just [1 2 3]"
  ;; TODO: redo
  ;;(filter #(not (:items %))
  (tree-seq
    (fn branch?[x] (or (vector? x) (map? x))) 
    (fn children[y] 
      (cond 
        (and (map? y) (:items y)) 
        (:items y)
        (vector? y)
        (rest y)))
    x))
;)

(defn- line-column-map [my-map line]
  {
   :pre [ (map? line) ]
   :post [ (map? %)
          (every? integer? (keys %))
          (every? #(:my_type %) (flatten (vals %)))
          ] }
  "my-map is a map from column number -> list of nodes in that column
  Add nodes from line to the map"
  (reduce (fn [accumulator node]
            (let [column (- (:start_index node) 
                            (:start_index line))]
              (assoc accumulator
                     column 
                     (conj (get accumulator column) node)
                     )))
          my-map (filter #(:start_index %) (my-seq line))
          )) 


(defn- get-source[node txt]
  {
   :pre [ (string? txt)]
   :post [ (or (nil? %) (string? %))] }
  "Get instaparse source for the node or nil if not available." 
  (let [ s (:instaparse.gll/start-index (meta node))
        e (:instaparse.gll/end-index (meta node))]
    (if (and s e)
      (subs txt s e))))

(defn- update-sargam-pitch-node [pitch nodes syl]
  {
   :pre [
         (#{:pitch} (:my_type pitch))
         (my-seqable? nodes)
         (or (nil? syl) (string? syl))
         ]
   :post [ (#{:pitch} (:my_type %))]
   }
  (if false (do
              (println "entering update-sargam-pitch-node")
              (pprint nodes)))
  (let [
        upper-dots (count (filter #(= (:my_type %) :upper_octave_dot) nodes))
        lower-dots (count (filter #(= (:my_type %) :lower_octave_dot) nodes))
        upper-upper-dots (count (filter #(= (:my_type %) :upper_upper_octave_symbol) nodes))
        lower-lower-dots (count (filter #(= (:my_type %) :lower_lower_octave_symbol) nodes))
        octave (+ upper-dots (- lower-dots) (* -2 lower-lower-dots) (* 2 upper-upper-dots))
        ]
    (merge pitch 
           {
            :attributes 
            (into [] (concat (:attributes pitch) 
                             (filter #(#{:begin_slur 
                                         :end_slur
                                         :chord_symbol
                                         :ornament
                                         :ending
                                         :mordent} (:my_type %)) nodes)))
            :octave octave
            :syllable syl
            ;;;  :syllable (some #(if (= (:my_type %) :syllable)  (:source %)) nodes)
            :chord (some #(if (= (:my_type %) :chord_symbol)  (:source %)) nodes)
            :tala (some #(if (= (:my_type %) :tala)  (:source %)) nodes)
            }
           )))



(defn assign-group-line-numbers-to-ornament-pitches[sargam-section]
  {
   :pre [ (= :sargam_section (:my_type sargam-section)) ]
   :post [ (= :sargam_section (:my_type %)) ] }
  ;; Set group-line-no for upper dots and ornament pitches
  ;; group-line-no is the line number within the sargam-section 
  ;;
  (let [ctr (atom 0)]
    (postwalk (fn[x] 
                (if (= :upper_octave_line (:my_type x)) 
                  (swap! ctr inc))
                (if (or (upper-dot-set (:my_type x))
                        (:in_ornament x))
                  (assoc x :group_line_no @ctr)
                  ;; else
                  x))
              sargam-section))
  )


(defn- collapse-sargam-section [sargam-section txt]
  {
   :pre [ (= :sargam_section (:my_type sargam-section))
         (string? txt) ]
   :post [ (= :sargam_section (:my_type %)) ]
   }
  ;; TODO: refactor. "GOD method" 
  ;; TODO: use atom to manage items removed from column map. And an atom to track unassigned
  ;; items. column-map needs to be mutable.
  ;; (pprint sargam-section) (println "************^^^^")
  "main logic related to lining up columns is here"
  "Deals with the white-space significant aspect of doremi-script"
  "Returns the sargam-line with the associated objects in the same column attached
  to the corresponding pitches/items on main line. The dot becomes an upper octave of S and 
  Hi becomes a syllable for S"
  "Assign attributes to the main line(sargam_line) from the lower and upper lines using 
  column numbers. Returns a sargam-line"
  (assert (= (:my_type sargam-section) :sargam_section))
  (assert (string? txt))
  (let [
        ;; _ (println "sargam-section")
        ;; _ (pprint sargam-section)
        sargam-section1 (assign-group-line-numbers-to-ornament-pitches sargam-section)
        ;;_ (println "sargam-section1")
        ;;_ (pprint sargam-section1)
        assigned (atom #{})
        sargam-line (some #(if (= (:my_type %) :sargam_line) %)
                          (:items sargam-section1))
        ;; TODO: make atom of column-map
        column-map (reduce line-column-map {}  (:items sargam-section1))
        _ (if false (do
                      (println "**** sargam-section")
                      (pprint sargam-section1)
                      (println "**** column-map")
                      (pprint column-map)
                      ))
        line-starts (map :start_index (:items sargam-section1))
        line-start-for  (fn line-start-for-fn[column] 
                          (last (filter (fn[x] (>= column x)) line-starts)) )
        column-for-node (fn[node]
                          (- (:start_index node) (line-start-for (:start_index node))))

        lower-lines (filter #(= (:my_type %) :lyrics_line) (:items sargam-section1))
        _ (if false (do
                      (println "sargam-section is")
                      (pprint sargam-section1)
                      ;_ (println "--------end sargam-section is")
                      ;; _ (println "--------lower-lines is")
                      ;; _ (pprint lower-lines)
                      ))
        syls-to-apply (atom (map :source (filter #(= :syllable (:my_type %))
                                                 (mapcat #(:items %) lower-lines))))
        in-slur (atom false)

        postwalk-for-ornament-octaves  ;; Code is a little clumsy. Handle ornament octave dots.
        (fn postwalk-for-orn-octaves[node]
          (if (and (= (:my_type node) :pitch)
                   (:in_ornament node)) ;; This is a pitch within an ornament
            (let [
                  column (column-for-node node)
                  nodes (get column-map column) 
                  my-dots (filter (fn[x] ((:my_type x) upper-dot-set)) nodes)
                  nums 
                  (map (fn map-dots[x]
                         (let [my_type (:my_type x)]
                           (if (< (:group_line_no x) (:group_line_no node))              
                             (my_type upper-dot-magnitude)
                             (- (upper-dot-magnitude my_type))
                             )))
                       my-dots)

                  ]
              _ (swap! assigned clojure.set/union @assigned (set my-dots))
              (assoc node :octave (apply + nums)))
            ;; else
            node))
        sargam-section2 (postwalk postwalk-for-ornament-octaves sargam-section1) 
        debug false
        postwalk-fn 
        (fn sargam-section-postwalker[node]
          ;; save this for debugging
          (if debug
            (if (:my_type node) (println "postwalker:" (:my_type node)))
            )
          (let [ my-type (:my_type node)
                column (if my-type (column-for-node node))
                nodes (if my-type (get column-map column)) 
                ]
            ;; (pprint my-type)
            (case my-type
              :pitch
              (cond 
                (:in_ornament node) ;; This is a pitch within an ornament
                node ;; Handled above
                (:pointer node)  ;; pitch_to_use case. Messy code!! TODO: rewrite? how
                node 
                ;; else
                true
                (let [
                      ;; _ (println "pitch is "  "\n--------")
                      ;; _ (pprint node)
                      ;; _ (println "pitch is "  "\n--------")
                      ;; _ (println "in-slur is " @in-slur)
                      has-begin-slur (some (fn[x] (= :begin_slur (:my_type x))) (:attributes node))
                      has-end-slur (some (fn[x] (= :end_slur (:my_type x))) (:attributes node))
                      all-orns1 (filter #(= :ornament (:my_type %)) (my-seq sargam-section2))
                      all-orns (filter #(not (@assigned %)) all-orns1)
                      ;; To find the orns before, look at each ornament and
                      ;; find the ones where column + length of source is one less
                      ;; than the pitch's column
                      orns-before (filter #(= column (+ (count (:source %)) (column-for-node %)))
                                          all-orns)
                      orns-after (filter #(= column (dec (column-for-node %)))
                                         all-orns)
                      orns (concat orns-before orns-after)
                      ;; Have to update @assigned set before 'modifying' the orns
                      _ (swap! assigned clojure.set/union @assigned (set orns))
                      ;;
                      orns-before-for-this-node (map #(assoc % :placement :before) orns-before)
                      orns-after-for-this-node (map #(assoc % :placement :after) orns-after)
                      orns-for-this-node (concat orns-before-for-this-node orns-after-for-this-node)
                      next-syl (first @syls-to-apply)
                      my-syl (if (and next-syl
                                      (not @in-slur))
                               (do (swap! syls-to-apply rest)
                                   next-syl))
                      ]
                  (if has-begin-slur (reset! in-slur true)) 
                  (if has-end-slur (reset! in-slur false)) 

                  (update-sargam-pitch-node node (concat nodes orns-for-this-node) my-syl)
                  ))
              ;; TODO: Actually only some nodes get this 
              ;; treatment. And  
              ;; update-sargam-pitch-node 
              ;; has common code re attributes
              node)))
]
(assert (= :sargam_line (:my_type sargam-line)))
(assert (map? column-map))
(postwalk postwalk-fn sargam-section2)
))


(defn significant?[x]
  {:post [ (contains? #{true false nil} %) ] }
  "the last 2 dashes of S--- are not significant"
  (and (map? x) (#{:pitch :dash} (:my_type x))
       (not (:ignore x))))

(defn number-significant-pitches-in-beat[my-beat]
  {:pre [(= :beat (:my_type my-beat))]
   :post [(= :beat (:my_type %))]
   }
  "add beat-counter to significant pitches in a beat"
  (let [ beat-counter (atom -1) ]
    (postwalk (fn[a] (if-not (significant? a)
                       a
                       ;; else
                       (do
                         (swap! beat-counter inc)
                         (assoc a :beat-counter @beat-counter))
                       ))  my-beat)
    ))

(def is-line? #{:sargam_line})

(defn whole-note?[node]
  (= (:fraction_array node) [{:numerator 1, :denominator 1}])
  )


(defn dash-at-start[obj]
  {:pre [ (:items obj)
         ]
   :post [ (or (nil? %) (= :dash (:my_type %)))]
   }
  "Return the dash at start of obj's items or nil if there is a :pitch first"
  (let [my-first
        (first (filter (fn[z] (and (not (:ignore z))
                                   (contains? #{:pitch :dash} 
                                              (:my_type z))))
                       (my-seq obj))) ]
    (cond (:rest my-first)
         nil
          (= :dash (:my_type my-first))
      my-first 
          true
      nil)
    ))

;;;;      (comment   (map (fn fix[maybe-measure] 
;;;;               (println "in fix, type of maybe-measure is " (:my_type maybe-measure))
;;;;                (if-not (= :measure (:my_type maybe-measure))
;;;;                  maybe-measure
;;;;                  (let [dash-at-start  (dash-at-start maybe-measure)
;;;;                        done (atom false)]
;;;;                 ;;   (println "dash case" " dash-at-start is:" dash-at-start)
;;;;                    
;;;;                    (if (nil? dash-at-start) maybe-measure
;;;;                      ;; else
;;;;                      (postwalk 
;;;;                        (fn[item] 
;;;;                        ;;  (if true (do (println "fix:item is") (pprint item)))
;;;;                          (cond 
;;;;                            (= item dash-at-start)
;;;;                            (let [pitch-to-use-for-tie (:pitch_to_use_for_tie item)]
;;;;                              (dissoc (assoc item 
;;;;                            ;;  :zzz (println "change dash-at-start")
;;;;                                        :octave (:octave (:octave pitch-to-use-for-tie)) 
;;;;                                        :normalized_pitch (:normalized_pitch pitch-to-use-for-tie) 
;;;;                                     :my_type :pitch
;;;;                                     :z true
;;;;                                        )  :pitch_to_use_for_tie))
;;;;
;;;;                                (:pointer item)
;;;;                                item
;;;;                                true
;;;;                          (do
;;;;                            (if (= :pitch (:my_type item))
;;;;                              (reset! done true))
;;;;                            (if (or @done 
;;;;                                    (not (:dash_to_tie item))) 
;;;;                              item
;;;;                              (assoc-in item [:pitch_to_use_for_tie :pitch-counter] (:pitch-counter dash-at-start))
;;;;                          ))))
;;;;                        (my-seq maybe-measure)))
;;;;                    )))
;;;;              (:items line)))




(defn measure-case-for-fix[measure]
  ;;; TODO: Only set :tied for a pitch if there is a dash at
  ;;; the beginning of the next measure.....
  ;;; S - S S S tieing the first 2 sas
  {:pre [ (= :measure (:my_type measure))] }
  (if false (do
  (println "measure-case-for-fix")
  (println "\n\n\n\n")))

  (let [done (atom false)]
  (postwalk (fn postwalk-measure-item[x] 
              (let [dash-at-start (dash-at-start measure)
                    my-type (:my_type x) 
                    ]
              (cond
                (not my-type)
                x
             ;;   (or (pprint my-type) false)
              ;;  x
                (:pointer x)
                x
                (= dash-at-start x)
                (let [;  _ (println "dash-at-start case")
                      z (:pitch_to_use_for_tie x)

                      ]
               (dissoc
                  (assoc x :my_type :pitch
                         :octave (:octave z)
                         :normalized_pitch (:normalized_pitch z)
                         :pitch_source (:pitch_source z)
                         :value (:value z)
                         :syllable (:syllable z)
                         ) 
                     :pitch_to_use_for_tie))
                (= my-type :pitch)
                (do (reset! done true)
                    x)
                (and 
                  (= my-type :dash)
                     dash-at-start
                  (not @done)
                  ) 
                (do ;; (println "******") 
                   (assoc-in x [:pitch_to_use_for_tie :pitch-counter] (:pitch-counter dash-at-start)) 
                    ) 
                true
                x
                   ))) measure)
  ))

(defn tied-over-barline-fix[line]
  ;; This is to fix  where S - | - - is emitting a whole note followed
  ;; by a barline instead of 1/2 note barline 1/2 note
  ;; The current code has set the pitch_counter of the second dash of measure
  ;; 2 to the pitch-counter of the S. 
  ;; We need to set the pitch counter of the second dash to the pitch-counter of the first dash of measure 2. 
  ;;
  ;; For each measure in line
  ;;   if measure starts with a dash
  ;;     set pitch-counter of following dashes to the pitch-counter of the first dash
  {:pre [
         (is-line? (:my_type line))
         (:items line) 
         ]
   :post [(is-line? (:my_type line))]
        ;;  (do (println "leaving fix, line is") (pprint %) true)
   }
;;(if true (do (println "entering fix")(pprint line)(println "\n\n\n")))
  (assoc line :items 
         (map (fn[line-item] 
                {:pre [ (or  (#{:measure :line_number} (:my_type line-item))
                            (is-barline? line-item))]}
               ;; (println (:my_type line-item))
               (if false (do (println "line-item type is" (:my_type line-item)))) 
                (cond (= :measure (:my_type line-item))
                  (measure-case-for-fix line-item)
                      true
                   line-item)
                )
             
              (:items line))
         )
  )

(defn combine-tied-whole-notes-by-pitch-counter[line]
  ;; TODO: need to change it to do it by MEASURE. That combined with fix should do it
  ;; Collecting tied notes
  {:pre [(is-line? (:my_type line))]
   :post [ (map? %)
          (every? integer? (keys %)) 
          ] 
   }
  (if false (do (println "\n\nentering combine-tied-whole-notes-by-pitch-counter") (pprint line)(println "\n\n")))
  ;; TODO: have fraction array collect items by barline!
  ;; To avoid S - - | - R - | having the Sa be a whole note
  ;;
  ;; Doesn't handle case of S - - - | - - - - |
  ;; TODO: rewrite in more idiomatic clojure
  ;;(println "my-seq line")
  ;;(pprint (my-seq line))
  ;;(println "my-seq line")
  (reduce (fn [accumulator node]
            ;; (pprint "in fn: node is:")
            ;; (pprint node)

            (let [
                  fraction-array (:fraction_array node)
                  pitch-to-use-for-tie (:pitch_to_use_for_tie node)
                  pitch-counter (:pitch-counter pitch-to-use-for-tie)
                  ary (or (get accumulator pitch-counter
                               ) [])
                  ]
              (if pitch-counter ;; TODO: review (whole-note? node)
                (assoc accumulator pitch-counter (conj ary (first fraction-array))) 
                ;; else
                accumulator 
                )))
          {} (filter #(:dash_to_tie %)  (my-seq line))
          ))




(defn fix-fraction-array[line]
  {:pre [(is-line? (:my_type line))]
   :post [(is-line? (:my_type %))] }
  (let [pitch-counter-to-index
        []
        ]
    line
    ))

(defn add-pitch-counters[line]
  {:pre [(is-line? (:my_type line))]
   :post [(is-line? (:my_type %))] }
  "Add pitch-counter to every significant item in the line"
  (let [ pitch-counter (atom -1)]
    (postwalk (fn add-pitch-counters[item-in-line] 
                (if-not (significant? item-in-line)
                  item-in-line
                  (do
                    (swap! pitch-counter inc)
                    (assoc item-in-line :pitch-counter @pitch-counter))))
              line)))


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
(defn- tie-and-measure-pitches[line]
  ;;; Might have to do tieing after fix... TODO
  {:pre [(is-line? (:my_type line))]
   :post [(is-line? (:my_type %))] }
  ;; Needs to be called from within collapse-sargam-section
  ;; TODO: not setting fraction_array and fraction_total properly
  ;; example | S -
  (let [
        line2 (add-pitch-counters line)
        pitches (into []  (filter significant? (my-seq line2) ))
        line3 (postwalk 
                (fn [node-in-line] (if (= :beat (:my_type node-in-line))
                                     (number-significant-pitches-in-beat node-in-line)
                                     node-in-line))
                line2)
        ]
    (postwalk (fn walk-line-tieing-dashes-and-pitches[node-in-line] 
                "if item is dash at beginning of line, set :dash_to_tie false and :rest true
                if item is dash (not at beginning of line) and line starts with a pitch"
                (if-not (significant? node-in-line)
                  node-in-line
                  ;; else
                  (let
                    [
                     ;;xyz (pprint node-in-line)
                     my-key (:my_type node-in-line) 
                     prev-item (last (filter #(and (significant? %)
                                                   (< (:pitch-counter %) (:pitch-counter node-in-line))) pitches))
                     next-item  (first (filter #(and (significant? %)
                                                     (> 
                                                       (:pitch-counter %)
                                                       (:pitch-counter node-in-line))) 
                                               pitches))
                     ]
                    (cond 
                      ;; Case 1: dash is first significant item in line
                      (and (= my-key :dash)  ;; dash is first item in line
                           (not prev-item))  ;; No previous item 
                      (assoc node-in-line              ;; it becomes a rest.
                             :dash_to_tie false
                             :rest true )
                      ;; Case 2: pitch and next item is a dash  
                      ;; TODO: should be next "unignored" dash
                      (and 
                           (= :pitch my-key)   
                           (= :dash (:my_type next-item)))
                      (do
                        (if false (do
                        (println "setting :tied true !!!next-item is:")
                        (pprint next-item)))
                        ;; and add to fraction-array of current note!
                        (assoc node-in-line :tied true)   ;; tie to next dash
                        )
                      ;; 
                      ;; )
                      ;; Case 3: dash at beginning of beat
                      (and (= :dash my-key) 
                           (= 0 (:beat-counter node-in-line)))
                      ;; doremi-v1 requires that the :tied attribute not
                      ;; be set to anything if not tied
                      (let [prev-pitch         ;; previous pitch in this line 
                            (last (filter #(and (= :pitch (:my_type %))
                                                (< 
                                                  (:pitch-counter %)
                                                  (:pitch-counter node-in-line)))
                                          pitches))
                            my-tied (and next-item (= :dash (:my_type next-item))) 
                            ;;  _ (println "prev-pitch")
                            ;;  _ (pprint prev-pitch)
                            my-result1 (assoc node-in-line 
                                              :case3 true
                                              :dash_to_tie true
                                              :pitch_to_use_for_tie (assoc prev-pitch :pointer true))
                            ]
                        ;; set dash_to_tie true ; tied true, and pitch_to_use_for_tie
                        (if my-tied
                          (assoc my-result1 :tied true)
                          ;; else
                          my-result1))
                      true
                      node-in-line
                      )
                    )
                  ))
              line3)
    ))

(defn- handle-beat-in-main-walk[ node2]
  {:pre [(= :beat (:my_type node2))]
   :post [(= :beat (:my_type %))] }
  (let [
        ;; [ [pitch dash dash] pitch pitch ] => [pitch dash dash pitch pitch]
        ;; apply concat to get rid of pitch with dashes' array
        my-fun (fn[z]
                 (apply concat (into [] (map (fn[x] (if (vector? x) x (vector x))) z))))
        items2 (into [] (my-fun (:items node2)))  ;; TODO: ugly
        subdivisions 
        (count (filter (fn[x] (unit-of-rhythm (:my_type x))) 
                       items2))
        my-beat (assoc node2 :items items2 :subdivisions subdivisions)
        ]
    (postwalk (fn postwalk-in-beat[node-in-beat] 
                (if (and (#{:pitch :dash} (:my_type node-in-beat))
                         (not (:ignore node-in-beat))
                         (:numerator node-in-beat)  ;; probably don't need this
                         )
                  (let [my-ratio (/ (:numerator node-in-beat) subdivisions)
                        frac 
                        (if (= (class my-ratio) java.lang.Long)
                          (array-map  :numerator 1 
                                     :denominator 1) 
                          ;; else 
                          (array-map :numerator (numerator my-ratio)
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

(defn- handle-pitch-with-dashes-in-main-walk[[pitch & rest]]
  {:pre [ (contains? #{:dash :pitch} (:my_type pitch))]
   :post [ (vector? %)
          ]
   }
  "Handle  - and S--  and  ---. Returns vector of pitches"
  ;; set :ignore true for all the dashes
  ;;  and set numerator for pitch
  (let [micro-beats (inc (count (filter #(= :dash (:my_type %)) rest)))]
    (into [] (concat [ (assoc pitch
                              :numerator micro-beats)] 
                     (map (fn[x] (if (= :dash (:my_type x))
                                   (assoc x :ignore true)
                                   ;; else
                                   x)) rest)))))

(def reserved-attributes [:lines 
                          :attributes
                          :warnings
                          :my_type
                          :source
                          :start_index
                          :attributes
                          ]
  )

(defn- handle-composition-in-main-walk[node2]
  {:pre [ (= :composition (:my_type node2))]
   :post [ (= :composition (:my_type %))]
   }
  (let [
        attribute-sections 
        (filter #(= :attributes (:my_type %))  (:items node2))
        attribute-section (first attribute-sections)
        ;; _ (println "attribute-sections" attribute-sections) 
        sections 
        (filter #(= :sargam_section (:my_type %))  (:items node2))
        lines
        (into [] (map  (fn[x] (some #(if (= :sargam_line (:my_type %)) %) 
                                    (:items x))) sections))
        items-map2 
        (into {} (map (fn[[k v]] [(keyword (lower-case (name k))) v]) (:items_map attribute-section)))

        lines2 (into [] (map #(assoc % :kind "latin_sargam") lines))
        ] 
    (assert (map? items-map2))
    (merge (dissoc node2 :items) {
                                  :key 
                                  (:key items-map2 "c")
                                  ;; Don't set default time signature!
                                  :time_signature
                                  (:timesignature items-map2 nil) 
                                  :apply_hyphenated_lyrics
                                  (:apply_hyphenated_lyrics items-map2 false)
                                  :mode (:mode items-map2 "major") 
                                  :filename (:filename items-map2 "untitled") 
                                  :author (:author items-map2 "")
                                  :title (:title items-map2 "")
                                  :notes_used (:notes_used items-map2 "")
                                  :force_sargam_chars_hash (:force_sargam_chars_hash items-map2 {})
                                  :warnings []
                                  :lines  lines2 
                                  :attributes 
                                  attribute-section
                                  }
           )))

(defn- handle-attribute-section [node]
  {:pre [ (= :attribute_section (:my_type node))]
   :post [ ;;(pprint %)
          (= :attributes (:my_type %))]
   }
  ;;  (println "my_type node is:" (:my_type node))
  ;; (pprint node)

  ;;(pprint node)
  (let [
        ;; make keys keywords.
        items-map (apply array-map 
                         (map-indexed (fn [i v] 
                                        (if (even? i) 
                                          (keyword v)
                                          ;;  (keyword (lower-case v)) 
                                          ;; else
                                          v))  (:items node)))
        x (into [] (map (fn[[k v]] 
                          (str k " " v)
                          {  :my_type :attribute :key (name k) :value v }  
                          )

                        items-map))
        ]
    ;;(pprint x)
    ;; (merge node2 { :my_type :attributes })
    ;; (let [ [:ATTRIBUTE_SECTION [:ATTRIBUTE_SECTION_ITEMS pairs]] node
    ;;  [:ATTRIBUTE_SECTION
    ;;     [:ATTRIBUTE_SECTION_ITEMS "hi" "john" "author" "me"]]
    ;; output looks like:
    ;; { my_type: 'attributes',
    ;;  items: 
    ;;   [ { my_type: 'attribute', key: 'foo', value: 'bar', source: 'todo' },
    ;;    { my_type: 'attribute', key: 'hi', value: 'john', source: 'todo' } ],
    ;; source: 'xx' },
    ;;{:start_index 0,
    ;; :source "hi:john\nauthor:me",
    ;; :my_type :attribute_section,
    ;; :items ["hi" "john" "author" "me"]} 
    ;; (println "items-map" items-map)
    (assert (= :attribute_section (:my_type  node)))
    (assert (:items node))
    (assert (vector? (:items node)))
    ;;(println "items-map:") (pprint items-map)
    (merge node {:source "TODO" :my_type :attributes :items x :items_map items-map})
    ))

(defn- main-walk[node txt]
  (if-not (vector? node) node
    ;; else
    (let [
          my-key (first node)
          my-map (array-map :my_type (keyword (lower-case (name my-key)))
                            :start_index (start-index node) 
                            )
          node2 (if (and (vector? (second node)) 
                         (keyword? (first (second node)))
                         (.endsWith (name (first (second node))) "ITEMS"))
                  (merge {:items (subvec  (second node) 1) } my-map)
                  ;; else
                  node)
          ]
      (case my-key 
        :ATTRIBUTE_SECTION
        (handle-attribute-section node2)
        :SARGAM_MUSICAL_CHAR
        (let [
              [_ [sarg-keyword]] node
              ]
          (assoc my-map :value sarg-keyword 
                 :octave 0
                 :normalized_pitch (sarg-keyword to-normalized-pitch)
                 :pitch_source (sarg-keyword sargam-pitch-to-source)
                 ))
        :SARGAM_ORNAMENT 
        ;;  "ornaments should look like this"
        ;;                        { my_type: 'ornament',
        ;;                           id: 2,
        ;;                           column_offset: undefined,
        ;;                           source: 'N',
        ;;                           usable_source: 'N',
        ;;                           ornament_items: 
        ;;                            [ { my_type: 'pitch',
        ;;                                normalized_pitch: 'B',
        ;;                                attributes: [],
        ;;                                pitch_source: 'N',
        ;;                                source: 'N',
        ;;                                column_offset: 0,
        ;;                                octave: 0,
        ;;                                group_line_no: 0 } ],
        ;;                           column: 1,
        ;;                           group_line_no: 0,
        ;;                           placement: 'before' } 
        ;;   Ornaments can be before or after a pitch. Set :placement to  "before"
        ;;   or "after"
        (let [
              my-items (:items node2)
              node3 (dissoc node2 :items)
              source (get-source node txt)
              ]
          ;;(println :SARGAM_ORNAMENT)
          (merge 
            node3 
            {
             :my_type :ornament
             :usable_source source
             :source source
             :ornament_items (into [] 
                                   (map (fn[x] 
                                          ;; TODO
                                          (merge x {:pointer true 
                                                    :in_ornament true
                                                    :my_type  :pitch} 
                                                 )
                                          )
                                        my-items))
             }))
        :MEASURE
        ;; [:MEASURE
        ;;  [:BARLINE [:LEFT_REPEAT "|:"]]
        ;;  [:MEASURE_ITEMS
        ;;          [:BEAT
        ;;             [:BEAT_UNDELIMITED_ITEMS
        ;;                             [:SARGAM_PITCH [:SARGAM_MUSICAL_CHAR [:S]]]]]]
        ;;  [:BARLINE [:SINGLE_BARLINE "|"]]]
        (let [ might-be-barline (second node) 
              my-items2  
              (if (:is_barline (second node))
                (into [(second node)] (rest (nth node 2)))
                ;; else
                (:items node2) 
                )
              my-items3
              (if (:is_barline (last node))
                (conj my-items2 (last node))
                my-items2
                )
              beat-count (count (filter #(= (:my_type %) :beat) my-items3))
              ]
          ;; TODO: review is_partial. It is not really needed if always set true
          (assoc my-map :beat_count beat-count :is_partial true :items my-items3))
        :TALA 
        (assoc my-map :source (get-source node txt))
        :CHORD_SYMBOL
        (assoc my-map 
               :source (get-source node txt))
        :END_SLUR_SARGAM_PITCH
        ;; TODO: DRY with BEGIN_SLUR_SARGAM_PITCH
        (let [
              [_ my-pitch2 end-slur ] node
              my-pitch (merge my-pitch2
                              {
                               :source (:source my-map)
                               })
              ]
          ;; add end slur to attributes
          (assoc my-pitch 
                 :attributes
                 (conj (into [] (:attributes my-pitch)) end-slur)))
        :BEGIN_SLUR_SARGAM_PITCH
        (let [
              [_ begin-slur my-pitch2] node
              my-pitch (merge my-pitch2
                              {:column_offset 1
                               :source (get-source node txt)
                               })
              ]
          ;; add begin slur to attributes
          (assoc my-pitch 
                 :attributes
                 (conj (into [] (:attributes my-pitch)) begin-slur)))
:UPPER_OCTAVE_LINE
node2
:LOWER_OCTAVE_LINE
node2
:SYLLABLE
(assoc my-map :source (get-source node txt))
:ALTERNATE_ENDING_INDICATOR
;; TODO: finish
(merge my-map {:my_type :ending :num 99 :source (get-source node txt) })
:COMPOSITION 
(handle-composition-in-main-walk (assoc node2 :source (get-source node txt)))
:PITCH_WITH_DASHES
(handle-pitch-with-dashes-in-main-walk (rest node))
:BEAT_DELIMITED_SARGAM_PITCH_WITH_DASHES
(handle-pitch-with-dashes-in-main-walk (rest node))
:DASHES  ;; can be a single dash
(handle-pitch-with-dashes-in-main-walk (rest node))
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
         :my_type 
         (keyword (keyword (lower-case (name (get-in node [1 0])))))
         :is_barline true))
:SARGAM_SECTION
(let [collapsed
      (collapse-sargam-section 
        (merge (sorted-map :items (subvec node 1)) my-map)
        txt)
      ;; TODO: should this be moved into collapse-sargam-section ??
      tied2a (tie-and-measure-pitches (some #(if (= (:my_type %) :sargam_line) %) (:items collapsed)))

      ;; should do it by measure, not line!! TODO  S - - - | - - - - | should result in tied wholes.
      ;;tied2 tied2a ;;; (tied-over-barline-fix tied2a)
      tied2 (tied-over-barline-fix tied2a)
      tied-whole-notes-by-pitch-counter (combine-tied-whole-notes-by-pitch-counter tied2)
      ;; experimental code
      ;; new-line (new-collect-tied-notes (some #(if (= (:my_type %) :sargam_line) %) (:items collapsed)))
      tied3 (postwalk (fn[node]
                        (if (and (:pitch-counter node) (not (:pointer node)))
                          (assoc node 
                                 :fraction_array 
                                 (into [] (concat (:fraction_array node)
                                                  (get tied-whole-notes-by-pitch-counter (:pitch-counter node)))))
                          ;; else
                          node
                          )) tied2)
      ;;  _ (println "tied-whole-notes-by-pitch-counter")
      ;; _ (pprint tied-whole-notes-by-pitch-counter)

      ]
  (if false (println "collapsed"))
  (if false (pprint collapsed)) 
  (assoc collapsed :kind "latin_sargam" :items [tied3])
  )
:SARGAM_PITCH
(let [
      [_ sarg & my-rest] node 
      ]
  _ (assert (= :sargam_musical_char (:my_type sarg)))
  ;; (pprint node)
  (merge 
    my-map
    sarg
    {
     :my_type :pitch
     :numerator 1  ;;; numerator and denominator may get updated later!
     :denominator 1
     :column_offset 0  ;; may get updated
     }
    )
  )
;; default
node2
)
))) ;; end main-walk

(defn transform-parse-tree[parse-tree txt]
  "Transform parse-tree into doremi-script json style format"
  ;;(println "parse tree:")
  ;;(pprint parse-tree)
  (if (map? parse-tree)
    parse-tree
    ;; else
    (postwalk 
      (fn[node] (main-walk node txt)) 
      parse-tree)))


(def run-tests false)
(comment
  (pprint (doremi_script_clojure.core/doremi-text->parse-tree 
            ;;   (-> "fixtures/yesterday.txt" resource slurp)))
            "<S-- R> -- -S"))
  )


(if nil ;;true ;;true ;;run-tests
  (let [txt2 "S | - - "
        txt3 (-> "fixtures/yesterday.txt" resource slurp)
        txt4 "- S"
        txt5 "S | - "
        txt "S - S S "
        txt7 "-S | - "
        txt8 "| GP - -  - | GR - - - |\nGeo-rgia geo-rgia"

        _ (println txt "\n\n")]
    
    (pprint (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script
              txt
              ))))

