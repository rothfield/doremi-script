(ns doremi-script.grammar-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [instaparse.core :as insta]
            [clojure.java.io :as io :refer [input-stream resource]]
            [ring.mock.request :as mock]
            [doremi-script.doremi-core :refer [is-kind? doremi-script-grammar doremi-script-parse]]))


;; (run-tests)

(defn fixtures[which]
  (->> (str "fixtures/" (clojure.string/replace (name which) "-" "_"))
       io/resource
       io/file
       file-seq 
       (filter (fn[^java.io.File x] (.endsWith (.getName x) ".txt")))
       (sort-by (fn[^java.io.File x] (.getName x))))) 




;; (def my-file (-> "fixtures/semantic_analyzer_test.txt" io/resource slurp doremi-script-parse))
;; (prn my-file)
(defn parse-fixtures-test-helper[which]
  (doseq [my-file (fixtures which)
          :let [file-name (.getName my-file)
                txt (-> my-file slurp)
                result
                (-> txt (doremi-script-parse which))
                results2 (insta/parses doremi-script-grammar txt
                                       :start which )
                more (second results2)
                ambiguous? (not (nil? (second results2)))
                ;; (insta/failure? result) will detect both these scenarios and return true if the result is either a failure object, or an empty list with a failure object attached as metdata.
                failure? (true? (insta/failure? result))
                ] 

          ]
    (println "processing" (.getPath my-file))
    ;;(println "failure? is" failure?)
    (when ambiguous? 
      (println "Warning: (known bug) " file-name " had an ambiguous parse" txt
               "\nFirst 5 results are\n")
      (->> results2 (take 5) pprint)) 

    (if (.contains file-name "fail")
      (is (true? failure?)
          (str file-name "was expected  to fail, but didn't!\n" txt "\n" (-> result pprint with-out-str)))
      (is (false? failure?)
          (str "insta/failure:\n " txt "\n"  (insta/get-failure result))
          ))
    ))

(deftest test-sargam-composition[]
  (parse-fixtures-test-helper :sargam-composition)
  )

(deftest test-number-composition[]
  (parse-fixtures-test-helper :number-composition)
  )
(deftest test-doremi-composition[]
  (parse-fixtures-test-helper :doremi-composition)
  )
(deftest test-hindi-composition[]
  (parse-fixtures-test-helper :hindi-composition)
  )
(deftest test-abc-composition[]
  (parse-fixtures-test-helper :abc-composition)
  )
(println "testv1")
;; (run-tests)
; attribute-section->map
