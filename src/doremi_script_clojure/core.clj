(ns doremi_script_clojure.core
  (:gen-class
    :methods [#^{:static true} [myparse [String] String]]) 

  (:require	
    [instaparse.core :as insta]

    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [clojure.java.io :refer [input-stream resource]]
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]] 
    ))


(defn- json-key-fn[x]
  (let [y (name x)]
    (if (= \_ (first y))
      (subs y 1)
      y)))


(defn pprint-results[x]
  (if (:_my_type x)
    (with-out-str (json/pprint x :key-fn json-key-fn))
    (with-out-str (pprint x))))


(defn slurp-fixture [file-name]
  (slurp (resource 
           (str "fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))

(def doremi-script-parser  
  (insta/parser
    (slurp (resource "doremiscript.ebnf")) :total true))

(defn run-through-parser[txt]
  (doremi-script-parser txt))


(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (clojure.string/join "\n" seq)))))


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

