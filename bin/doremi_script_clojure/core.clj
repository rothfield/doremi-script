(ns doremi_script_clojure.core
              (:require [instaparse.core :as insta])
              )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!")
  1)

(def get-parser22 
  (insta/parser
    (slurp (clojure.java.io/resource "doremi_script_clojure/doremiscript.ebnf")))
  )


