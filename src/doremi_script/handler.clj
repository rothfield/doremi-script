(ns doremi_script.handler
  (:use compojure.core)
  (:import [java.io File])
  (:require  [compojure.handler :only [site]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io :refer [input-stream resource]]
            [doremi_script.doremi_core :refer 
             [doremi-text->collapsed-parse-tree doremi-text->parsed doremi-text->lilypond parse-failed? format-instaparse-errors] ]
            [clojure.string :refer 
             [split replace-first upper-case lower-case join] :as string] 
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.json :only wrap-json-response]
            ;;json/wrap-json-response  ;; Converts responses that are clojure objects to json
            [ring.middleware.etag   :refer [wrap-etag]]
            [ring.middleware [multipart-params :as mp]]
            [doremi_script.middleware :only [wrap-request-logging]]
            [ring.middleware.params         :only [wrap-params]]
            [ring.util.response :only [file-response]]
            [compojure.route :as route]))
;; (wrap-file "compositions")
;;


(def compositions-dir
     (str (System/getenv "HOME") "/compositions"))

;; (println compositions-dir)
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


(defn upload-file [file]
  (let [file-name (file :filename)
        size (file :size)
        actual-file (file :tempfile)]
    (do
      (io/copy actual-file (File. (format "/Users/milinda/Desktop/%s" file-name)))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (html [:h1 file-name]
                   [:h1 size])})))
(defn sanitize[x]
  (if x
    (-> x (string/replace  #"[^0-9A-Za-z\.]" "_") string/lower-case)))

;; (pprint (-> "SSS" doremi-text->parsed))
;; (pprint (-> "Title: john\n\n|SSS" doremi-text->parsed))
;;(-> "Title: hi\n\nSSS|" (doremi-post true))
(defn doremi-generate-staff-notation[x kind]
  (try
    (let [kind2 (if (= kind "")
                  nil
                  )]
      (if (= "" x)
        {} ;; TODO review
        (let [md5 (my-md5 x)
              results (doremi-text->parsed x kind2)
              ]
          (if (:error results) ;; error
            results
            (let [
                  title (get-in results [:attributes :title])
                  file-id (str
                            (if title
                              (str (sanitize title) "-"))
                            md5) 
                  doremi-script-fname (str compositions-dir "/" file-id ".doremi.txt")
                  lilypond-fname (str compositions-dir "/" file-id ".ly")
                  url (str "compositions/" file-id ".png")
                  ]
              (->> (:lilypond results)(spit lilypond-fname))
              (->> x (spit doremi-script-fname))
              (assoc results :staffNotationPath (str "/compositions/" file-id ".png"))
              results 
              )
            ))))
    (catch Exception e 
      { :error
       (str "caught exception: " (.getMessage e))
       } 
      )))


(defn doParse [src kind] 
  ;; returns a hash
  (try
    (let [kind2 (if (= kind "")
                  nil
                  (keyword kind))
          ]
      (doremi-text->collapsed-parse-tree src kind2)
      )
    (catch Exception e 
      { :error
       (str "caught exception: " (.getMessage e))
       }
      )))


(defroutes app-routes
  ;;  (mp/wrap-multipart-params 
  ;;   (POST "/file" {params :params} (upload-file (get params "file"))))

  (GET "/load/yesterday.txt"[]
       {:body (-> "public/compositions/yesterday.txt" resource slurp doremi-text->parsed)}
       )

  (POST "/parse" [src  kind] 
        {:body
         (doParse src kind)
         }
        )
  (POST "/generate_staff_notation" [src  kind] 
        {:body
         (doremi-generate-staff-notation src kind)
         }
        )
  (GET "/compositions/:filename" [filename]
       (ring.util.response/file-response
         (str filename)
         {:root (str (System/getenv "HOME") "/compositions")}
         ))

         (route/resources "/")
         (route/not-found "Not Found"))

       (defn wrap-dir-index [handler]
         (fn [req]
           (handler
             (update-in req [:uri]
                        #(if (= "/" %) "/index.html" %)))))




       ;;(defn upload-file
       ;; [file]
       ;;(ds/copy (file :tempfile) (ds/file-str "file.out"))
       ;;(render (upload-success)))

       ;;(defroutes public-routes
       ;;             (GET  "/" [] (render (index)))
       (def app
         (->  app-routes
             compojure.handler/site 
             ring.middleware.json/wrap-json-response  ;; Converts responses that are clojure objects to json
             wrap-dir-index
             doremi_script.middleware/wrap-request-logging 
             ))
