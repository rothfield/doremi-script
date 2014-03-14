(ns doremi_script_clojure.test-helper
	(:require [clojure.test :refer :all ]
           [doremi_script_clojure.core :refer [doremi-script-parser]]
		[clojure.pprint :refer :all ]
		[instaparse.core :as insta]
		))
  (use 'clojure.stacktrace)



(defn trunc_aux [txt,len]
	(if (<  (.length txt) len)
		txt
		(str (.substring txt 0 len)  "...")
		))

(defn trunc
	""
	([x] (trunc_aux x 20))
	([x y] (trunc_aux x y)))

(defn my-raise[x]
  (pprint x)
  (throw (Exception. (str x))))



(defn good-parse
	; TODO: refactor?  
	"Returns true if parse succeded and the list of expected values are in the 
	flattened parse tree"
	[txt start expected]
	(println "Testing " (trunc txt)  "  with start " start " and expected " expected)
	(let [ result 
		(doremi-script-parser txt :total false
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

(def my-tree (doremi-script-parser  "S- --"))


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

;;(pprint my-transform)
(pprint (doremi-script-parser "    \nauthor:john\ntitle:untitled\n\n    RG 1.___\n1) <SS#S> SS :|\nhe-llo"))