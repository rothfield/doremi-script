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

(defn slurp-fixture [file-name]
  (slurp (clojure.java.io/resource 
           (str "fixtures/" file-name))))

(def yesterday (slurp-fixture "yesterday.txt"))

(def doremi-script-parser  
  (insta/parser
    (slurp (io/resource "doremiscript.ebnf"))))

(defn run-through-parser[txt]
  (doremi-script-parser txt))

(defn pp-to-json[x]
  "For debugging, pretty print json output. Not useable"
  (json/pprint :key-fn json-key-fn)) 

(defn my-pp-json[x]
  "very primitive json pretty-printer. Changes dq,dq => dq,newline,dq "
  (clojure.string/replace x "\",\"" "\",\n\""))

(defn- my-to-json[x]
  "returns json/text version of parse tree. It is a string"
  (my-pp-json (json/write-str x :key-fn json-key-fn)))


(defn -main[& args]
  (let [txt (slurp *in*)]
    (println (my-to-json (transform-parse-tree (run-through-parser txt) txt)))))

