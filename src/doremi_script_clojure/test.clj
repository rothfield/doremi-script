(ns doremi_script_clojure.test
  (require	
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
  (let [
        
        parse-tree (time (run-through-parser txt)) 
        z (with-out-str (pprint parse-tree))
        result (with-out-str (pprint (time (transform-parse-tree parse-tree txt))))
        ]
    (println "Source:\n" txt "\n\nParse tree:\n" z "\n\njson:\n" result)
    (spit "tmp.txt" (str z
                         result))))


(def x "   Fm7\n   +\n1) S\n   Hi")
;; (my-test yesterday)
;;(my-test "(SRG-\n.\nhe-llo john")
;; (my-test "(PG) | S G P | S\nOh say can you see")
;;(my-test ".\nS -")
(my-test (slurp-fixture "b"))
;; (pprint (run-thrnough-parser  (slurp-fixture "ending.txt")))
;;(pprint (my-test " RS\nG"))
;;tt(pprint (my-test "lines: lineshere\nKey: D\nhi:john\nAuthor:me\n\n|S"))
;;(def h (new net.davidashen.text.Hyphenator))
;;(def hyphens  (clojure.java.io/input-stream (clojure.java.io/resource "hyphen.tex")))
;;(.loadTable h hyphens)
;;          (assert (= :sargam_musical_char (:_my_type (second node))))
;;(pprint (.hyphenate h "hello"))
