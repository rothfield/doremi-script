(defproject doremi-script-clojure "0.1.0-SNAPSHOT"
  :description "Parser for doremi-script written in Clojure using instaparse - see http://github.com/rothfield/doremi-script-base"
  :url "http://github.com/rothfield/doremi-script-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"]
                 [instaparse "1.2.2"] 
                 [com.googlecode.texhyphj/texhyphj "1.2"]
                 [clabango "0.5"]
                 ]
  :main doremi_script.doremi_core
  :profiles {:uberjar {:aot :all}}
  ;; lein uberjar creates jar file. Run it use java -jar
  :jar-name "doremi-script.jar"
  :uberjar-name "doremi-script-standalone.jar"
  )
