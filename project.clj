(defproject doremi-server "0.1.0-SNAPSHOT"
  :description "Parser for doremi-script written in Clojure using instaparse - see http://github.com/rothfield/doremi-script-base"
  :url "http://github.com/rothfield/doremi-script-clojure"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojars.mikejs/ring-etag-middleware "0.1.0-SNAPSHOT"]
                 [instaparse "1.2.2"] 
                 [com.googlecode.texhyphj/texhyphj "1.2"]
                 [compojure "1.1.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :jar-name "doremi-script.jar"
  :uberjar-name "doremi-script-standalone.jar"
  :ring {:handler doremi_script_clojure.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
