(ns doremi_script.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojure.java.io :as io :refer [input-stream resource]]
            [doremi_script.doremi_core :refer 
             [doremi-text->collapsed-parse-tree doremi-text->parsed doremi-text->lilypond parse-failed? format-instaparse-errors] ]
            [clojure.string :refer 
             [split replace-first upper-case lower-case join] :as string] 
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.json :as middleware]
            [ring.middleware.etag   :refer [wrap-etag]]
             [ring.middleware [multipart-params :as mp]]
            [ring.middleware.params         :only [wrap-params]]
            [compojure.route :as route]))
;; (wrap-file "compositions")
;;
(defn parse-succeeded[txt]
  (and (string? txt)
       (> (.indexOf txt "#(ly") -1)))

(defn my-md5[txt]
  (apply str
         (map (partial format "%02x")
              (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                         .reset
                         (.update (.getBytes 
                                    txt
                                    )))))))




(defn sanitize[x]
  (if x
    (-> x (string/replace  #"[^0-9A-Za-z\.]" "_") string/lower-case)))

;; (pprint (-> "SSS" doremi-text->parsed))
;; (pprint (-> "Title: john\n\n|SSS" doremi-text->parsed))
;;(-> "Title: hi\n\nSSS|" (doremi-post true))
(defn doremi-generate-staff-notation[x]
  (if (= "" x)
    {}
    (let [md5 (my-md5 x)
          results (doremi-text->parsed x)
          ]
      (if (:error results) ;; error
        results
        (let [
              title (get-in results [:attributes :title])
              file-id (str
                        (if title
                          (str (sanitize title) "-"))
                        md5) 
              lilypond-fname (str "resources/public/compositions/" file-id ".ly")
              url (str "compositions/" file-id ".png")
              ]
          (->> (:lilypond results)(spit lilypond-fname))
          (assoc results :staffNotationPath (str "/compositions/" file-id ".png"))
          results 
          )
        ))))

;; (-> "public/compositions/yesterday.txt" resource slurp)
;; (when-not (.exists (io/as-file fname))


(defn doremi-parse[src]

  )
(defroutes app-routes
;;  (mp/wrap-multipart-params 
 ;;   (POST "/file" {params :params} (upload-file (get params "file"))))

(GET "/load/yesterday.txt"[]
     {:body (-> "public/compositions/yesterday.txt" resource slurp doremi-text->parsed)}
     )

(POST "/parse" [src generateStaffNotation] 
      (if (= "true" generateStaffNotation)
        {:body (doremi-generate-staff-notation src) }
        ;;
        {:body  (doremi-text->collapsed-parse-tree src) }
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
      handler/site 
      middleware/wrap-json-response  ;; Converts responses that are clojure objects to json
      wrap-dir-index
      ))
