(ns doremi_script_clojure.to_lilypond
  (:require	
    [clabango.parser :refer [render]]
    [clojure.pprint :refer [pprint]] 
    ))


(defn to-lilypond[template x]
  :pre (map? x)
  "Takes parsed doremi-script and returns lilypond text"
  ""
  (render template x))
(defn extract-lyrics[x]

 ) 



(comment
(to-lilypond "notmuch" sample-data)
  )

(def sample-data
  {}
)
