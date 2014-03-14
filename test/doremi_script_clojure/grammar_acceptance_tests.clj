(ns doremi_script_clojure.grammar-acceptance-tests
	"Acceptance tests for doremi-script grammar. Start rule will be :COMPOSITION 
	"
	(:require [clojure.test :refer :all ]
						[clojure.pprint :refer :all ]
						[doremi_script_clojure.test-helper :refer :all ]
						[doremi_script_clojure.core :refer [slurp-fixture doremi-script-parser] ]
						[instaparse.core :as insta]))

(deftest composition_with_only_sargam_and_no_eol
				 (is (good-parse "S" :SARGAM_LINE [:S :MEASURE :BEAT "S"])))

 
(deftest test-yesterday
				 (is (good-parse 
							 (slurp-fixture "yesterday.txt")
							 :COMPOSITION ["Yesterday"])))

(deftest yesterday_no_chords
				 (is (good-parse 
							 (slurp-fixture "yesterday_no_chords.doremiscript.txt")
							 :COMPOSITION ["Yesterday"]))
				 )

(deftest composition-with-attributes-lyrics-and-sargam-section
					 (is (good-parse 
							 (slurp-fixture "georgia.doremiscript.txt")
								:COMPOSITION 
								["Georgia",:UPPER_OCTAVE_DOT,".",:TALA]))
					 )




