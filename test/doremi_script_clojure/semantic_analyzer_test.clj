(ns doremi_script_clojure.semantic-analyzer-test
	(:require [clojure.test :refer :all ]
						[clojure.pprint :refer :all ]
						[doremi_script_clojure.test-helper :refer :all ]
						[doremi_script_clojure.semantic-analyzer :refer :all ]
						[instaparse.core :as insta]
						))


(deftest test-position-map-for-sargam-section
				 (let [nodes (get-parser2  "G- P-\nHe-llo" :start :SARGAM_SECTION)
										 result (position-map-for-sargam-section nodes)
										 ]  
          (println (meta nodes))
					; (println "nodes are")
					; (pprint nodes)

					 ;(is (not= (position-map-for-sargam-section nodes 
					 ;					) true)
					 ;		 )))
          (pprint result)
					 ))


(test-position-map-for-sargam-section)

;(pprint (get-parser2  "G- P-\nHe-llo" :start :SARGAM_SECTION))
