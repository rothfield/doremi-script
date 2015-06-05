(ns doremi-script.db
  (:require 
    [doremi-script.sargam-key-map :refer [default-key-map]]
))
(def production?
  ;; set the global in index.html
  (and (not= js/undefined js/DOREM_SCRIPT_APP_ENV)
       (= js/DOREM_SCRIPT_APP_ENV "production")
       ))

(def development?
  (not production?))

(def GENERATE-STAFF-NOTATION-URL
  (if production?
    "http://ragapedia.com/doremi-server/run-lilypond-on-doremi-text"
    "http://localhost:4000/doremi-server/run-lilypond-on-doremi-text")
  )
(def PARSE-URL
  ;; TODO dry
  (if production?
    "http://ragapedia.com/doremi-server/parse"
    "http://localhost:4000/doremi-server/parse")
  )


(def initial-state
  {
   :lilypond-source "lilypond source here"
   :supports-utf8-characters false
   :generate-staff-notation-url GENERATE-STAFF-NOTATION-URL
   :parse-url PARSE-URL
   :parser nil
   :links []
   :doremi-text "|S"
   :online true 
   :the-parser nil
   :key-map  default-key-map
  :rendering false
  :parse-xhr-is-running false
   :ajax-is-running false 
   :composition-kind :sargam-composition
   :render-as :sargam-composition
   :composition nil 
   :environment (if production? :production :development)
   })

