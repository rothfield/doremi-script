(defproject doremi-server "0.1.0-SNAPSHOT"
  :description "doremi-script: Easy to use textual music notation supporting multiple dialects (ABC 123 AACM and Hindi/Bhatkande. http://ragapedia.com "
  :url "http://github.com/rothfield/doremi-script"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojars.mikejs/ring-etag-middleware "0.1.0-SNAPSHOT"]
                 [instaparse "1.2.2"] 
                  [javax.servlet/servlet-api "2.5"]
                 ;;			   [ring/ring-jetty-adapter "1.1.6"] 
                 ;;                 [com.googlecode.texhyphj/texhyphj "1.2"]
                 [compojure "1.1.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :main doremi_script.doremi_core
  :jar-name "doremi-script.jar"
  :uberjar-name "doremi-script-standalone.jar"
  :ring {:handler doremi_script.handler/app}
  :profiles { :uberjar {:aot :all}
             :dev {:dependencies [
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
