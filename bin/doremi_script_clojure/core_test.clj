(ns doremi_script_clojure.core-test
	(:require [clojure.test :refer :all ]
		[clojure.pprint :refer :all ]
		[doremi_script_clojure.core :refer :all ]
		[clojure.walk :refer :all ]
		[instaparse.core :as insta]
		))

(def my-get-parser 
  (insta/parser
    (slurp (clojure.java.io/resource "doremi_script_clojure/doremiscript.ebnf")))
  )

(defn parse-succeeded3
	"Returns true if parse succeded and the list of expected values are in the 
	flattened parse tree"
	[txt start expected]
	(println "Testing " txt "with start " start " and expected " expected)
	(let [ result 
		(my-get-parser txt :total false
			:start start)
		flattened (flatten result)]
		(letfn [(my-helper [x]
			(let [found (some #(= x %) flattened)]
				(if (nil? found) 
					(do
						(println "Parsing " txt " with start " start "; Didn't find " x " in flattened result ") 
						(pprint result)
						)
					found) 
				))]
		(if (insta/failure? result)
			(do (println "Parsing " txt " with start " start "; Failed. Result is:")
				(pprint result)
				false
				)
			(do
				(pprint result)
				(every? #(true? %) (map my-helper expected))
				))
		)))

(deftest yesterday_no_chords
	(let [txt (slurp 
		(clojure.java.io/resource 
			"doremi_script_clojure/fixtures/yesterday_no_chords.doremiscript.txt"))]
	(pprint txt)
	(is (true? (parse-succeeded3 txt :COMPOSITION ["Yesterday"])))
	))

(deftest composition-with-attributes-lyrics-and-sargam-section
	(let [txt (slurp 
		(clojure.java.io/resource 
			"doremi_script_clojure/fixtures/georgia.doremiscript.txt"))]
	(pprint txt)
	(is (true? (parse-succeeded3 txt :COMPOSITION ["Georgia",:UPPER_OCTAVE_DOT,".",:TALA])))
	))



(deftest lower-octave-line
	(is (true? (parse-succeeded3 ". : _" :LOWER_OCTAVE_LINE [:LOWER_OCTAVE_DOT :KOMMAL_INDICATOR ])))
		)

(deftest dot
	(is (true? (parse-succeeded3 "." :DOT [:DOT "."])))
	(is (true? (parse-succeeded3 "*" :DOT [:DOT "*"])))
	(is (true? (parse-succeeded3 "*" :LOWER_OCTAVE_DOT [:DOT "*"])))
	(is (true? (parse-succeeded3 "." :LOWER_OCTAVE_DOT [:DOT "."])))
	(is (true? (parse-succeeded3 "*" :UPPER_OCTAVE_DOT [:DOT "*"])))
	(is (true? (parse-succeeded3 "." :UPPER_OCTAVE_DOT [:DOT "."])))
		)


(deftest sargam-pitch-can-include-left-slur
	(is (true? (parse-succeeded3 "(S" :SARGAM_PITCH [:BEGIN_SLUR :S ])))
		)
(deftest lower-octave-dot
	(is (true? (parse-succeeded3 "." :LOWER_OCTAVE_DOT ["."])))
		)
(deftest kommal_underscore
	(is (true? (parse-succeeded3 "_" :KOMMAL_INDICATOR ["_"])))
		)


(deftest lower-lower-octave
	(is (true? (parse-succeeded3 ":" :LOWER_LOWER_OCTAVE_SYMBOL [":"])))
		)

(defn parse-fails? [text-to-parse starting-production]
	(let [result (my-get-parser text-to-parse :start starting-production)]
		(insta/failure? result)
		)
	)

(deftest sargamNotes
	(let [txt "SrRgGmMPdDnNSbS#R#G#MP#D#N#"
		result (my-get-parser txt :start :BEAT )
		flattened (flatten result)]
    ; (println txt result)
    ;(println "flattend is " flattened)
    (is (some #(= :S %) flattened))
    (is (some #(= :r %) flattened))
    (is (= 1 (count (filter #(= :BEAT %) flattened))) "beat count off")
    )
	)
; (is (some #(= :LINE_NUMBER %)  flattened))
(deftest composition
	(let [
		txt "foo:bar\ncat:dog\n\n | S R G R |\n"
		result (my-get-parser txt :total true :start :COMPOSITION )
		flattened (flatten result)]
		(println "parsing " txt)
		(println result)
    ;(println flattened)
    (is (some #(= "foo" %) flattened))
    (is (some #(= "bar" %) flattened))
    (is (some #(= "cat" %) flattened))
    (is (some #(= "dog" %) flattened))
    )
	)
(deftest syllable
	(let [txt "foo"
		result (my-get-parser txt :total false :start :SYLLABLE )
		flattened (flatten result)]
    ;(pprint result)
    (is (some #(= :SYLLABLE %) flattened))
    (is (some #(= "foo" %) flattened))
    ))
(deftest lyrics-section
	(let [txt "  Georgia georgia\nNo peace I find ba-by"
		result (my-get-parser txt :total true :start :LYRICS_SECTION )
		flattened (flatten result)]
    ;(pprint result)
    (is (some #(= :SYLLABLE %) flattened))
    (is (some #(= "ba" %) flattened))
    ))


(deftest upper-octave-line-item
	(let [txt "."
		result (my-get-parser txt :total true :start :UPPER_OCTAVE_LINE_ITEM )
		flattened (flatten result)]
		(pprint result)
		(is (some #(= "." %) flattened))
		))
(deftest upper-octave-line
	(let [txt ". + 0 2 3"
		result (my-get-parser txt :total true :start :UPPER_OCTAVE_LINE )
		flattened (flatten result)]
		(pprint result)
		(is (some #(= "+" %) flattened))
		(is (some #(= "2" %) flattened))
		))
(deftest lyrics-section
	(let [txt "  Georgia georgia\nNo peace I find ba-by"
		result (my-get-parser txt :total true :start :LYRICS_SECTION )
		flattened (flatten result)]
    ;(pprint result)
    (is (some #(= :SYLLABLE %) flattened))
    (is (some #(= "ba" %) flattened))
    ))






(deftest lyrics-line
	(let [txt "  he-llo  dolly"
		result (my-get-parser txt :total true :start :LYRICS_LINE )
		flattened (flatten result)]
		(pprint result)
		(is (some #(= :SYLLABLE %) flattened))
		(is (some #(= "he" %) flattened))
		(is (some #(= "llo" %) flattened))
		(is (some #(= "dolly" %) flattened))
		))

(deftest sargam-ornament
	(is (true? (parse-succeeded3 "PMDP" :SARGAM_ORNAMENT  [:P :D]))))
(deftest alternate_ending
	(is (true? (parse-succeeded3 "1._____" :ALTERNATE_ENDING_INDICATOR  [])))
	(is (true? (parse-succeeded3 "3_____" :ALTERNATE_ENDING_INDICATOR  [])))
 )
  

(deftest syllable-with-hyphen
	(is (true? (parse-succeeded3 "foo-   bar baz-" :LYRICS_LINE ["foo"]))))

(deftest syllable-with-hyphen-bad-case
  ;;; TODO: change EBNF
  (is (true? (parse-succeeded3 "foo-   bar baz---" :LYRICS_LINE ["foo"]))))
(deftest composition-attributes-and-sargam-sections
	(let [txt "foo:bar  \ndog:cat    \n\n | S R G | "
		result (my-get-parser txt :total false :start :COMPOSITION )
		flattened (flatten result)]
		(pprint result)
		(is (some #(= :G %) flattened))
		))
(deftest composition-two-attribute-sections
	(let [txt "foo:bar  \ndog:cat    \n\nhat:bat"
		result (my-get-parser txt :total true :start :COMPOSITION )
		flattened (flatten result)]
		(pprint result)
		(is (some #(= :ATTRIBUTE_LINE %) flattened))
		))

(deftest composition-one-attribute-no-eol
	(let [result (my-get-parser "foo:bar   " :start :COMPOSITION )
		flattened (flatten result)]
		(pprint result)
    ;(println flattened)
    (is (some #(= :ATTRIBUTE_LINE %) flattened))
    )
	)
(deftest section-two-attributes-no-eol
	(let [result (my-get-parser "foo:bar  \ndog:cat    " :total true :start :SECTION )
		flattened (flatten result)]
		(pprint result)
    ;(println flattened)
    (is (some #(= :ATTRIBUTE_SECTION %) flattened))
    (is (some #(= :SECTION %) flattened))
    )
	)

(deftest sargam-section
	(let [result (my-get-parser "  | S R G - | " :start :SARGAM_SECTION )
		flattened (flatten result)]
    ;(println result)
    ;(println flattened)
    (is (some #(= :S %) flattened))
    (is (some #(= :MEASURE %) flattened))
    (is (some #(= :R %) flattened))
    (is (some #(= :G %) flattened))
    )
	)
(deftest attribute-line
	(is (true? (parse-succeeded3 "foo:bar" :ATTRIBUTE_LINE ["foo" "bar" ])))
	(is (true? (parse-succeeded3 "foo : bar" :ATTRIBUTE_LINE ["foo" "bar" ])))
	(is (true? (parse-succeeded3 "foo : bar    " :ATTRIBUTE_LINE ["foo" "bar" ])))
)
(deftest attribute-section
	(let [result (my-get-parser "foo:bar\ncat:dog" :start :ATTRIBUTE_SECTION )
		flattened (flatten result)]
    ;(println result)
    ;(println flattened)
    (is (some #(= "foo" %) flattened))
    (is (some #(= "bar" %) flattened))
    (is (some #(= "cat" %) flattened))
    (is (some #(= "dog" %) flattened))
    )
	)

(deftest attributes
	(is (true? (parse-succeeded3 "foo:bar" :ATTRIBUTE_LINE ["foo" "bar"]))))

(deftest line-number
	(let [result (my-get-parser "1)" :start :LINE_NUMBER )
	flattened (flatten result)]
    ;(println flattened)
    (is (some #(= "1" %) flattened))
    (is (some #(= :LINE_NUMBER %) flattened))
    )
	)
(deftest sargam-line-2
	(let [result (my-get-parser "1) | S- Rgm |" :start :SARGAM_LINE )
	flattened (flatten result)]
    ;(println result)
    (is (some #(= :S %) flattened))
    (is (some #(= :R %) flattened))
    (is (= 2 (count (filter #(= :BEAT %) flattened))) "beat count off")
    )
	)
(deftest sargam-line-simple
	(is (true? (parse-succeeded3 "| S R |" :SARGAM_LINE [:S :R :BEAT ]))))




(deftest beat-can-be-delimited-with-angle-brackets
	(is (true? (parse-succeeded3 "<S>" :BEAT_DELIMITED [:S :BEAT_DELIMITED ]))))

(deftest beat-can-be-delimited-with-angle-brackets-more-than-one-note-with-spaces
	(is (true? (parse-succeeded3 "<S r>" :BEAT_DELIMITED [:S :r :BEAT_DELIMITED ]))))

(deftest sargam-pitch-can-include-right-slur
	(is (true? (parse-succeeded3 "S)" :SARGAM_PITCH [:S :END_SLUR ]))))

(deftest parses-double-barline
	(is (true? (parse-succeeded3 "||" :DOUBLE_BARLINE [:DOUBLE_BARLINE ]))))

(deftest doesnt-parse-single-barline-when-it-sees-double-barline
	(is (true? (parse-fails? "||" :SINGLE_BARLINE ))
		""))

(deftest test-left-repeat
	(is (true? (parse-succeeded3 "|:" :LEFT_REPEAT [:LEFT_REPEAT ]))))

(deftest test-final-barline
	(is (true? (parse-succeeded3 "|]" :FINAL_BARLINE [:FINAL_BARLINE ]))))

(deftest test-reverse-final-barline
	(is (true? (parse-succeeded3 "[|" :REVERSE_FINAL_BARLINE [:REVERSE_FINAL_BARLINE ]))))


(deftest test-right-repeat
	(is (true? (parse-succeeded3 ":|" :RIGHT_REPEAT [:RIGHT_REPEAT ]))))


(deftest test-tala
  (let [start :TALA
        items "+2034567"]
	     (is (true? (parse-succeeded3 "+" :TALA ["+"])))
	     (is (true? (parse-succeeded3 "0" :TALA ["0"])))
	     (is (true? (parse-succeeded3 "2" :TALA ["2"])))
   )
  )
 

(deftest test-dash
	(is (true? (parse-succeeded3 "-" :DASH [:DASH ]))))

(deftest test-repeat-symbol
	(is (true? (parse-succeeded3 "%" :REPEAT_SYMBOL [:REPEAT_SYMBOL ])) ""))

(defn test-some
	[]
	(sargam-pitch-can-include-left-slur)
	(composition-with-attributes-lyrics-and-sargam-section)
	(test-dash)
	(attributes)
	(parses-double-barline)
	(test-final-barline)
	(test-repeat-symbol)
	(syllable-with-hyphen)
	(beat-can-be-delimited-with-angle-brackets-more-than-one-note-with-spaces)
	(sargam-pitch-can-include-right-slur)
	(sargam-line-simple)
	(beat-can-be-delimited-with-angle-brackets)
	)

;(run-tests)
; (composition-with-attributes-lyrics-and-sargam-section)

(println "loaded core-test")
;  (test-some)
;  (sargam-line-simple)
;  (test-tala)
;  	(composition-with-attributes-lyrics-and-sargam-section)
;  (dot)
;  (yesterday_no_chords)
;  (attribute-line)
;  
;(composition-with-attributes-lyrics-and-sargam-section)
;(yesterday_no_chords)
