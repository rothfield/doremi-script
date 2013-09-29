(ns doremi_script_clojure.test
  (:require	
    [doremi_script_clojure.core :reload true :refer [run-through-parser]]
    [doremi_script_clojure.semantic_analyzer :reload true :refer [transform-parse-tree]]
    [clojure.pprint :refer [pprint]] 
    [clojure.java.io]
    [instaparse.core :as insta]
    ))
(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  (use 'doremi_script_clojure.test :reload) (ns doremi_script_clojure.test) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
;  (pst)
  )

(defn my-test[txt]
  (transform-parse-tree (run-through-parser txt) txt))

(def x "   Fm7\n   +\n1) S\n   Hi")
(pprint (my-test x))
;;(pprint (run-through-parser x))

