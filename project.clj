(defproject doremi-server "0.1.0-SNAPSHOT"
  :description "doremi-script: Easy to use textual music notation supporting multiple dialects (ABC 123 AACM and Hindi/Bhatkande. http://ragapedia.com "
  :url "http://github.com/rothfield/doremi-script"
  :dependencies 
  [
   [org.clojure/clojure "1.5.1"]
   [hiccup "1.0.5"]
   [compojure "1.1.6"]
   [ring/ring-json "0.2.0"]
   [ring/ring-jetty-adapter "1.2.0"] 
   [org.clojars.mikejs/ring-etag-middleware "0.1.0-SNAPSHOT"]
   [instaparse "1.2.2"] 
   [ring.middleware.logger "0.4.0"]
   [org.clojure/clojurescript "0.0-2173"]
    [om "0.5.1"]
   ]
  ;;                 [com.googlecode.texhyphj/texhyphj "1.2"]
  :plugins 
  [[lein-ring "0.8.10"]
   [lein-cljsbuild "1.0.2"]
   ]
  ;; You can run the standalone jar as follows
  ;; java -jar doremi.jar -s  to run server
  ;; It will run the main function in doremi_core
  :main doremi_script.doremi_main 
  :jar-name "doremi-script.jar"
  :uberjar-name "doremi.jar"
  :ring {:handler doremi_script.handler/app}
  :profiles
  { :uberjar {:aot :all}
   :dev {:dependencies [
                        [javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}

  :cljsbuild
  {
   :builds {
            :dev {
                  :source-paths ["src-cljs"]
                  :compiler {:output-to "resources/public/cljs_js/doremi_cljs.js"
                             :output-dir "resources/public/cljs_js"
                             :optimizations :none
                             :pretty-print true
                             :source-map "resources/public/cljs/doremi_cljs.js.map"}}

            ;;                     :prod {
            ;;                            :source-paths ["src-cljs"]
            ;;                            :compiler {:output-to "resources/public/js-min/doremi_cljs_min.js"
            ;;                                       :output-dir "resources/public/js-min"
            ;;                                       :optimizations :advanced
            ;;                                       :pretty-print false
            ;;                                       :source-map "resources/public/js-min/doremi_cljs_min.js.map"}}
            }
   }


  )
