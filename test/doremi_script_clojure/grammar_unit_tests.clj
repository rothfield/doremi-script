(ns doremi_script_clojure.grammar-unit-tests
	(:require [clojure.test :refer :all ]
						[clojure.pprint :refer :all ]
						[doremi_script_clojure.test-helper :refer :all ]
						[clojure.walk :refer :all ]
						[instaparse.core :as insta]
						))

(deftest lower-octave-line
				 (is (good-parse ". : _" :LOWER_OCTAVE_LINE [:LOWER_OCTAVE_DOT :KOMMAL_INDICATOR ])))


(deftest dot
				 (is (good-parse "." :DOT [:DOT "."]))
				 (is (good-parse "*" :DOT [:DOT "*"]))
				 (is (good-parse "*" :LOWER_OCTAVE_DOT [:DOT "*"]))
				 (is (good-parse "." :LOWER_OCTAVE_DOT [:DOT "."]))
				 (is (good-parse "*" :UPPER_OCTAVE_DOT [:DOT "*"]))
				 (is (good-parse "." :UPPER_OCTAVE_DOT [:DOT "."]))
				 )

;;;; Ornaments
(deftest sargam-ornament
				 (is (good-parse "PMDP" :SARGAM_ORNAMENT  [:P :D]))
				 ;;(is (good-parse "PM-DP" :SARGAM_ORNAMENT  [:DASH]))
				 )
(deftest delimited_sargam_ornament
				 (is (good-parse "<SNRS>" :DELIMITED_SARGAM_ORNAMENT [ :S :N :R :S]))
			   (is (good-parse "<S>" :DELIMITED_SARGAM_ORNAMENT [:S]))
 				 )
(deftest delimited_sargam_ornament_is_an_ornament
				 (is (good-parse "<SNRS>" :SARGAM_ORNAMENT [:SARGAM_ORNAMENT :S :N :R :S]))
			   (is (good-parse "<S>" :SARGAM_ORNAMENT [:SARGAM_ORNAMENT :S]))
 				 )

(deftest undelimited_sargam_ornament
				 (is (good-parse "SNRS" :UNDELIMITED_SARGAM_ORNAMENT [ :S :N :R :S]))
			   (is (good-parse "S" :UNDELIMITED_SARGAM_ORNAMENT [:S]))
 				 )
(deftest undelimited_sargam_ornament_is_an_ornament
				 (is (good-parse "SNRS" :SARGAM_ORNAMENT [:UNDELIMITED_SARGAM_ORNAMENT :S :N :R :S]))
			   (is (good-parse "S" :SARGAM_ORNAMENT [:UNDELIMITED_SARGAM_ORNAMENT :S]))
 				 )
(deftest upper_octave_line_can_contain_sargam_ornaments
			 (is (good-parse "SNRS" :UPPER_OCTAVE_LINE [:SARGAM_ORNAMENT :S :N :R :S]))
			   (is (good-parse "<rg>" :UPPER_OCTAVE_LINE [:SARGAM_ORNAMENT :r :g])))

;;; Pitches
(deftest sargam-pitch-can-include-left-slur
				 (is (good-parse "(S" :SARGAM_PITCH [:BEGIN_SLUR :S ]))
				 )
(deftest lower-octave-dot
				 (is (good-parse "." :LOWER_OCTAVE_DOT ["."])))

(deftest kommal_underscore
				 (is (good-parse "_" :KOMMAL_INDICATOR ["_"])))



(deftest lower-lower-octave
				 (is (good-parse ":" :LOWER_LOWER_OCTAVE_SYMBOL [":"])))


(defn parse-fails? [text-to-parse starting-production]
	(let [result ((my-get-parser) text-to-parse :start starting-production)]
		(insta/failure? result)
		)
	)

(deftest sargam-notes
				 (let [txt "SrRgGmMPdDnNSbS#R#G#MP#D#N#"]
					 (is (good-parse txt :BEAT [:S :Sb :N :Nsharp])))
				 )

(deftest composition
				 (let 
					 [txt "foo:bar\ncat:dog\n\n | S R G R |\n"]
					 (is (good-parse txt :COMPOSITION ["foo" "bar" "cat" "dog"])))
				 )

(deftest syllable
				 (is (good-parse "foo" :SYLLABLE  ["foo"])))


(deftest lyrics-section1
				 (let [txt "  Georgia georgia\nNo peace I find ba-by"]
					 (is (good-parse txt :LYRICS_SECTION   [:HYPHENATED_SYLLABLE ])))
				 )

(deftest upper-octave-line-item
					 (is (good-parse "." :UPPER_OCTAVE_LINE_ITEM   ["." :DOT])))

(deftest upper-octave-line
				 (let [txt ". + 0 2 3"]
					 (is (good-parse txt :UPPER_OCTAVE_LINE   ["+" "2" :TALA]))))



(deftest alternate_ending
				 (is (good-parse "1._____" :ALTERNATE_ENDING_INDICATOR  []))
				 (is (good-parse "3_____" :ALTERNATE_ENDING_INDICATOR  [])))

