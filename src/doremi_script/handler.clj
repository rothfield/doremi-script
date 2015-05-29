(ns doremi-script.handler
  (:import [java.io File]
           [java.net URL MalformedURLException]
           )
  (:require [compojure.core :refer [ GET POST PUT defroutes routes]]
            [digest]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.route :as route :refer [resources]]
            [compojure.handler :as handler]
            [clojure.java.io :as io :refer [input-stream resource]]
            [clojure.java.shell :only  [sh]]
            [doremi-script.middleware]
            [com.stuartsierra.component :as component]
            [doremi-script.to-lilypond :refer [to-lilypond]]
            [doremi-script.utils :refer [get-attributes]]
            [doremi-script.core :refer 
             [doremi-text->collapsed-parse-tree new-parser]]
            [doremi-script.to-lilypond :refer [to-lilypond]]
            [clojure.string :refer [split replace-first upper-case
                                    lower-case join] :as string] 
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :only wrap-json-response]
            [ring.util.request]
            [ring.util.response :only [file-response]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.cors :refer [wrap-cors]]
           )) 

(defonce app-state (atom {:the-parser nil}))


(def production?
    (= "production" (get (System/getenv) "APP_ENV")))

(def development?
    (not production?))

(comment
  ;; in repl, type the following to reload this file:
  ;; note the tick symbol
  (use 'doremi-script.handler :reload)
  )


(def compositions-dir
  (str (System/getenv "HOME") "/doremi-public/compositions"))


(defn parse-succeeded[txt]
  (and (string? txt)
       (> (.indexOf txt "#(ly") -1)))

(defn my-md5[txt]
  (let [long-string 
        (apply str
               (map (partial format "%02x")
                    (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                               .reset
                               (.update (.getBytes 
                                          txt
                                          ))))))
        ]
    (subs long-string 0 8) 
    ))
;; (my-md5 "S")
;;
(defn- url? [^String s]
  (try (URL. s) true
       (catch MalformedURLException _ false)))

(defn absolute-url [location request]
  ; creates an absolute URL. location is a path ie. "/compositions/2.png" 
  ; uses the ring request to create an absolute URL. Trust me, it works.
  ; public URL(String protocol, String host,  int port,
  ;          String file) ;  throws MalformedURLException
  ;
  (if (url? location)
    (str location)
    (str  "http://"
          (if production? 
            (:server-name request)
            "ragapedia.local" 
           ) ;; ragapedia.com
               location)))

