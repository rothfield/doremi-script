(ns doremi_script_clojure.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [doremi_script_clojure.core :refer :all]
            [clojure.walk :refer :all]
            [instaparse.core :as insta] 
            ))

(defn parse-succeeded? [text-to-parse starting-production expected]
  ; Returns true if parse succeeded, false otherwise
  (let [result (get-parser text-to-parse :start starting-production)]
         (if (and (not (insta/failure? result)) (not= result expected))
      (println "Result was " result "; Expected: " expected)
      )
    (and (not (insta/failure? result)) (= result expected))
    )
  )

(defn parse-succeeded3
  "Returns true if parse succeded and the list of expected values are in the 
  flattened parse tree"
  [txt start expected]
  ;(println "body1?")
  (let [ result (get-parser txt :total false :start start)
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
        (every? #(true? %) (map my-helper expected))
        )
      )))


(deftest sargam-pitch-can-include-left-slur
         (is (true? (parse-succeeded3 "SS | z" :SARGAM_PITCH  [:BEGIN_SLUR :S])))
         (is (true? (parse-succeeded3 "(S" :SARGAM_PITCH  [:BEGIN_SLUR :S :r])))
         )




(defn parse-succeeded2? [text-to-parse starting-production expected]
  ; Returns true if parse succeeded, false otherwise
  (let [result (get-parser text-to-parse :start starting-production)]
    (if (and (not (insta/failure? result)) (not= result expected))
      (println "Result was " result "; Expected: " expected)
      )
    (and (not (insta/failure? result)) (= result expected))
    )
  )

