(ns doremi_script_clojure.core
  (:gen-class)
  (:require	
    [instaparse.core :as insta]
    [doremi_script_clojure.semantic_analyzer :refer [transform-parse-tree]]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    ))

(defn- json-key-fn[x]
  (let [y (name x)]
    (if (= \_ (first y))
      (subs y 1)
      y)))

(def doremi-script-parser  
  (insta/parser
    (slurp (io/resource "doremiscript.ebnf"))))

(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn pp-to-json[x]
  "For debugging, pretty print json output. Not useable"
  (json/pprint :key-fn json-key-fn)) 

(defn- my-to-json[x]
  "returns json/text version of parse tree. It is a string"
  (json/write-str x :key-fn json-key-fn))


(defn -main[& args]
  (let [txt (slurp *in*)]
    (my-to-json (transform-parse-tree (run-through-parser txt) txt))))