(defn sanitize[x]
  (if x
    (-> x (string/replace  #"[^0-9A-Za-z\.]" "_") string/lower-case)))

(defn remove-from-end [s end]
    (if (.endsWith s end)
            (.substring s 0 (- (count s)
                                                        (count end)))
          s))
(defn create-mp3![midi-file-path]
  (let [
        ;;  timidity yesterday.mid -Ow -o - | lame - -b 64 yesterday.mp3
        file-path-base (remove-from-end midi-file-path ".mid")
        _ (assert (.endsWith midi-file-path ".mid"))
        tim-file-name (str file-path-base ".tim")
        mp3-file-name (str file-path-base ".mp3")
        tim-results 
        (clojure.java.shell/sh "timidity"
                               midi-file-path
                               "-Ow"
                               "-o"
                               (str file-path-base ".tim"))
        lame-results 
        (clojure.java.shell/sh "lame"
                               tim-file-name
                               "-b"
                               "64"
                               mp3-file-name
                               )
         - (println "lame-results =" lame-results)
        ]
    (clojure.java.io/delete-file tim-file-name) ;; :silently)
    {:lame-results lame-results
     :tim-results tim-results
     :mp3-file-name mp3-file-name
     }
    ))

(defn parse[doremi-text kind]
  (if (or (nil? doremi-text) (= "" doremi-text))
    {:error "empty input"
     :composition nil}
        (doremi-text->collapsed-parse-tree 
                        doremi-text 
                        (get @app-state :the-parser)
                        (keyword kind))
        ))


;; (-> "|(SR)" (doremi-text->collapsed-parse-tree :sargam-composition))
;; (pprint (run-lilypond-on-doremi-text {} "S" :sargam-composition true))
(defn run-lilypond-on-doremi-text[req doremi-text kind mp3]
  ;; version2; eventually delete version1
  ;; TODO: handle and log failures better
  (try
    (let [
          debug false
          kind2 (if (= kind "")
                  nil
                (keyword kind)
                  )
          ]
      (if (= "" doremi-text)
        {} ;; TODO review
        (let [md5 (digest/md5 doremi-text)
              {:keys [:error :composition]:as results} 
              (doremi-text->collapsed-parse-tree 
                doremi-text
             (get @app-state :the-parser)
                  kind2)
              ]
       (if error 
         results
            (let [
                  attributes (get-attributes composition)
                  title (sanitize (get attributes :title "untitled"))
                  dir-str (str compositions-dir "/" title)
                  mkdir-results (.mkdir (java.io.File. dir-str))
                   _ (when debug (println "result of mkdir is" mkdir-results))
                  dir (clojure.java.io/file dir-str)
                  _ (println "title is " title)
                  file-count (count (file-seq dir))
                  _ (println "file-count is" file-count)
                  file-name (str title "-" md5)
                  file-path-base (str dir-str "/" file-name) 
                  current-path-base (str dir-str "/" title "-" "current") 
                  _ (when debug (println "********file-path-base=" file-path-base))
                  doremi-script-fname (str file-path-base ".doremi.txt")
                  lilypond-fname (str file-path-base ".ly")
                  _ (when debug (println "lilypond-fname=" lilypond-fname))
                  fname-with-page1 (str file-path-base "-page1.png")
                  _ (when debug (println "fname-with-page1" fname-with-page1))
                  lily2image-command (str "lily2image -f=png -q " lilypond-fname)
                  path-for-url (absolute-url (str "/compositions/" title "/" file-name) req)
                  _ (when debug (println "path-for-url=" path-for-url))

                  lilypond-results (to-lilypond composition doremi-text)

                  ]
              (when debug
              (println "lilypond-results=" lilypond-results)
              (println "***************parse-results=" composition)
                )
              (->> lilypond-results (spit lilypond-fname))
              ;; if [ -f "$name_with_page1" ]
              ;;(.exists (as-file "myfile.txt"))
              (when debug (println "writing" lilypond-fname))
              (->> doremi-text (spit doremi-script-fname))
              (println "running lily2image")
              (let [
                    shell-results
                    (clojure.java.shell/sh "lily2image" "-f=png" "-q" lilypond-fname)
                    lilypond-shell-results
                    (clojure.java.shell/sh "lilypond" "-f" "pdf"  "-o" (str file-path-base) (str file-path-base ".ly"))
                    create-mp3-results (if mp3 (create-mp3! (str file-path-base ".mid")) {})
                    _ (if mp3 (println "create-mp3! returns =" create-mp3-results))
                    ]
                (println "lily2image returns")
                (println "lilypond results=" lilypond-shell-results)
                (println shell-results)
                )
              (when (.exists (clojure.java.io/as-file fname-with-page1))
                (let [
                      arg1 (str file-path-base  "-page*.png")
                      _ (println "arg1=" arg1)
                      _ (println "arg3=" (str file-path-base ".png")) ]
                  _ (println "running convert")
                  ;;(clojure.java.shell/sh "convert" "/home/john/compositions/094f03ce-page*.png"  "-append" "/home/john/compositions/094f03ce.png")
                  ;;convert ${fp}-page*.png -append ${fp}.png
                  ;;
                  (let [convert-results
                        (clojure.java.shell/sh "convert" 
                                               arg1
                                               "-append"
                                               (str file-path-base ".png"))
                        ]
                    (println "convert results =")
                    (pprint convert-results)
                    )))
                (doseq [extension [".pdf" ".png" ".mid" ".ly" ".doremi.txt" ".mp3"]]
                  (try
               (io/copy (io/file (str file-path-base extension)) (io/file (str current-path-base extension)))
                         (catch Exception e 
                           (println (str "copy:caught exception: " (.getMessage e))))
                 ))
           ;;   (assert (:src parse-results))
            ;;  (assert (:parsed parse-results)) ;; TODO: change to :parsed to  :parse-tree
             ;; (assert (:attributes parse-results))
            (merge results  {  
               :links {
               :pdf-url 
               (str path-for-url ".pdf")
               :staff-notation-url 
               (str path-for-url ".png")
               :midi-url
               (str path-for-url ".mid")
               :lilypond-url
               (str path-for-url ".ly")
               :doremi-text-url 
               (str path-for-url  ".doremi.txt")
               :mp3-url 
               (str path-for-url ".mp3")
              :browse-url
                  (absolute-url (str "/open/" title) req)
                     }}) 
              )
            )

          ))

      )
    ))

(defn init[]
  ;; This is called once before the handler starts
  ;; See lein-ring docs
  ;; In project.clj, this is set under the ring key: 
  ;;  :ring {:handler doremi-script.handler/app
  ;;             :init doremi-script.handler/init
  ;;             :destroy doremi-script.handler/destroy}
  (swap! app-state 
           assoc
           :the-parser 
           (component/start (new-parser
                    (slurp (resource "doremiscript.ebnf")))))
  (println "doremi-script/handler.init: doremi-script is starting"))

(defn destroy []
  (println "doremi-script is shutting down"))


(defroutes app-routes
  (POST "/doremi-server/parse" [src  kind] 
        {:body
         (parse src kind)
         }
        )
  (GET "/doremi-server/run-lilypond-on-doremi-text" [] 
       "Usage: POST /doremi-server/run-lilypond-on-doremi-text {:src \"SRG|\"
       :kind \"sargam-composition\" :mp3 true   }"
       )

  (POST "/doremi-server/run-lilypond-on-doremi-text" [src  kind mp3] 
        (println "in POST, mp3 =" mp3)
        (fn [req]
          {:body
           (run-lilypond-on-doremi-text req src kind mp3)
           }
          ))

  (GET "/doremi-server/ping" []
       "doremi-server is up!!")
  (route/resources "/doremi-server")
  (GET "/doremi-server" []
       "doremi-server is up.")
  (GET "/doremi-server/" []
       "doremi-server is up.")
  (route/resources "/")
  (route/not-found "Not Found")
  )


(def app
    (-> app-routes
              (wrap-cors :access-control-allow-origin [#".*"]
                                          :access-control-allow-methods [:get :put :post])
              (handler/site)
              ring.middleware.json/wrap-json-response  ;; Converts responses that are clojure objects to json
         doremi-script.middleware/wrap-request-logging 
))

(def my-port 4000)

;;(println "STARTING server on port" my-port)
;;(defonce server (run-jetty #'app {:port my-port :join? false}))


