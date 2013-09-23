(ns doremi_script_clojure.test-helper
	(:require [clojure.test :refer :all ]
		[clojure.pprint :refer :all ]
		[instaparse.core :as insta]
		))
  (use 'clojure.stacktrace)

(defn slurp-fixture [file-name]
	(slurp (clojure.java.io/resource 
		(str "resources/fixtures/" file-name))))

(defn get-parser2[]
	(insta/parser
		(slurp (clojure.java.io/resource "resources/doremiscript.ebnf")))

(defn pst[]
   (print-stack-trace *e)
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

(defn my-raise[x]
  (pprint x)
  (throw (Exception. (str x))))



