(ns doremi_script_clojure.core
  (:require	
    ;;  [doremi_script_clojure.test-helper :refer :all ]
    [clojure.java.io :as io]
    [doremi_script_clojure.semantic_analyzer :refer [main5]]
    [clojure.pprint :refer [pprint]] 
    )
  (:gen-class :main true))

(def get-parser22 
  ;(insta/parser
  ;  (slurp (clojure.java.io/resource "doremi_script_clojure/doremiscript.ebnf")))
  )

(defn -main[& args]
  (pprint (main5 "S")))
