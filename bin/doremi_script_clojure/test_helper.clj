(ns doremi_script_clojure.test-helper
	(:require [clojure.test :refer :all ]
						[clojure.pprint :refer :all ]
						[instaparse.core :as insta]
						))

(defn slurp-fixture [file-name]
	(slurp (clojure.java.io/resource 
					 (str "doremi_script_clojure/fixtures/" file-name))))


(defn my-get-parser [] 
	(insta/parser
		(slurp (clojure.java.io/resource "doremi_script_clojure/doremiscript.ebnf")))
	)

(defn trunc [txt,len]
	 (if (<  (.length txt) len)
		   txt
			 (str (.substring txt 0 len)  "...")
			 ))

(defn good-parse
	; TODO: refactor?
	"Returns true if parse succeded and the list of expected values are in the 
	flattened parse tree"
	[txt start expected]
	(println "Testing " (trunc txt 20)  "  with start " start " and expected " expected)
	(let [ result 
				 ((my-get-parser) txt :total false
												:start start)
				 flattened (flatten result)]
		(letfn [(my-helper [x]
											 (let [found (some #(= x %) flattened)]
												 (if (nil? found) 
													 (do
														 (println "Parsing " txt " with start " start "; Didn't find " x " in flattened result ") 
														 ;(pprint result)
														 )
													 found) 
												 ))]
			(if (insta/failure? result)
				(do (println "Parse failed: " txt " with start " start)
				;(println "Result is:\n") 	(pprint result)
					false
					)
				(do
					;(pprint result)
					(every? #(true? %) (map my-helper expected))
					))
			)))

