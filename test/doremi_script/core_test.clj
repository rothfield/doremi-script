(ns doremi-script.core-test
  (:require [clojure.test :refer :all]
    [instaparse.core :as insta]
            [clojure.java.io :as io :refer [resource]]
            [ring.mock.request :as mock]
            [doremi-script.utils :refer [get-attributes]]
            [doremi-script.core :refer [doremi-script-parser initialize-parser! doremi-text->collapsed-parse-tree]]))

(defonce initialize-parser
  (initialize-parser!  (slurp (resource "doremiscript.ebnf"))))

(defn destroy []
  (println "doremi-script is shutting down"))

(defn doParse [src kind] 
  ;; returns a hash
  (try
    (let [kind2 (if (= kind "")
                  nil
                  (keyword kind))
          ]
      (doremi-text->collapsed-parse-tree src kind2)
      )
    (catch Exception e 
      { :error
       (str "caught exception: " (.getMessage e))
       }
      )))


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

        (insta/parse @doremi-script-parser 
                     "S"
                     :start 
                     :sargam-composition))
  ))

(deftest doremi-script-grammar-test[]
    (is (= instaparse.core.Parser (class @doremi-script-parser)))
)
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