(defn parse-passes? [text-to-parse starting-production ]
  ; Returns true if parse succeeded, false otherwise
  (let [result (get-parser text-to-parse :start starting-production)]
    (if (insta/failure? result)
      (          false
        result
        )
      )
    ))

  (defn parse-fails?  [text-to-parse starting-production]
    (let [result (get-parser text-to-parse :start starting-production)]
   composition-with-attributes-lyrics-and-sargam-section   (instcomposition-with-attributes-lyrics-and-sargam-sectiona/failure? result) 
      )
    )

 
  (deftest sargamNotes 
           (let [txt "SrRgGmMPdDnNSbS#R#G#MP#D#N#"
                 result (get-parser txt :start :BEAT)
                 flattened (flatten result)]
             ; (println txt result)
             ;(println "flattend is " flattened)
             (is (some #(= :S %)  flattened))
             (is (some #(= :r %)  flattened))
             (is (= 1 (count (filter #(= :BEAT %) flattened))) "beat count off" )
             )
           )
  ; (is (some #(= :LINE_NUMBER %)  flattened))
  (deftest composition 
           (let [
                 txt "foo:bar\ncat:dog\n\n | S R G R |\n" 
                 result (get-parser txt :total true :start :COMPOSITION)
                 flattened (flatten result)]
             (println "parsing " txt)
             (println result)
             ;(println flattened)
             (is (some #(= "foo" %)  flattened))
             (is (some #(= "bar" %)  flattened))
             (is (some #(= "cat" %)  flattened))
             (is (some #(= "dog" %)  flattened))
             )
           )
  (deftest syllable
           (let [txt "foo"
                 result (get-parser txt :total false :start :SYLLABLE)
                 flattened (flatten result)]
             ;(pprint result)
             (is (some #(= :SYLLABLE %)  flattened))
             (is (some #(= "foo" %)  flattened))
             ))
  (deftest lyrics-section
           (let [txt "  Georgia georgia\nNo peace I find ba-by"
                 result (get-parser txt :total true :start :LYRICS_SECTION)
                 flattened (flatten result)]
             ;(pprint result)
             (is (some #(= :SYLLABLE %)  flattened))
             (is (some #(= "ba" %)  flattened))
             ))
  (deftest  upper-octave-line-item
           (let [txt "."
                 result (get-parser txt :total true :start :UPPER_OCTAVE_LINE_ITEM)
                 flattened (flatten result)]
             (pprint result)
             (is (some #(= "." %)  flattened))
             ))
  (deftest  upper-octave-line
           (let [txt ". + 0 2 3"
                 result (get-parser txt :total true :start :UPPER_OCTAVE_LINE)
                 flattened (flatten result)]
             (pprint result)
             (is (some #(= "+" %)  flattened))
             (is (some #(= "2" %)  flattened))
             ))
  (deftest lyrics-section
           (let [txt "  Georgia georgia\nNo peace I find ba-by"
                 result (get-parser txt :total true :start :LYRICS_SECTION)
                 flattened (flatten result)]
             ;(pprint result)
             (is (some #(= :SYLLABLE %)  flattened))
             (is (some #(= "ba" %)  flattened))
             ))


  (deftest composition-with-attributes-lyrics-and-sargam-section
           (let [txt 
                "Title:Georgia
                 Author:Hoargy Carmichael

                 Georgia Georgia
                 No peace I find

                 +
                 | GP - -  - | GR - -  | - G D G | R - - SR |
                 Geor-gia   

                 .
                 | G P N - | S


                 "
                 result (get-parser txt :total true :start :COMPOSITION)
                 flattened (flatten result)]
             (println txt)
             (pprint result)
             (is (some #(= "Georgia" %)  flattened))
             ))




(deftest lyrics-line
         (let [txt "  he-llo  dolly"
               result (get-parser txt :total true :start :LYRICS_LINE)
               flattened (flatten result)]
           (pprint result)
           (is (some #(= :SYLLABLE %)  flattened))
           (is (some #(= "he" %)  flattened))
           (is (some #(= "llo" %)  flattened))
           (is (some #(= "dolly" %)  flattened))
           ))
(deftest syllable-with-hyphen
         (let [txt "foo-"
               result (get-parser txt :total true :start :SYLLABLE)
               flattened (flatten result)]
           (pprint result)
           (is (some #(= :SYLLABLE %)  flattened))
           (is (some #(= "foo-" %)  flattened))
           ))
(deftest composition-attributes-and-sargam-sections
         (let [txt "foo:bar  \ndog:cat    \n\n | S R G | "
               result (get-parser txt :total false :start :COMPOSITION)
               flattened (flatten result)]
           (pprint result)
           (is (some #(= :G %)  flattened))
           ))
(deftest composition-two-attribute-sections
         (let [txt "foo:bar  \ndog:cat    \n\nhat:bat"
               result (get-parser txt :total true :start :COMPOSITION)
               flattened (flatten result)]
           (pprint result)
           (is (some #(= :ATTRIBUTE_LINE %)  flattened))
           ))

(deftest composition-one-attribute-no-eol
         (let [result (get-parser "foo:bar   " :start :COMPOSITION)
               flattened (flatten result)]
           (pprint result)
           ;(println flattened)
           (is (some #(= :ATTRIBUTE_LINE %)  flattened))
           )
         )
(deftest section-two-attributes-no-eol
         (let [result (get-parser "foo:bar  \ndog:cat    " :total true :start :SECTION)
               flattened (flatten result)]
           (pprint result)
           ;(println flattened)
           (is (some #(= :ATTRIBUTE_SECTION %)  flattened))
           (is (some #(= :SECTION %)  flattened))
           )
         )

(deftest sargam-section 
         (let [result (get-parser "  | S R G - | " :start :SARGAM_SECTION)
               flattened (flatten result)]
           ;(println result)
           ;(println flattened)
           (is (some #(= :S %)  flattened))
           (is (some #(= :MEASURE %)  flattened))
           (is (some #(= :R %)  flattened))
           (is (some #(= :G %)  flattened))
           )
         )


(deftest attribute-section 
         (let [result (get-parser "foo:bar\ncat:dog" :start :ATTRIBUTE_SECTION)
               flattened (flatten result)]
           ;(println result)
           ;(println flattened)
           (is (some #(= "foo" %)  flattened))
           (is (some #(= "bar" %)  flattened))
           (is (some #(= "cat" %)  flattened))
           (is (some #(= "dog" %)  flattened))
           )
         )

(deftest attributes
         (let [result (get-parser "foo:bar\n" :start :ATTRIBUTE_LINE)
               flattened (flatten result)]
           ;(println result)
           ;(println flattened)
           (is (some #(= "foo" %)  flattened))
           (is (some #(= "bar" %)  flattened))
           )
         )

(deftest line-number
         (let [result (get-parser "1)" :start :LINE_NUMBER)
               flattened (flatten result)]
           ;(println flattened)
           (is (some #(= "1" %)  flattened))
           (is (some #(= :LINE_NUMBER %)  flattened))
           )
         )
(deftest sargam-line-2
         (let [result (get-parser "1) | S- Rgm |" :start :SARGAM_LINE)
               flattened (flatten result)]
           ;(println result)
           (is (some #(= :S %)  flattened))
           (is (some #(= :R %)  flattened))
           (is (= 2 (count (filter #(= :BEAT %) flattened))) "beat count off" )
           )
         )
;;;;
(deftest sargam-line-simple
         (let [result (get-parser "| S R |\n" :start :SARGAM_LINE)
               flattened (flatten result)]
           (is (some #(= :S %)  flattened))
           (is (some #(= :R %)  flattened))
           )
         )




(deftest beat-can-be-delimited-with-angle-brackets
         (let [result (parse-passes?  "<S>" :BEAT_DELIMITED)]
           ;     (println result)
           (is (not= nil 
                     (parse-passes?  "<S>" :BEAT_DELIMITED))
               "")

           (is (some #(= :S %)  (flatten result)))
           ))

(deftest beat-can-be-delimited-with-angle-brackets-more-than-one-note-with-spaces
         (let [result (parse-passes?  "<S r>" :BEAT_DELIMITED)]
           ;(println result)
           (is (not= nil 
                     result)
               "")
           (is (some #(= :S %)  (flatten result)))
           (is (some #(= :r %)  (flatten result)))
           ))




(deftest sargam-pitch-can-include-right-slur
         (is (true? 
               (parse-succeeded?  "S)" :SARGAM_PITCH [:SARGAM_PITCH [:S] [:END_SLUR]] ))
             ""))

;                  (println (get-parser "(S" :start :SARGAM_PITCH))
;                  (println (get-parser "S)" :start :SARGAM_PITCH))

(deftest parses-double-barline
         (is (true? 
               (parse-succeeded?  "||" :DOUBLE_BARLINE [:DOUBLE_BARLINE]))
             ""))

(deftest doesnt-parse-single-barline-when-it-sees-double-barline
         (is (true? (parse-fails?  "||" :SINGLE_BARLINE))
             ""))

(deftest test-left-repeat
         (is (true? 
               (parse-succeeded?  "|:" :LEFT_REPEAT[:LEFT_REPEAT]))
             ""))

(deftest test-final-barline
         (is (true? 
               (parse-succeeded?  "|]" :FINAL_BARLINE[:FINAL_BARLINE]))
             ""))

(deftest test-final-barline
         (is (true? 
               (parse-succeeded?  "[|" :REVERSE_FINAL_BARLINE[:REVERSE_FINAL_BARLINE]))
             ""))


(deftest test-right-repeat
         (is (true? 
               (parse-succeeded?  ":|" :RIGHT_REPEAT[:RIGHT_REPEAT]))
             ""))

(deftest test-dash
         (is (true? (parse-succeeded2?  "-" :DASH [:DASH])) ""))

(deftest test-repeat-symbol
  (is (true? (parse-succeeded?  "%" :REPEAT_SYMBOL [:REPEAT_SYMBOL])) ""))



