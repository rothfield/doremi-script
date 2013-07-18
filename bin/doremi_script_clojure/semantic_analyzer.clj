(ns doremi_script_clojure.semantic-analyzer
	"Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
	(:require	[doremi_script_clojure.test-helper :refer :all ]
						[doremi_script_clojure.semantic-analyzer :refer :all ]
				    [clojure.pprint :refer :all ]
				    [instaparse.core :as insta]
      ))

(defn- is-line? [x]
	(if (not(vector? x))
		false
		(or (= :SARGAM_LINE (first x))
				(= :LYRICS_LINE (first x))
				(= :UPPER_OCTAVE_LINE (first x))
				(= :LOWER_OCTAVE_LINE (first x))
				)
		)
	)

(defn line-positions [line]
    (let [start (meta(first line))
          (:instaparse.gll/start-index (meta z))
          ]
      (print "start is " start)
  ))


(defn position-map-for-sargam-section
	"I don't do a whole lot."
	[x]
  (print (meta (first x)))
	(let [ lines (filter is-line? x)
       ]
   (print lines)
   )
	)

(defn test-line-positions[]
      (let [section (get-parser2  "G- P-/nhe-llo" :start :SARGAM_SECTION)
            ]
        (println "section is ")
        (pprint section)
      ;  (println "line_positions is")
       ; (pprint (line-positions line))
        ))

(test-line-positions)