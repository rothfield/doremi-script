(ns doremi_script_clojure.core
  (:gen-class)
  (:require	
    [instaparse.core :as insta]
    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [clojure.java.io :refer [input-stream resource]]
    [clojure.data.json :as json]
    ))


(defn- json-key-fn[x]
  (let [y (name x)]
    (if (= \_ (first y))
      (subs y 1)
      y)))

(defn slurp-fixture [file-name]
  (slurp (resource 
           (str "fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))

(def doremi-script-parser  
  (insta/parser
    (slurp (resource "doremiscript.ebnf"))))

(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn -main[& args]
  (let [txt (str (slurp *in*) "\n")
       x (transform-parse-tree (run-through-parser txt) txt)
        ]
  (json/pprint x :key-fn json-key-fn)))

