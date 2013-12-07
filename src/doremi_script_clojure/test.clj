(ns doremi_script_clojure.test
  (require	
    [doremi_script_clojure.core :reload true :refer [doremi-script-text->parsed-doremi-script run-through-parser yesterday slurp-fixture]]
    [doremi_script_clojure.semantic_analyzer :reload true :refer [transform-parse-tree]]
    [clojure.pprint :refer [pprint]] 
    [clojure.data.json :as json]
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


(defn- my-seq2[x]
  (tree-seq (fn branch[node](or (map? node) (vector? node)))
            (fn children[node]
              (or (:items node) (:lines node) (:attributes node)
                (:attributes node) node))
            x))

(defn my-test[txt]
  (println "txt is")
  (pprint txt)
  (let [

        parse-tree (time (run-through-parser txt)) 
        z (with-out-str (pprint parse-tree))
        result (with-out-str (pprint (time (transform-parse-tree parse-tree txt))))
        ]
    (println "Source:\n" txt "\n\nParse tree:\n" z "\n\njson:\n" result)
    (spit "tmp.txt" (str z
                         result))))

;;(my-test "(SRG-\n.\nhe-llo john")
;; (my-test "(PG) | S G P | S\nOh say can you see")
;;(my-test ".\nS -")
;; (my-test "R\n    .\n| S")
;;(my-test (slurp-fixture "auto_assign_syllables_in_line.txt"))
;;(my-test "| S R\nHello")
;;(my-test (slurp-fixture "ornament_after.txt"))
;; (my-test (slurp-fixture "ornament_after_with_octave.txt"))
;;(my-test (slurp-fixture "problem.txt"))
;;(my-test (slurp-fixture "yesterday.txt"))
;; (pprint (run-thrnough-parser  (slurp-fixture "ending.txt")))
;;(pprint (my-test " RS\nG"))

