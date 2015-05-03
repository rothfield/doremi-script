(ns doremi-script.doremi-core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [doremi-script.doremi-core :refer :all]))

(deftest test-is-kind?
  (is is-kind? :sargam-composition))

(deftest test-1
  (testing "sample"
    (let [x 2]
      (is (= 2 x))
      (is (= 2 x))
      )))

; (test-is-kind?)
; (run-tests)

(deftest doremi-script-parse-test[]
  (is (= 
        [:sargam-composition
                       [:sargam-stave 
                        [:sargam-notes-line
                         [:sargam-measure
                          [:sargam-beat 
                           [:sargam-pitch "S"]]]]]] 

        (doremi-text->parse-tree "S" :sargam-composition)))
  )
(deftest doremi-script-grammar-test[]
  (let [g doremi-script-grammar]
    (is (= instaparse.core.Parser (class doremi-script-grammar)))
    ))

(def collapsed-parse-tree1
  [:composition [:attribute-section "title" "Hello" "kind" :sargam-composition]
   [:stave [:notes-line [:measure [:beat [:pitch "C" [:octave 0] [:syl "\" \" "]]]] [:barline [:single-barline]]]]]
  )

(deftest get-attributes-test[]
  (let [atts (get-attributes collapsed-parse-tree1)
        ]
    (is (= "Hello" (:title atts)))
    (is (= :sargam-composition (:kind atts)))
    ))
;; (get-attributes-test)
(deftest attribute-section->map-test[]
  (is (= {:title "hello", :author "john", :kind :abc-composition} 
         (#'doremi-script.doremi-core/attribute-section->map 
           [:attribute-section 
            "Title" "hello" 
            "Author" "john" 
            "kind" :abc-composition]
           ))))

(run-tests)
; attribute-section->map
