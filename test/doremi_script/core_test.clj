(ns doremi-script.core-test
  (:require [clojure.test :refer :all]
    [instaparse.core :as insta]
        [com.stuartsierra.component :as component]
            [clojure.java.io :as io :refer [resource]]
            [ring.mock.request :as mock]
            [doremi-script.utils :refer [get-attributes]]
            [doremi-script.core :refer [new-parser doremi-text->collapsed-parse-tree]]))

(defonce parser 
  (component/start (new-parser (slurp (resource "doremiscript.ebnf")))))

(deftest test-is-kind?
  (is #'doremi-script.core/is-kind? :sargam-composition))

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

        (insta/parse (:parser parser)
                     "S"
                     :start 
                     :sargam-composition))
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
         (#'doremi-script.utils/attribute-section->map 
           [:attribute-section 
            "Title" "hello" 
            "Author" "john" 
            "kind" :abc-composition]
           ))))

;;(run-tests)
; attribute-section->map
