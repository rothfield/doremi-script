(ns doremi_script_clojure.core
  (:gen-class
    :methods [#^{:static true}[myparse [String] String]
              #^{:static true}[json_text_to_lilypond[String] String]])
  (:require	
    [instaparse.core :as insta]

    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [doremi_script_clojure.to_lilypond :refer [to-lilypond]]
    [clojure.java.io :refer [input-stream resource]]
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]] 
    ))


(defn sample-data[]
  (read-string (slurp (resource "fixtures/sample-data.clj"))))

(def doremi-script-parser  
  (insta/parser
    (slurp (resource "doremiscript.ebnf")) :total true))

(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn doremi-script-text->parsed-doremi-script[txt]
  (transform-parse-tree (run-through-parser txt) txt))

;;(spit "resources/fixtures/sample-data.clj" (with-out-str (pprint (doremi-script-text->parsed-doremi-script "S R\nHi there"))))


(defn sample-data2[]
  (doremi-script-text->parsed-doremi-script "S\nHi John")
  )

;;(pprint (doremi-script-text->parsed-doremi-script "S"))
;;(pprint (transform-parse-tree (run-through-parser "S") "S"))
;; (pprint (sample-data))
;;
(defn pprint-results[x]
  (if (:my_type x)
    (with-out-str (json/pprint x))
    (with-out-str (pprint x))))


(defn slurp-fixture [file-name]
  (slurp (resource 
           (str "fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))




(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (clojure.string/join "\n" seq)))))

(defn -json_text_to_lilypond[txt]
  "Takes parsed doremi-script json data as text"
  "Returns lilypond text"
  (to-lilypond (-> "lilypond_templates/lilypond.txt" resource slurp)
               (json/read-str txt)))

(comment
  (-json_text_to_lilypond "{}"))



(defn -myparse[txt1]
  (try
    (let [
          txt (clojure.string/replace txt1 "\r\n" "\n")
          x (transform-parse-tree (run-through-parser  txt)
                                  txt)
          ]
      ;;(println "x is\n" x)
      ;;(println "class of x is:" (class x))
      (pprint-results x))
    (catch Exception e (str "Error:" (.getMessage e)))
    )) 

(defn -main[& args]
  "Read from stdin. Writes results to stdout"
  (try
    (let [
          txt1 (get-stdin)
          txt (clojure.string/replace txt1 "\r\n" "\n")
          x (transform-parse-tree (run-through-parser  txt)
                                  txt)
          ]
      (println (pprint-results x)))
    (catch Exception e (str "Error:" (.getMessage e)))
    ))




