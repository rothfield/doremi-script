(defproject doremi-server "0.1.0-SNAPSHOT"
   :description "doremi-script: Easy to use textual music notation supporting multiple dialects (ABC 123 AACM and Hindi/Bhatkande. http://ragapedia.com "
    :url "http://github.com/rothfield/doremi-script"
  :dependencies [
                 [org.clojure/clojure "1.7.0-beta1"]
  ;;;;;org.clojure/clojure "1.7"]
                 [digest "1.4.4"]
                 [compojure "1.3.3"]
                 [stencil "0.3.5"]  ;;; mustache implementation-clostache is broken
                 [instaparse "1.3.6"]
                 [ring.middleware.jsonp "0.1.6"]
                 [cheshire "5.4.0"]
                 [ring.middleware.logger "0.5.0"]
                 [ring/ring-json "0.3.1"]
                 [ring-cors "0.1.7"]
                 [ring-server "0.4.0"]]
  :plugins [[lein-ring "0.8.12"]]
  :repl-options { :init-ns doremi-script.to-lilypond}
  :ring {:handler doremi-script.handler/app
         :init doremi-script.handler/init
         :destroy doremi-script.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
