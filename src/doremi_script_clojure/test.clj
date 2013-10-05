(ns doremi_script_clojure.test
  (:require	
    [doremi_script_clojure.core :reload true :refer [run-through-parser yesterday slurp-fixture]]
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
  (let [result (with-out-str (pprint (transform-parse-tree (run-through-parser txt) txt)))]
    (println result)
    (spit "tmp.txt" result)))


(def x "   Fm7\n   +\n1) S\n   Hi")
;;(my-test yesterday)
;;(my-test "S - -")
(my-test (slurp-fixture "ending.txt"))
;; (pprint (run-through-parser  (slurp-fixture "ending.txt")))
;;(my-test "| S R\nHe-llo")

