(ns doremi_script_clojure.core
  (:gen-class
    :methods [
              #^{:static true}[doremi_text_to_lilypond [String] String]
              ])
  (:require	
    [instaparse.core :as insta]

    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [doremi_script_clojure.to_lilypond :refer [to-lilypond]]
    [clojure.java.io :refer [input-stream resource]]
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk]]
    ))



(def doremi-script-parser  
  (insta/parser
    (slurp (resource "doremiscript.ebnf")) :total true))

(defn doremi-text->parse-tree[txt]
  ;; Parser doesn't handle input that doesn't end with a newline.
  ;; So fix it here. 
  (doremi-script-parser txt))

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
;; (pprint (doremi-text->parse-tree "-S-R"))
;; (pprint (doremi-text->parse-tree (slurp (resource "fixtures/bracketed_beat.txt"))))

