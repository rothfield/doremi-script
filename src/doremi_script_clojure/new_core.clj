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

(def debug false)
(def doremi-script-parser  
  (insta/parser
    (slurp (resource "doremiscript.ebnf")) :total true))

(defn doremi-text->parse-tree[txt]
  ;; Parser doesn't handle input that doesn't end with a newline.
  ;; So fix it here. 
  (doremi-script-parser txt))

(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.core :reload) (ns doremi_script_clojure.core) 
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
  {:pre [(vector? x)]}
 (contains? #{:sargam-line} (first x)))

(defn is-sargam-section?[x]
  (and (vector? x)
 (contains? #{:sargam-section} (first x))))

(defn start-index[x]
  (:instaparse.gll/start-index (meta x)))


(defn line-column-map[my-map my-line]
  (let [line-start (start-index my-line) 
        ]
    (reduce (fn[accum obj]
              (let [start-index (start-index obj)]
                (cond (not start-index)
                      accum
                      (is-ornament? obj) 
                      ;; Ornaments are not directly over notes. They are before or after
                      ;; Add the ornament twice in the column map.
                      (let [
                            span (insta/span obj) 
                            ornament-string-length (apply - (reverse span))
                            column (- start-index line-start)
                            column-for-after-ornament (dec column)
                            column-for-before-ornament (+ column  ornament-string-length)
                            ]
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
                      (let [ column (- start-index line-start) ]
                        (assoc accum 
                               column 
                               (conj (get accum column [])
                                     obj))
                        ))))
            my-map
            (rest my-line))
    ))

(if false (do
            (pprint (doremi-text->parse-tree ".\nS" ))

            (println "line-column-map")
            (pprint 
              (reduce (fn[accum item] 
                        (line-column-map accum item)) {}
                      (rest (second (doremi-text->parse-tree ".\nS" )))))
            ))

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
  (contains? #{:upper-octave-dot :upper-upper-octave-symbol } 
             x))

(defn is-ornament?[x]
  (contains? #{:ornament }  (first x)))

(defn is-dot?[x]
  (contains? #{:upper-octave-dot :upper-upper-octave-symbol 
               :lower-octave-dot :lower-lower-octave-symbol}
             x))

(defn is-upper-line?[x]
  {:pre [(vector? x)]}
  (contains? #{:upper-octave-line } (first x)))

(defn is-lower-line?[x]
  {:pre [(vector? x)]}
  (contains? #{:lower-octave-line} (first x)))

(defn is-lyrics-line?[x]
  {:pre [(vector? x)]}
  (contains? #{:lyrics-line} (first x)))

(defn is-main-line?[x]
  {:pre [(vector? x)]}
  (contains? #{:sargam-line} (first x)))

(defn assign-slurs-in-main-line[section]
 { :pre [;; (do (println "z1") (pprint section) true)
         (is-sargam-section? section)
         ] 
  :post [(is-sargam-section? %)] }
  (pprint section)
  (println "_____")
  (into [] (map (fn asign-slurs-fn[x]
         (if (and (vector? x) (is-main-line? x))
           x
           x)) section)))



(defn collapse-section[section]
  ;;(use 'clojure.stacktrace) 
  ;;(print-stack-trace *e)
  (let [
        upper-lines (filter  is-upper-line? (rest section))
        _ (if debug (println "upper-lines =" upper-lines))
        lower-lines (filter is-lower-line? (rest section))
        _ (if debug (println "lower-lines =" lower-lines))
        lyrics-lines (filter is-lyrics-line? (rest section))
        _ (if debug (println "lyrics-lines =" lyrics-lines))
        main-line (first (filter is-main-line? (rest section)))     
        _ (if debug (println "main-line is" main-line))
        line-starts (map start-index (rest section))
        _ (do (println "line-starts=") (pprint line-starts))
        line-start-for-position  (fn line-start-for-position-fn[position] 
                                   (last (filter (fn[x] (>= position x)) line-starts)))
        line-start-for-node (fn line-start-for-node[node]
                              (line-start-for-position (start-index node)))
        column-for-node (fn[node] (let [my-start-index (start-index node)]
                                    (- my-start-index (line-start-for-position my-start-index))))
        main-line-start-index (start-index main-line)
        assigned (atom #{})
        section->column-map (fn section->column-map[my-section]
                              (reduce (fn[accum item] (line-column-map accum item)) {}
                                      (remove is-sargam-line?  (rest my-section)))
                              )
        column-map1 (section->column-map section)
        assign-dots-to-ornaments (fn[my-section]
                                   (postwalk (fn assign-dots-to-ornaments-in-section-postwalk-fn[item] 
                                               (cond 
                                                 (and (vector? item) (is-pitch? item) (< (line-start-for-node item) main-line-start-index))
                                                 ;; Look for dot in this column from lines above and below this one. But not in the lower lines
                                                 (let [column (column-for-node item)
                                                       dots-from-upper-lines (remove @assigned (filter #(#{:upper-octave-dot :upper-upper-octave-symbol} (first %)) (column-map1 column []))  )
                                                       ]
                                                   (if (not-empty dots-from-upper-lines)
                                                     (let [
                                                           fixed-dots (map (fn[x] 
                                                                             {:pre [(is-upper-dot? (first x))]
                                                                              :post [(is-dot? (first x))] }
                                                                             (if (> (start-index x) (start-index item))
                                                                               ({[:upper-octave-dot] [:lower-octave-dot]
                                                                                 [:upper-upper-octave-symbol] [:lower-lower-octave-symbol]}
                                                                                x "wtf")
                                                                               x))  dots-from-upper-lines)
                                                           ] 
                                                       (reset! assigned (apply conj @assigned dots-from-upper-lines))
                                                       (apply conj item fixed-dots))
                                                     ;; else
                                                     item
                                                     )
                                                   )
                                                 true
                                                 item))
                                             my-section))

        _ (if debug (do (println "column-map") (pprint column-map1)))
        section-with-ornaments-assigned (assign-dots-to-ornaments section)
        _ (println "zzzzzzzzzzzz")
        _  (pprint section-with-ornaments-assigned) 
        _ (println "zzzzzzzzzzzz")
        column-map (section->column-map section-with-ornaments-assigned)
        section3 (assign-slurs-in-main-line section-with-ornaments-assigned)
        collapsed 
        (postwalk (fn[item] 
                    (cond 
                      (and (vector? item) (is-pitch? (first item)) (< (line-start-for-node item) main-line-start-index))
                      item
                      (and (vector? item) 
                           (takes-values-from-column? (first item)))
                      (let [column (column-for-node item)
                            nodes-in-this-column (remove @assigned (column-map column []))
                            ]
                        (if (not-empty nodes-in-this-column)
                          (do
                            (reset! assigned (apply conj @assigned nodes-in-this-column))
                            (apply conj item nodes-in-this-column) 
                            )
                          item))
                      true
                      item
                      ))
                  section3)
        _ (if debug (do (println "collapsed:") (pprint collapsed)))
        ]
    (println "assigned is")
    (pprint @assigned)
    ;; TODO: assign begin and end slurs and assign lyrics
    (first (filter is-main-line? (rest collapsed)))     
    ))




;; [:composition [:sargam-section [:upper-octave-line [:upper-octave-dot [:dot "."]]] [:sargam-line [:measure [:beat [:Ssharp]]]]]
;;


(defn experiment[txt]
  (println "parsing")
  (println txt)
  (let [parse-tree
        (doremi-text->parse-tree txt)
        _ (pprint parse-tree)
        ]
    (map #(if (is-sargam-section? %)
            (collapse-section %)
            %) (rest parse-tree))))


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
;;          z
                    (slurp (resource "fixtures/yesterday.txt"))
          ;;   (slurp (resource "fixtures/semantic_analyzer_test.txt"))
          ))

(println z)))


