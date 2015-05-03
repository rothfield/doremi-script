(ns doremi-script.core
  (:require	
    [instaparse.core :as insta]
    [clojure.string :refer 
     [lower-case split replace-first upper-case lower-case join] :as string] 
    [clojure.zip :as zip]
    [clojure.java.io :as io :refer [input-stream resource]]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk]]
    [doremi-script.utils :refer [items map-even-items is? get-attribute]]
    ))

(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  ;; cp% runs current form. vim-fireplace
  (set! *warn-on-reflection* true)
  (use 'doremi_script.core :reload) (ns doremi_script.core)
  (use 'clojure.stacktrace)
  (print-stack-trace *e)
  (print-stack-trace *e)
  (pst)
  )

(def doremi-script-parser 
  ;;;Optional keyword arguments to insta/parser:
  ;;;   :start :keyword  (where :keyword is name of starting production rule)
  ;;;   :partial true    (parses that don't consume the whole string are okay)
  ;;;   :total true      (if parse fails, embed failure node in tree)
  ;;;   :unhide <:tags or :content or :all> (for this parse, disable hiding)
  ;;;   :optimize :memory   (when possible, employ strategy to use less memory)
  ;;;
  (insta/parser
    (slurp (resource "doremiscript.ebnf")) :total true))

(defn ^:private is-notation-system-name?
  [x]
  (contains? #{"sargam"
               "number"
               "abc"
               "hindi"
               "doremi"
               :doremi
               :sargam
               :number
               :abc
               :hindi} x))

(def kind-set
  #{:doremi-composition
    :sargam-composition
    :number-composition
    :abc-composition
    :hindi-composition})

(defn is-kind?[x]
  (contains? kind-set x))

(defn ^:private unique-for-assigned
  "Make a unique key using meta-data"
  [x]
  { :pre [(vector? x)]
   :post [(vector? %)]
   }
  [ (meta x) (first x)])

(defn ^:private in-assigned?
  "Tests whether x is in set my-set. Uses metadata to distinguish "
  [my-set x]
  (contains? my-set (unique-for-assigned x)))


(defn ^:private sargam-pitch->normalized-pitch
  "Map sargam pitch to pitch in the key of 'C'.  M => F# "
  [x]
  (get {"S" "C"
        "r" "Db"
        "R" "D"
        "g" "Eb"
        "G" "E"
        "m" "F"
        "M" "F#"
        "P" "G"
        "d" "Ab"
        "D" "A"
        "n" "Bb"
        "N" "B"
        "Sb" "Cb"
        "S#" "C#"
        "R#" "D#"
        "G#" "E#"
        "mb" "Fb"
        "Pb" "Gb"
        "P#" "G#"
        "D#" "A#"
        "N#" "B#"
        } x "ERROR"))

(defn ^:private number-pitch->normalized-pitch[x]
  "Map from number pitch to pitch in the key of 'C'.  4# => F# "
  (get {"1" "C"
        "2b" "Db"
        "2" "D"
        "3b" "Eb"
        "3" "E"
        "4" "F"
        "4#" "F#"
        "5" "G"
        "6b" "Ab"
        "6" "A"
        "7b" "Bb"
        "7" "B"
        "1#" "C#"
        "2#" "D#"
        "3#" "E#"
        "4b" "Fb"
        "5b" "Gb"
        "5#" "G#"
        "6#" "A#"
        "7#" "B#"
        } x "ERROR"))

(defn ^:private doremi-pitch->normalized-pitch[x]
  "Map from solfege pitch to pitch in the key of 'C'.  4# => F# "
  (get {"d" "C"
        "rb" "Db"
        "r" "D"
        "mb" "Eb"
        "m" "E"
        "f" "F"
        "f#" "F#"
        "s" "G"
        "lb" "Ab"
        "l" "A"
        "tb" "Bb"
        "t" "B"
        "d#" "C#"
        "r#" "D#"
        "m#" "E#"
        "fb" "Fb"
        "sb" "Gb"
        "s#" "G#"
        "l#" "A#"
        "t#" "B#"
        } (lower-case x) "ERROR"))



(defn ^:private hindi-pitch->normalized-pitch[x]
  "Map from Hindi pitch to pitch in the key of 'C'."
  (get
    {
     "\u0938" "C"
     "\u0930" "D"
     "\u095A" "E"
     "\u092E" "F"
     "\u092a" "G"
     "\u0927" "A"
     "\u0929" "B"
     "\u0938#" "C#"
     "\u0930#" "D#"
     "\u095A#" "E#"
     "\u092E#" "F#"
     "\u092a#" "G#"
     "\u0927#" "A#"
     "\u0929#" "B#"
     "\u092E'" "F#"  ;; note the tick
     }
    x "ERROR"))


(defn ^:private remove-notation-system-prefix
  "Change keyword to remove notation prefixes. :sargam-pitch -> :pitch "
  [k]
  { :pre [(keyword? k)]
   :post [(keyword? %)]
   }
  (let [ary (split (name k) #"-")
        prefix (first ary)        
        ]
    (if (is-notation-system-name? prefix)
      (keyword (join "-" (rest ary)))
      k)
    ))

(defn ^:private make-it-kommal[pitch]
  ;; Only flatten D E A and B !!!!! and should only be
  ;; done for hindi-composition (I think)
  ;; hackish
  { :pre [(is? :pitch pitch)]
   ;; :post [(is? :pitch %)]
   }
  (when false (println "make-it-kommal")
    (pprint pitch))
  (let [without-kommal (vec (remove #(is? :kommal-indicator %) pitch))]
    ;;    [:pitch "D" [:kommal-indicator "_"] [:octave 0]]]]
    ;;   (pprint without-kommal)
    (if (#{"D" "E" "A" "B"} (second pitch))
      (vec (update-in without-kommal [1] str "b"))
      pitch)
    ))

(defn ^:private apply-kommal-to-pitches
  [original-tree] ;; assigned]
  (when true 
    (pprint "apply-kommal-to-pitches, tree is")
    (pprint original-tree))
  (assert (is? :stave original-tree))
  (loop [loc (zip/vector-zip original-tree)
         id 0 ]
    (if (zip/end? loc)
      (zip/root loc)
      ;; else
      (let [current (zip/node loc)
            ]
        (cond
          (and (is? :pitch current)
               (get-attribute current :kommal-indicator)
               )
          (recur
            (zip/next
              (zip/edit loc make-it-kommal)
              )
            id)
          :else
          (recur (zip/next loc) id))))))

(defn ^:private match-slurs[original-tree]
;;  "add id to begin and end slurs"
  { :pre [(vector? original-tree)]
      :post [(vector? %)]
   }
  (assert (is? :composition original-tree))
  (println "entering match-slurs, original-tree=")
  (pprint original-tree)
  ;;(pprint "matchslurs") (pprint original-tree)
  ;(assoc original-tree :parsed
  (loop [loc (zip/vector-zip original-tree)
         id 0 ]
    (if (zip/end? loc)
      (zip/root loc)
      ;; else
      (let [current (zip/node loc)
            ]
        (cond
          (and (is? :pitch current)
               (get-attribute current :begin-slur)
               )
          (recur
            (zip/next
              (zip/edit loc conj [:begin-slur-id id])
              )
            id)
          (and (is? :pitch current)
               (get-attribute current :end-slur)
               )
          (recur (zip/next
                   (zip/edit loc conj [:end-slur-id id])) (inc id))
          :else
          (recur (zip/next loc) id))))))


(defn ^:private remove-notation-system-prefixes
  "Removes notation system prefixes from the tree"
  [original-tree]
  { :pre [(vector? original-tree)]
   :post [(vector? %)]
   }
  (assert (is-kind? (first original-tree)))
  (loop [loc (zip/vector-zip original-tree) ]
    (if (zip/end? loc)
      (zip/root loc)
      (recur
        (zip/next
          (cond  (and (vector? (zip/node loc))
                      (keyword? (first (zip/node loc))))
                (zip/edit loc
                          (fn[y]
                            (assoc y 0
                                   (remove-notation-system-prefix
                                     (first y)))))
                :else loc))))))

(defn ^:private normalize-pitches
  "Replace pitch names to CDEFGAB style from the style found in
  the parse tree (number,sargam,hindi)"
  [original-tree]
  { :pre [(vector? original-tree)]
   :post [(vector? %)]
   }
  (prn "entering normalize-pitches")
  (pprint original-tree)
  (assert (is-kind? (first original-tree)))
  (loop [loc (zip/vector-zip original-tree) ]
    (if (zip/end? loc)
      (zip/root loc)
      (recur
        (zip/next
          (cond
            (and (vector? (zip/node loc))
                 (#{:sargam-pitch :sargam-ornament-pitch }
                                  (first (zip/node loc))))
            (zip/edit loc
                      (fn[x]
                        (assoc x 1
                               (sargam-pitch->normalized-pitch
                                 (second x)))))
            (and (vector? (zip/node loc))
                 (#{:doremi-pitch :doremi-ornament-pitch }
                                  (first (zip/node loc))))
            (zip/edit loc  (fn[x]
                             (assoc x 1
                                    (doremi-pitch->normalized-pitch
                                      (second x)))))
            (and (vector? (zip/node loc))
                 (#{:hindi-pitch :hindi-ornament-pitch }
                                 (first (zip/node loc))))
            (zip/edit loc
                      (fn[x]
                        (assoc x 1
                               (hindi-pitch->normalized-pitch
                                 (second x)))))
            (and (vector? (zip/node loc))
                 (#{:number-pitch :number-ornament-pitch }
                                  (first (zip/node loc))))
            (zip/edit loc
                      (fn[x]
                        (assoc x 1
                               (number-pitch->normalized-pitch
                                 (second x)))))
            :else loc))))))

(defn ^:private normalize-kind[x]
  { :pre [(vector? x)]
   :post [(vector? %)]
   }
  (assert (is-kind? (first x)))
  (when false
    (println "entering normalize-kind, x =")
  (pprint x)
    )
  (let [kind (first x)]
    (cond
      kind
      (let [ has-attributes (= :attribute-section (first (second x)))
            ]
        (if has-attributes
          (update-in x [1] conj "kind" kind)
          (vec (concat [ (first x)]
                       [ [:attribute-section "kind" kind]]
                       (subvec x 1)
                       ))))


      true
      (vec (concat [:composition
                    [:attribute-section
                     "kind"
                     (first (second x))]]
                   (rest (second x))))

      )))

(defn ^:private start-index[x]
  (let [x (insta/span x)]
    (when x
      (first x))))


(defn ^:private line-column-map[my-map my-line]
  (when false (println "entering line-column-map, my-map=" my-map))
  (let [line-start (start-index my-line)
        ]
    (reduce (fn[accum obj]
              (when nil (println "line-column-map:, obj =" obj)
                (println "start-index obj=" (start-index obj))
                )
              (let [start-index (start-index obj)
                    column (if start-index
                             (- start-index line-start))
                    _ (when nil (println "obj" obj))
                    ]
                (cond (not start-index)
                      accum
                      (is? :ornament obj)
                      ;; Ornaments are not directly over notes.
                      ;; They are before or after
                      ;; Add the ornament twice in the column map.
                      ;; ;;  [:ornament [:G] [:m] [:P] [:D]]
                      (let [
                            span (insta/span obj)
                            ornament-string-length
                            (apply - (reverse span))
                            column-for-after-ornament (dec column)
                            column-for-before-ornament
                            (+ column  ornament-string-length)
                            ]
                        (assoc accum
                               column-for-after-ornament
                               (conj (get accum
                                          column-for-after-ornament [])
                                     (conj obj :after))
                               column-for-before-ornament
                               (conj (get accum
                                          column-for-before-ornament [])
                                     (conj obj :before))
                               )
                        )
                      true
                      (let [ column (- start-index line-start)
                            _ (when false
                                ( println
                                  "true case, column is" column
                                  " obj is" obj)) ]
                        (assoc accum
                               column
                               (conj (get accum column [])
                                     obj))
                        ))))
            my-map
            (tree-seq vector? identity my-line)
            )))



(defn ^:private takes-values-from-column?[x]
  (contains? #{:pitch :dash :barline  }
             x))
(comment
  [:composition
   [:sargam-composition
    [:sargam-stave
     [:sargam-upper-line [:upper-line-dot]]
     [:sargam-upper-line [:upper-line-two-dots]]
     [:sargam-notes-line
      [:sargam-measure [:sargam-beat [:sargam-pitch "S"]]]]]]]
  )

(defn ^:private is-upper-dot?[x]
  (and (vector? x) (contains? #{:upper-line-dot :upper-line-two-dots }
                              (first x))))

(defn ^:private is-dot?[x]
  (contains? #{:upper-line-dot :upper-line-two-dots
               :lower-line-dot :lower-line-two-dots}
             (first x)))


(defn ^:private is-lower-line?[x]
  (and (vector? x)
       (contains? #{:lower-octave-line :lyrics-line} (first x))))

(defn ^:private lines->column-map[lines]
  (reduce (fn[accum item]
            (line-column-map accum item)) {}
          (remove keyword?
                  (remove #(or (is? :notes-line %)
                               (is? :lyrics-line %)) lines))))


(defn ^:private pitch->octave[pitch]
  {
   :pre [ (or (= :ornament-pitch (first pitch)) (is? :pitch pitch))]
   :post[ (integer? %)]
   }
  (->> pitch (filter vector?)
       (map first)
       (map {:upper-line-dot 1
             :upper-line-two-dots 2
             :lower-line-dot -1
             :lower-line-two-dots -2 }
            )
       (remove nil?)
       (apply +))
  )


(defn ^:private calculate-octave[x]
  (if (not (or (is? :pitch x)
               (is? :ornament-pitch x)
               ))
    x
    (let [without-dots (vec (remove #(and (vector? %) (is-dot? %)) x))
          octave (pitch->octave x)
          ]
      (vec (conj without-dots [:octave octave]))
      )))



(defn ^:private assign-ornament-octaves[assigned my-stave]
  {;; :pre [(is-stave? my-stave)]
   :post [(is? :stave %)] }
  (when false
    (pprint @assigned)
    (pprint my-stave)
    )
  (let [column-map (lines->column-map
                     (filter #(is? :upper-line %) (items my-stave)))
        ]
    (vec
      (map
        (fn map-fn[line]
          (cond (is? :upper-line line)
                (postwalk
                  (fn assign-dots-postwalk-fn[item]
                    (cond
                      (is? :ornament-pitch item)
                      ;; Look for dot in this column from lines above
                      ;; and below this one. But not in the lower lines
                      (let [column (- (start-index item) (start-index line))
                            dots-from-upper-lines
                            (remove (partial in-assigned? @assigned)
                                    (filter is-upper-dot?
                                            (column-map column [])))
                            ]
                        (if (empty? dots-from-upper-lines)
                          item
                          (let [
                                fixed-dots
                                (map
                                  (fn[x]
                                    {:pre [(is-upper-dot? x)]
                                     :post [(is-dot? x)] }
                                    (if (> (start-index x) (start-index item))
                                      ({[:upper-line-dot]
                                        [:lower-line-dot]
                                        [:upper-line-two-dots]
                                        [:lower-line-two-dots]}
                                       x "wtf")
                                      x))  dots-from-upper-lines)
                                ]
                            (reset!
                              assigned
                              (apply conj
                                     @assigned
                                     (map unique-for-assigned
                                          dots-from-upper-lines)))
                            (calculate-octave (apply conj item fixed-dots)))
                          )
                        )
                      true
                      item)) line)
                true
                line
                )) my-stave))))


(defn ^:private assign-syllables[assigned stave]
  (let [ syls-to-apply
        (atom (mapcat items (filter #(is? :lyrics-line %) stave)))
        in-slur (atom false)
        ]
    (map (fn[line] (if (not (is? :notes-line line)) line
                     ;; else
                     (postwalk
                       (fn walk-fn[item]
                         (cond  (empty? @syls-to-apply)
                               item
                               (is? :pitch item)
                               (let [syl
                                     (if (not @in-slur)
                                       (first @syls-to-apply))
                                     ret-val
                                     (if syl
                                       (conj item [:syl syl])
                                       item)
                                     has-begin-slur
                                     (some #(is? :begin-slur %) item)
                                     has-end-slur
                                     (some #(is? :end-slur %)  item)
                                     ]
                                 (if syl (swap! syls-to-apply rest))
                                 (cond (and has-begin-slur has-end-slur)
                                       nil
                                       has-begin-slur
                                       (reset! in-slur true)
                                       has-end-slur
                                       (reset! in-slur false))
                                 ret-val)
                               true
                               item
                               ))     line)
                     )) stave)
    ))


(defn ^:private handle-slurs[assigned stave ]
  (map (fn[line]
         (if-not (is? :notes-line line)
           line
           (let [
                 pitch-positions
                 (map start-index
                      (filter #(is? :pitch %) (tree-seq vector? identity line)))
                 end-slur-positions
                 (map start-index (filter #(is? :end-slur %)
                                          (tree-seq vector? identity line)))
                 begin-slur-positions
                 (map start-index (filter #(is? :begin-slur %)
                                          (tree-seq vector? identity line)))

                 pitches-needing-begin-slurs
                 (set  (map
                         (fn[slur-position]
                           (first (filter
                                    (partial < slur-position)
                                    pitch-positions)))
                         begin-slur-positions))
                 pitches-needing-end-slurs
                 (set  (map
                         (fn[slur-position]
                           (last (filter
                                   (partial > slur-position)
                                   pitch-positions)))
                         end-slur-positions))
                 ]
             (postwalk (fn walk-line[item]
                         (cond (is? :pitch item)
                               (let [ items-to-conj
                                     (remove nil?
                                             [
                                              (if
                                                (contains?
                                                  pitches-needing-end-slurs
                                                  (start-index item))
                                                [:end-slur])
                                              (if (contains?
                                                    pitches-needing-begin-slurs
                                                    (start-index item))
                                                [:begin-slur])
                                              ])]
                                 (if (not-empty items-to-conj)
                                   (apply conj item items-to-conj)
                                   item))
                               (is? :begin-slur item)
                               nil
                               (is? :end-slur item)
                               nil
                               (is? :beat item)
                               (vec (remove nil? item))
                               (is? :measure item)
                               (vec (remove nil? item))
                               true
                               item))
                       line
                       ))))
       stave)
  )

(defn ^:private assign-to-notes-line[assigned stave ]
  {
   :pre [(is? :stave stave)]
   :post [(is? :stave %)
          ]
   }
  (let [ column-map (lines->column-map (items stave))
        notes-line (first (filter #(is? :notes-line %) (items stave)))
        notes-line-start-index (start-index notes-line)
        line-starts (map start-index (items stave))
        line-start-for-position
        (fn line-start-for-position-fn[position]
          (last (filter (fn[x] (>= position x)) line-starts)))
        line-start-for-node
        (fn line-start-for-node[node]
          (line-start-for-position (start-index node)))
        column-for-node
        (fn[node] (let [my-start-index (start-index node)]
                    (- my-start-index
                       (line-start-for-position my-start-index))))
        ]
    (vec (map (fn[line]
                (if-not (is? :notes-line line)
                  line
                  (postwalk
                    (fn[item]
                      (when false
                        (println "postwalk in collapse item is"
                                 item "\n\n"))
                      (cond
                        (and (vector? item)
                             (is? :pitch item)
                             (< (start-index item)
                                notes-line-start-index))
                        item  ;; skips ornaments
                        (and (vector? item)
                             (takes-values-from-column? (first item)))
                        (let [column (column-for-node item)
                              start-index (start-index item)
                              nodes-in-this-column1
                              (filter
                                (fn[x] (#{:upper-line-dot
                                          :upper-line-two-dots
                                          :lower-line-dot
                                          :lower-line-two-dots
                                          :ornament
                                          :chord
                                          :kommal-indicator
                                          :tala
                                          :ending
                                          :mordent} (first x)))

                                (remove
                                  (partial in-assigned? @assigned)
                                  (column-map column [])))
                              ;; remove ornaments from
                              ;; nodes-in-this-column when item is
                              ;; not pitch
                              nodes-in-this-column
                              (if-not (is? :pitch item)
                                (remove #(is? :ornament %)
                                        nodes-in-this-column1)
                                nodes-in-this-column1)
                              ]

                          (calculate-octave
                            (if (not-empty nodes-in-this-column)
                              (do
                                (reset! assigned
                                        (apply conj
                                               @assigned
                                               (map unique-for-assigned
                                                    nodes-in-this-column)))
                                (apply conj item nodes-in-this-column)
                                )
                              item)))
                        true
                        item
                        )) line)))
              stave
              ))))

(defn ^:private collapse-stave
  "Assigns items based on indentation"
  [stave]
  ;; { :pre [;(vector? stave)
  ;;        ;(is? :stave stave)]
  ;;:post [(is? :stave %)]
  ;; }
  (when false
    (println "entering collapse-stave,stave=")
    (println stave)
    )
  (let [my-assigned (atom (hash-set))]
    (vec (->> stave
              (handle-slurs my-assigned)
              (assign-ornament-octaves my-assigned)
              (assign-to-notes-line my-assigned)
              (remove
                (fn[x] (and (vector? x)
                            (#{:upper-line :lower-octave-line} (first x)))))
              (vec)
              (assign-syllables my-assigned)
              (vec)
              apply-kommal-to-pitches
              ))))

(defn ^:private get-ornament-pitches[ornament]
  (-> ornament rest drop-last)
  )

(def default-kind :sargam-composition)

;; (-> "a:hi\n\n.\n (Sr)" doremi-text->collapsed-parse-tree pprint)
(defn doremi-text->collapsed-parse-tree
    ([txt ] (doremi-text->collapsed-parse-tree txt default-kind))
    ([txt kind] 
     (assert (is-kind? kind))
     (let [ parsed (insta/parse doremi-script-parser 
                                txt 
                                :start kind)
          ]
       (if (insta/failure? parsed)    ;;;  or (string? x)))
         parsed
         (->> parsed
              normalize-kind
              normalize-pitches
              remove-notation-system-prefixes
              (map  #(if (is? :stave %)
                       (collapse-stave %)
                       %))
              vec
             match-slurs
              )
         ))))

