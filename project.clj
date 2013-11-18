(defproject doremi-script-clojure "0.1.0-SNAPSHOT"
  :description "Parser for doremi-script written in Clojure using instaparse - see http://github.com/rothfield/doremi-script-base"
  :url "http://github.com/rothfield/doremi-script-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-localrepo "0.5.2"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"]
                 [instaparse "1.2.2"] 
                ;; [com.stuartsierra/clojure.walk2 "0.1.0-SNAPSHOT"]
                ;; [net.davidashen/texhypj "1.0"]
               ;;  [net.davidashen.text.Hyphenator "1.0"]
                 ]
  :main doremi_script_clojure.core
  ;; lein uberjar creates jar file. Run it use java -jar
  )
