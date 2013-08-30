(defproject doremi-script-clojure "0.1.0-SNAPSHOT"
  :description "Parser for doremi-script written in Clojure using instaparse - see http://github.com/rothfield/doremi-script-base"
  :url "http://github.com/rothfield/doremi-script-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.2"]
                 [instaparse "1.2.2"] 
                 ]
  :main doremi_script_clojure.core
  )
