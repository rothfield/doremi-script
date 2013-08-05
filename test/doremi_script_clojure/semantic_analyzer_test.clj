(ns doremi_script_clojure.semantic-analyzer-test
  (:require [clojure.test :refer :all ]
            [clojure.string]
            [clojure.pprint :refer :all ]
            [doremi_script_clojure.test-helper :refer :all ]
            [doremi_script_clojure.semantic-analyzer :refer :all ]
            [instaparse.core :as insta]
            ))

(defn transform-beat-helper[txt divisions]
  (is (= divisions 
         (:divisions 
           (second 
             (insta/transform { :BEAT transform-beat } 
                              (get-parser2 txt :start :BEAT))))))
  )







(deftest transform-sargam-section-sargam-pitch-should-create-hash-of-values
  " json for old system:
{ my_type: 'pitch',
                      normalized_pitch: 'C',
                      attributes: [], 
                      pitch_source: 'S',
                      source: 'S',
                      column_offset: 0,
                      octave: 0,
                      numerator: 1,
                      denominator: 1,
                      fraction_array: [ { numerator: 1, denominator: 1 } ],
                      column: 2,
                      syllable: 'he-' } 
  "
  (let [ result
        (insta/transform { :SARGAM_SECTION transform-sargam-section } 
                         (get-parser2 "g" :start :SARGAM_SECTION))
        my-sargam-pitch (first (filter #(and (vector? %) (= :SARGAM_PITCH (first %))) (all-nodes result)))
        
        ]
    (println "transform-sargam-pitch-should-create-hash-of-values")
    (pprint result)
    (pprint my-sargam-pitch)
   (is (.contains (str result)  ":normalized_pitch \"Eb\""))
   ; (is (map? map))
   ; (is (= "g" (:pitch_source map)))
   ; (is (= "g" (:source map)))
   ; (is (= "Eb" (:normalized_pitch map)))
  )
  )

(def sample-section2
  '[:SARGAM_SECTION
 ([:SARGAM_LINE
   {:content
    ([:MEASURE
      [:BEAT
       {:items
        ([:SARGAM_PITCH
          {:mordent nil,
           :syllable nil,
           :chord nil,
           :pitch_source "g",
           :normalized_pitch "Eb",
           :ornament [],
           :octave 0,
           :tala nil,
           :source "g"}]),
        :divisions 1}]]),
    :syllables ()}])]
)

(defn transform-beat-helper2[txt which_pitch my_numerator my_denominator]
  (let [ result
        (insta/transform { :BEAT transform-beat } 
                         (get-parser2 txt :start :BEAT))
        item
        (first (filter #(= :S (first (second %))) (:items (second result))))
        ]
     ;;(pprint result)
     ;;(pprint item)
    ;;TODO
    )
  )

(deftest transform-beat-should-add-ratio-to-pitches
  (transform-beat-helper2 "S" :S 1 1)
  )

(deftest test-transform-beat-divisions
  "transform-beat should count the divisions"
  (transform-beat-helper "S--R--" 6)
  (transform-beat-helper "S" 1)
  (transform-beat-helper "Srg" 3)
  )
(deftest test-transform-beat-structure
  (is (= :BEAT (first 
                 (insta/transform { :BEAT transform-beat } 
                                  (get-parser2 "SR" :start :BEAT)))))
  )

;;(run-tests)

(transform-sargam-section-sargam-pitch-should-create-hash-of-values)
