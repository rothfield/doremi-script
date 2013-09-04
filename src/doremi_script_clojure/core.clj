(ns doremi_script_clojure.core
  (:gen-class)
  (:require	
   [doremi_script_clojure.semantic_analyzer :refer [main5]]
    [clojure.java.io]
    [clojure.pprint :refer [pprint]] 
    ))

(defn -main[& args]
   ;(pprint (slurp (clojure.java.io/resource "doremiscript.ebnf")))
 (pprint (main5 (slurp *in*)))
  ;;(pprint "hi")
  )

