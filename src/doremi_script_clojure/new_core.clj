(ns doremi_script_clojure.new_core
  (:require	
    [instaparse.core :as insta]

    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [doremi_script_clojure.to_lilypond :refer [to-lilypond]]
    [clojure.java.io :refer [input-stream resource]]
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk]]
    ))

(def assigned (atom {}))

(defn unique-for-assigned[x]
  "Make a unique key using meta-data"
  [ (meta x) x]) 

(defn in-assigned?[my-set x]
  (contains? my-set (unique-for-assigned x)))

(def debug false)

(def doremi-script-parser  
  (insta/parser
    (slurp (resource "new_doremiscript.ebnf")) :total true))

(defn is-composition?[x]
  (and (vector? x)
       (contains? #{:composition} (first x))))

(defn doremi-text->parse-tree[txt]
  {
   :pre [(string? txt)]
   :post [ (is-composition? %) ]
   }
  ;; Parser doesn't handle input that doesn't end with a newline.
  ;; So fix it here. 
  (doremi-script-parser txt))


(defn items[x] (nnext x))

(defn add-ids-and-create-map[parse-tree]
  (let [ctr (atom  0)
        parse-tree2 (postwalk (fn[x] (if (and (vector? x) (insta/span x))
                                       (with-meta (into [] (concat [(first x)]
                                                                   [(swap! ctr inc)] (rest x)))
                                                  (meta x)) 
                                       x))
                              parse-tree)
        saved-meta-data (reduce (fn[memo x] 
                                  (if (vector? x)
                                    (assoc memo (second x) (meta x))
                                    memo 
                                    )) {} (tree-seq vector? identity parse-tree2)
                                )
        _ (if false (do (println "saved-meta-data") (pprint saved-meta-data)))
        ]
    [saved-meta-data parse-tree2]))



(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.new_core :reload) (ns doremi_script_clojure.new_core) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (use 'doremi_script_clojure.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )

(comment
  ;;  (doremi-script-text->parsed-doremi-script 
  ;;   (slurp (resource "fixtures/georgia.doremiscript.txt")))
  )

(defn doremi-script-text->parsed-doremi-script[txt]
  (transform-parse-tree (doremi-text->parse-tree txt) txt))

(defn slurp-fixture[file-name]
  (slurp (resource 
           (str "fixtures/" file-name))))

(defn to-string[doremi-data]
  (let [
        default-formatter (fn default-formatter[x]
                            (array-map       :type (:my_type x)
                                       :source (:source x)
                                       ))
        postwalk-fn (fn to-string-postwalker[node]
                      (let [ my-type (if (:is_barline node)
                                       :barline
                                       ;; else
                                       (:my_type node))
                            default-val (default-formatter node)
                            ]
                        ;; (println my-type)
                        (case my-type
                          :beat
                          (array-map :type :beat :subdivisions (:subdivisions node) :items (:items node))
                          :measure
                          (array-map :type my-type 
                                     :beat_count (:beat_count node) 
                                     :items (:items node))
                          :composition
                          (array-map :type my-type 
                                     :source (:source node)
                                     :lines (:lines node))
                          :sargam_line
                          (array-map :type my-type 
                                     :source (:source node)
                                     :items (:items node))
                          :pitch
                          (array-map :type my-type 
                                     :value (:value node)
                                     :octave (:octave node)
                                     :syllable (:syllable node)
                                     )
                          :dash
                          :dash
                          ;; (default-formatter node)
                          :barline
                          (array-map :type my-type 
                                     :source (:source node)
                                     )
                          node
                          )))
        ]
    (postwalk postwalk-fn doremi-data)
    ))

(defn pprint-results[x]
  (if (:my_type x)
    (with-out-str (json/pprint x))
    (with-out-str (pprint x))))

(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (clojure.string/join "\n" seq)))))

(defn -json_text_to_lilypond[txt]
  "Takes parsed doremi-script json data as text"
  "Returns lilypond text"
  (to-lilypond (json/read-str txt)))

(defn doremi-json-to-lilypond[x]
  ""
  (to-lilypond x))

(comment
  (-json_text_to_lilypond "{}"))



(defn -myparse[txt]
  (try
    (let [ x (transform-parse-tree 
               (doremi-text->parse-tree  txt) txt)
          ]
      x)
    (catch Exception e (str "Error:" (.getMessage e)))
    )) 

(comment
  (pprint (-myparse "S"))
  )
(defn main-json[txt]
  (pprint-results 
    (transform-parse-tree (doremi-text->parse-tree  txt)
                          txt)))
(defn doremi-text->json-data[txt]
  (let [
        parse-tree (doremi-text->parse-tree txt)
        ]
    (transform-parse-tree parse-tree txt)
    ))

(defn doremi-text->lilypond[txt]
  (let [
        parse-tree (doremi-text->parse-tree txt)
        json-data (transform-parse-tree parse-tree txt)
        ]
    (to-lilypond json-data)
    ))

(defn -doremi_text_to_lilypond[x]
  (doremi-text->lilypond x)
  )

;;(pprint (doremi-text->lilypond "S"))

(defn usage[]
  (println "Usage: pipe std in as follows: \"SRG\" |  java -jar doremi-script-standalone.jar > my.ly to produce lilypond output. Or --json to produce doremi-script json format. Or --ly to produce lilypond output.") 
  )


(defn -main[& args]

  "Read from stdin. Writes results to stdout"
  "Command line params: --json returns doremi json data"
  "--ly returns lilypond data"
  "defaults to lilypond"
  (try
    (let [
          txt (get-stdin)
          parse-tree (doremi-text->parse-tree  txt)
          x (transform-parse-tree parse-tree txt)
          ]
      (cond 
        (= (first args) "--parse-tree")
        (pprint parse-tree)
        (= (first args) "--json")
        (println (pprint-results x))
        (or (= (first args) "--ly") (empty? args)) 
        (println (to-lilypond x))
        true
        (println (usage))
        )
      )
    (catch Exception e (println (str "Error:" (.getMessage e))))
    ))
;; (println (doremi-text->lilypond "<S-R>"))
;;(pprint (doremi-text->parse-tree "<S-R>"))
;; (pprint (doremi-text->parse-tree "-S-R <S - - R >"))
;; (pprint (doremi-text->parse-tree (slurp (resource "fixtures/yesterday.txt"))))
;;
(defn is-ornament?[x]
  {:pre [(vector? x)]}
  (contains? #{:ornament} (first x)))



(defn is-sargam-line?[x]
  (and (vector? x)
       (contains? #{:sargam-line} (first x))))

(defn is-sargam-section?[x]
  (and (vector? x)
       (contains? #{:sargam-section} (first x))))

(defn start-index[x]
  (let [x (insta/span x)]
    (if x
      (first x))))


(defn line-column-map[my-map my-line]
  (if false (do (println "entering line-column-map, my-map=" my-map)))
  (let [line-start (start-index my-line) 
        ]
    (reduce (fn[accum obj]
              (if debug (println "line-column-map:, obj =" obj))
              (let [start-index (start-index obj)
                    column (if start-index 
                             (- start-index line-start))
                    _ (if debug (do (println "obj")))
                    ;;  (pprint (start-index obj))))
                    ]
                (cond (not start-index)
                      accum
                      (is-ornament? obj) 
                      ;; Ornaments are not directly over notes. They are before or after
                      ;; Add the ornament twice in the column map.
                      ;; ;;  [:ornament [:G] [:m] [:P] [:D]]
                      (let [
                            span (insta/span obj) 
                            ornament-string-length (apply - (reverse span))
                            column-for-after-ornament (dec column)
                            column-for-before-ornament (+ column  ornament-string-length)
                            ]
                        ;;(println "ornament case")
                        (assoc accum
                               column-for-after-ornament
                               (conj (get accum column-for-after-ornament [])
                                     obj)
                               column-for-before-ornament
                               (conj (get accum column-for-before-ornament [])
                                     obj)
                               )
                        )
                      true
                      (let [ column (- start-index line-start)
                            _ (if false ( println "true case, column is" column " obj is" obj)) ]
                        (assoc accum 
                               column 
                               (conj (get accum column [])
                                     obj))
                        ))))
            my-map
            (tree-seq vector? identity my-line)
            )))
(defn is-end-slur?[x]
  (and (vector? x) (= :end-slur (first x))))

(defn is-begin-slur?[x]
  (and (vector? x) (= :begin-slur (first x))))

(defn is-beat?[x]
  (and (vector? x)
       (= :beat (first x))))

(defn is-pitch?[x]
  (and (vector? x)
       (contains? #{:Sb :Ssharp :Rsharp :Gsharp :Psharp :Pb :Dsharp :Nsharp 
                    :S :r :R :g :G :m :M :P :d :D :n :N } 
                  (first x))))

(defn takes-values-from-column?[x]
  (contains? #{:Sb :Ssharp :Rsharp :Gsharp :Psharp :Pb :Dsharp :Nsharp 
               :S :r :R :g :G :m :M :P :d :D :n :N :dash  } 
             x))

(defn is-upper-dot?[x]
  (if false (do (println "is-upper-dot?, x is") (pprint x)))
  (and (vector? x)
       (contains? #{:upper-octave-dot :upper-upper-octave-symbol } 
                  (first x))))

(defn is-ornament?[x]
  (and (vector? x)
       (contains? #{:ornament }  (first x))))

(defn is-dot?[x]
  (contains? #{:upper-octave-dot :upper-upper-octave-symbol 
               :lower-octave-dot :lower-lower-octave-symbol}
             (first x)))

(defn is-upper-line?[x]
  (and (vector? x)
       (contains? #{:upper-octave-line } (first x))))

(defn is-lower-line?[x]
  (and (vector? x)
       (contains? #{:lower-octave-line :lyrics-line} (first x))))

(defn is-lyrics-line?[x]
  (and (vector? x)
       (contains? #{:lyrics-line} (first x))))

(defn is-main-line?[x]
  (and (vector? x)
       (contains? #{:sargam-line} (first x))))


;;(use 'clojure.stacktrace) 
(defn lines->column-map[lines]
  (if false (do (println "entering lines->column-map, ") (pprint lines) (println "lines->column-map")))
  (reduce (fn[accum item] 
            (line-column-map accum item)) {}
          (remove keyword? (remove #(or (is-sargam-line? %) (is-lyrics-line? %)) lines))))



(defn assign-ornament-octaves[my-section]
  {;; :pre [(is-sargam-section? my-section)] 
   :post [(is-sargam-section? %)] }
  (if false (do (println "entering assign-ornament-octaves:")
               (pprint my-section)))
  (let [column-map (lines->column-map 
                     (filter is-upper-line? (items my-section)))
        _ (if false (do (println "****assign-ornament-octaves: column-map is")(pprint column-map)))
        _ (if false (do (println "****Entering assign-ornament-octaves: my-section is")(pprint my-section)))
        ]
    (into [] 
          (map 
            (fn map-fn[line] 
              (cond (is-upper-line? line)
                    (postwalk 
                      (fn assign-dots-postwalk-fn[item] 
                        (if false (println "assign-dots-postwalk-fn*** item is" item))
                        (cond 
                          (is-pitch? item)
                          ;; Look for dot in this column from lines above and below this one. But not in the lower lines
                          (let [column (- (start-index item) (start-index line))
                                _ (if false (println "column is:" column))
                                _ (if false (do (println "column-map is ")
                                                (pprint column-map)))
                                _ (if false (do (println "column-map column:") (pprint (column-map column))))
                                dots-from-upper-lines (remove (partial in-assigned? @assigned)
                                                              (filter is-upper-dot?
                                                                      (column-map column [])))
                                ;;     _ (if false (do (println "dots-from-upper-lines") (pprint dots-from-upper-lines)))
                                ]
                            (if debug (println "is-pitch case"))
                            (if (empty? dots-from-upper-lines)
                              item
                              (let [
                                    fixed-dots (map (fn[x] 
                                                      {:pre [(is-upper-dot? x)]
                                                       :post [(is-dot? x)] }
                                                      (if (> (start-index x) (start-index item))
                                                        ({[:upper-octave-dot] [:lower-octave-dot]
                                                          [:upper-upper-octave-symbol] [:lower-lower-octave-symbol]}
                                                         x "wtf")
                                                        x))  dots-from-upper-lines)
                                    ] 
                                (reset! assigned (apply conj @assigned
                                                        (map unique-for-assigned dots-from-upper-lines)))
                                (apply conj item fixed-dots))
                              )
                            )
                          true
                          item)) line)
                    true
                    line 
                    )) my-section)))) 


(defn assign-syllables[section]
  (if false (do (println "assign-syllables") (pprint section)))
  (let [ syls-to-apply (atom (items (first (filter is-lyrics-line? section))))
        _ (if false (println @syls-to-apply))
        in-slur (atom false)
        ]
    (map (fn[line] (if-not (is-sargam-line? line) line
                     ;; else
                     (postwalk (fn walk-fn[item]
                                 (cond  (empty? @syls-to-apply)
                                       item
                                       (is-pitch? item)
                                       (let [syl (if (not @in-slur) (first @syls-to-apply))
                                             ret-val (if syl (conj item syl) item)
                                             has-begin-slur (some is-begin-slur? item)
                                             has-end-slur (some is-end-slur? item)
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
                     )) section)
    ))

;; (print-stack-trace *e)

(defn handle-slurs[section]

  (map (fn[line]
         (if-not (is-sargam-line? line) 
           line
           (let [
                 pitch-positions 
                 (map start-index (filter is-pitch? (tree-seq vector? identity line)))
                 end-slur-positions (map start-index (filter is-end-slur? (tree-seq vector? identity line)))
                 begin-slur-positions (map start-index (filter is-begin-slur? (tree-seq vector? identity line)))

                 pitches-needing-begin-slurs 
                 (into #{}  (map (fn[slur-position]
                                   (first (filter 
                                            (partial < slur-position) pitch-positions)))
                                 begin-slur-positions))
                 pitches-needing-end-slurs 
                 (into #{}  (map (fn[slur-position]
                                   (last (filter 
                                           (partial > slur-position) pitch-positions)))
                                 end-slur-positions))
                 _ (if false (do
                               (pprint line) 
                               (println "pitch-positions" pitch-positions)
                               (println "begin-slur-positions" begin-slur-positions)
                               (println "end-slurs" end-slur-positions)
                               (println "pitches-needing-end-slurs" pitches-needing-end-slurs)))
                 ]
             (postwalk (fn walk-line[item] 
                         (cond (is-pitch? item)
                               (let [ items-to-conj  
                                     (remove nil? 
                                             [
                                              (if (contains? pitches-needing-end-slurs (start-index item)) [:end-slur])
                                              (if (contains? pitches-needing-begin-slurs (start-index item)) [:begin-slur])
                                              ])]
                                 (if (not-empty items-to-conj)
                                   (apply conj item items-to-conj)
                                   item))
                               (is-begin-slur? item)
                               nil
                               (is-end-slur? item)
                               nil
                               (is-beat? item)
                               (into [] (remove nil? item))
                               true
                               item))
                       line 
                       ))))
       section)
  )

(defn assign-to-main-line[section]
  {
   :pre [(is-sargam-section? section)]
   :post [(is-sargam-section? %)
         ;; (do (println (start-index %)) (pprint %) true) 
          ]
   }
  (if false (do (println "assign-to-main-line") (pprint section)))
  "collapse"
  (let [ column-map (lines->column-map (items section))
        _ (if false (println "column-map:\n" column-map))
        main-line (first (filter is-main-line? (items section)))     
        main-line-start-index (start-index main-line)
        _ (if debug (println "main-line is" main-line))
        line-starts (map start-index (items section))
        _ (if false (do (println "line-starts=") (pprint line-starts)))
        line-start-for-position  (fn line-start-for-position-fn[position] 
                                   (last (filter (fn[x] (>= position x)) line-starts)))
        line-start-for-node (fn line-start-for-node[node]
                              (line-start-for-position (start-index node)))
        column-for-node (fn[node] (let [my-start-index (start-index node)]
                                    (- my-start-index (line-start-for-position my-start-index))))
        ]
    (into [] (map (fn[line] 
           (if-not (is-sargam-line? line)
             line
             (postwalk (fn[item] 
                         (cond 
                           (and (vector? item) (is-pitch? item) (< (start-index item) main-line-start-index))
                           item
                           (and (vector? item) 
                                (takes-values-from-column? (first item)))
                           (let [column (column-for-node item)
                                 nodes-in-this-column 
                                 (filter (fn[x] (#{:upper-octave-dot :upper-upper-octave-symbol
                                                   :lower-octave-dot 
                                                   :lower-lower-octave-symbol :ornament 
                                                   :chord
                                                   :tala
                                                   :alternate-ending-indicator 
                                                   :mordent} (first x))) 

                                         (remove (partial in-assigned? @assigned) (column-map column [])))
                                 _ (if false (println "nodes-in-this-column" nodes-in-this-column))
                                 ]
                             (if (not-empty nodes-in-this-column)
                               (do
                                 (if false (println "conjing"))
                                 (reset! assigned (apply conj @assigned (map unique-for-assigned nodes-in-this-column)))
                                 (apply conj item nodes-in-this-column) 
                                 )
                               item))
                           true
                           item
                           )) line)))
         section 
         ))))

(defn collapse-section[section]
  (if false (do (println "collapse-section, section=") (pprint section)))
  (reset! assigned #{})
  (first (filter is-main-line?
                 (-> section 
                     handle-slurs
                     assign-ornament-octaves
                     assign-to-main-line
                     assign-syllables))))

(defn remove-ids[obj saved]
  {
   :pre [(is-composition? obj)]
   :post [
         (is-composition? %)
          ]
  }
  (postwalk (fn[x] 
             ;; (println "x is" x)
              (cond (and (vector? x) (keyword? (first x)) (number? (second x)) (> (count x) 2))
                     (into [] (concat [(first x)] (rest (rest x))))
              (and (vector? x) (keyword? (first x)) (number? (second x)) (= (count x) 2))
                    [(first x)]
                  ;;  (list? x)
                   ;; (into [] x)
                 true                    
                    x
                    )) obj))



(defn experiment[txt]
  (println "parsing") 
  (println txt)
;;  (pprint (doremi-text->parse-tree txt))
  (println "\n\n\n")
  (let [[saved parse-tree] (add-ids-and-create-map (doremi-text->parse-tree txt))
        _ (if false (do (println "parse-tree:") (pprint parse-tree)))
        ]
    (println "\n\n\n")
    (remove-ids (into [] (map #(if (is-sargam-section? %)
                                      (collapse-section %)
                                      %)
                              parse-tree)) saved)
   )) 
                     


(def runtest false)
(def t "Title:test\n\n.\nS\n.\nHi\n\nSRG" )
(def c (doremi-text->parse-tree 
         t         
         ))

;; ".\n.\nS#\n.\nHi"))
(def z
  "Hi: John\n\n.\nR GmPD\n.\n:\n*\n-S--\nYes-")



(if runtest (do 
              (pprint (experiment
                        (str " GR\n\n"
                             "..\nG\n S" 
                             "P  D" "\n\n"
                             "(SR Gm) P- D-\nHi there john")





                        ;;                                                 (slurp (resource "fixtures/yesterday.txt"))
                        ;;                           (slurp (resource "fixtures/semantic_analyzer_test.txt"))
                        ))))
(comment
  (str " GR\n"
       "P  D") 
  ;;          " .\n(SR)"
  ;;       t
  ;;    "..\nG\n S" 
  ;;   (slurp (resource "fixtures/semantic_analyzer_test.txt"))
  )



