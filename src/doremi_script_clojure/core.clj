(ns doremi_script_clojure.core
  (:gen-class)
  (:require	
   [doremi_script_clojure.semantic_analyzer :refer [doremi-script-parse doremi-script-to-json]]

    [clojure.java.io]
    [clojure.pprint :refer [pprint]] 
    ))

(defn -main[& args]
 (print (doremi-script-to-json (slurp *in*)))
  )


