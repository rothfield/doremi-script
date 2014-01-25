(ns doremi_script_clojure.hyphenate
  "Semantic analysis is the activity of a compiler to determine what the types of various values are, how those types interact in expressions, and whether those interactions are semantically reasonable. "
     (:refer-clojure :exclude [replace])
  (:import (net.davidashen.text Hyphenator))
  (:require	
    [clojure.java.io :refer [input-stream resource]]
    [clojure.string :refer [replace]]
    ))

(def hyphenator 
  (memoize 
    (fn []
      (let [h (new Hyphenator)]
        (.loadTable 
          h (input-stream (resource "hyphen.tex")))
        h))))

(def hyphenator-splitting-char (char 173))

(defn hyphenate[txt]
  " (hyphenate \"happy birthday\") => 
  (hap- py birth- day)
  "
  (let [hyphenated (.hyphenate (hyphenator) txt)
   hyphenated2 (replace hyphenated hyphenator-splitting-char \-)]
    (re-seq  #"\w+-?" hyphenated2)))

;; (println (hyphenate "happy"))

