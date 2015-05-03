(ns doremi-script.to-lilypond-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [instaparse.core :as insta]
            [clojure.java.io :as io :refer [input-stream resource]]
            [ring.mock.request :as mock]
            [doremi-script.core :refer []]))


;;; TODO: *******
;;;

  (comment 
    defn test-all-process-file[^java.io.File my-file]
  (let [ basename (.getName my-file) 
        _ (println "processing " basename)
        directory-path "test/test_results/"
        filename (str directory-path "/" basename)
        my-files (sort (file-seq (io/file directory-path)))
        _ (when nil (pprint my-files))
        to-delete (filter #(fn zz[^java.io.File x] (.startsWith (.getName x) basename))
                          my-files)
        ]
    (map io/delete-file to-delete)
    (io/copy my-file (io/file  filename))
    (->> my-file slurp 
         doremi-text->lilypond2 (spit (str filename ".ly")))
    basename
    ))