(deftest syllable-with-hyphen
				 (is (good-parse "foo-   bar baz-" :LYRICS_LINE ["foo-"])))


(deftest composition-attributes-and-sargam-sections
				 (let [txt "foo:bar  \ndog:cat    \n\n | S R G | "]
					 (is (good-parse txt :COMPOSITION [:KEY "foo" :VALUE "bar" :G]))))

(deftest composition-two-attribute-sections
				 (let [txt "foo:bar  \ndog:cat    \n\nhat:bat"]
					 (is (good-parse txt :COMPOSITION [:ATTRIBUTE_LINE "foo" "hat"]))
					 ))

(deftest composition-one-attribute-no-eol
				 (let [txt "foo:bar   "]
					 (is (good-parse txt :COMPOSITION [:ATTRIBUTE_LINE "foo" "bar"]))
					 ))

(deftest section-two-attributes-no-eol
				 (let [txt "foo:bar  \ndog:cat    "]
					 (is (good-parse txt
													 :COMPOSITION 
													 [:ATTRIBUTE_SECTION :SECTION "foo" "bar"]))))

(deftest sargam-section
					 (is (good-parse "  | S R G - | "
													 :SARGAM_SECTION
													 [:MEASURE :S :R :G])))
(deftest attribute-line
				 (is (good-parse "foo:bar" :ATTRIBUTE_LINE ["foo" "bar" ]))
				 (is (good-parse "foo : bar" :ATTRIBUTE_LINE ["foo" "bar" ]))
				 (is (good-parse "foo : bar    " :ATTRIBUTE_LINE ["foo" "bar" ]))
				 )

(deftest attribute-section
				 (is (good-parse "foo:bar\ncat:dog" :ATTRIBUTE_SECTION ["foo" "bar" "cat" "dog" ]))
				 )

(deftest attributes
				 (is (good-parse "foo:bar" :ATTRIBUTE_LINE ["foo" "bar"])))

(deftest line-number
				 (is (good-parse "1)" :LINE_NUMBER ["1"])))

(deftest sargam-line-2
				 (is (good-parse "1) S- Rgm | " :SARGAM_LINE [:S])))

(deftest sargam-line-simple
				 (is (good-parse "| S R |" :SARGAM_LINE [:S :R :BEAT ])))


(deftest beat-can-be-delimited-with-angle-brackets
				 (is (good-parse "<S>" :BEAT_DELIMITED [:S :BEAT_DELIMITED ])))

(deftest beat-can-be-delimited-with-angle-brackets-more-than-one-note-with-spaces
				 (is (good-parse "<S r>" :BEAT_DELIMITED [:S :r :BEAT_DELIMITED ])))

(deftest sargam-pitch-can-include-right-slur
				 (is (good-parse "S)" :SARGAM_PITCH [:S :END_SLUR ])))

(deftest parses-double-barline
				 (is (good-parse "||" :DOUBLE_BARLINE [:DOUBLE_BARLINE ])))

(deftest doesnt-parse-single-barline-when-it-sees-double-barline
				 (is (parse-fails? "||" :SINGLE_BARLINE ))
				 "")

(deftest test-left-repeat
				 (is (good-parse "|:" :LEFT_REPEAT [:LEFT_REPEAT ])))

(deftest test-final-barline
				 (is (good-parse "|]" :FINAL_BARLINE [:FINAL_BARLINE ])))

(deftest test-reverse-final-barline
				 (is (good-parse "[|" :REVERSE_FINAL_BARLINE [:REVERSE_FINAL_BARLINE ])))


(deftest test-right-repeat
				 (is (good-parse ":|" :RIGHT_REPEAT [:RIGHT_REPEAT ])))


(deftest test-tala
				 (let [start :TALA
										 items "+2034567"]
					 (is (good-parse "+" :TALA ["+"]))
					 (is (good-parse "0" :TALA ["0"]))
					 (is (good-parse "2" :TALA ["2"]))
					 )
				 )

(deftest chord
				 (is (good-parse "Fm7" :CHORD ["Fm7"]))
				 (is (good-parse "G#aug7" :CHORD ["G#aug7"]))
				 (is (good-parse "G" :CHORD ["G"]))
				 (is (good-parse "Em7" :CHORD ["Em7"]))
				 (is (good-parse "A7" :CHORD ["A7"]))
				 (is (good-parse "Dmi/C" :CHORD ["Dmi/C"]))
				 (is (good-parse "C/E" :CHORD ["C/E"]))
				 (is (good-parse "Bb" :CHORD ["Bb"]))
				 (is (good-parse "C7" :CHORD ["C7"]))
				 (is (good-parse "A11" :CHORD []))
     )

(deftest dash
				 (is (good-parse "-" :DASH [:DASH ])))

(deftest test-repeat-symbol
				 (is (good-parse "%" :REPEAT_SYMBOL [:REPEAT_SYMBOL ])) "")

(defn test-some
	[]
	(sargam-pitch-can-include-left-slur)
	(dash)
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
;(dash)
;(sargam-notes)
;(composition)
;(lyrics-section1)
;(upper-octave-line)
;;(chord)
