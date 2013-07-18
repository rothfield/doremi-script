(ns doremi_script_clojure.test-helper
	(:require [clojure.test :refer :all ]
		[clojure.pprint :refer :all ]
		[instaparse.core :as insta]
		))

(defn slurp-fixture [file-name]
	(slurp (clojure.java.io/resource 
		(str "resources/fixtures/" file-name))))

(def get-parser2  
	(insta/parser
		(slurp (clojure.java.io/resource "resources/doremiscript.ebnf")))
	)

(defn my-get-parser [] 
	(insta/parser
		(slurp (clojure.java.io/resource "resources/doremiscript.ebnf")))
	)



(defn trunc_aux [txt,len]
	(if (<  (.length txt) len)
		txt
		(str (.substring txt 0 len)  "...")
		))

(defn trunc
	""
	([x] (trunc_aux x 20))
	([x y] (trunc_aux x y)))




(defn good-parse
	; TODO: refactor?  
	"Returns true if parse succeded and the list of expected values are in the 
	flattened parse tree"
	[txt start expected]
	(println "Testing " (trunc txt)  "  with start " start " and expected " expected)
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

;(def my-tree ((my-get-parser) (slurp-fixture "yesterday.txt")))
(def my-tree ((my-get-parser)  "S- --"))


(defn transform-line [& args]
	(println "transform-line")
	(pprint args)
	[:SARGAM_LINE 3]
	)

(defn my-transform [] 
	(print "tree is\n")
	;(pprint my-tree)
	;(pprint	
		(insta/transform 
			{:SARGAM_LINE transform-line}
			my-tree)
	;	)
;	(insta/transform {:SARGAM_LINE (fn [& x] [:SARGAM_MUSICAL_CHAR x (insta/span (first x))])}
	;	my-tree))

)

(pprint my-transform)
