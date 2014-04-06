(ns doremi_script.handler
  (:use compojure.core)
  (:import [java.io File])
  (:require  [compojure.handler :only [site]]
            [clojure.java.io :as io :refer [input-stream resource]]
            [clojure.java.shell :only  [sh]]
            [doremi_script.doremi_core :refer 
             [doremi-text->collapsed-parse-tree doremi-text->parsed doremi-text->lilypond parse-failed? format-instaparse-errors] ]
            [clojure.string :refer 
             [split replace-first upper-case lower-case join] :as string] 
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.json :only wrap-json-response]
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


(defn sanitize[x]
  (if x
    (-> x (string/replace  #"[^0-9A-Za-z\.]" "_") string/lower-case)))

;; (pprint (-> "SSS" doremi-text->parsed))
;; (pprint (-> "Title: john\n\n|SSS" doremi-text->parsed))
;;(-> "Title: hi\n\nSSS|" (doremi-post true))
;;
;; sh clojure.java.shell
;; (sh & args)
;; Passes the given strings to Runtime.exec() to launch a sub-process.
;;
;; Options are
;;
;; :in may be given followed by any legal input source for
;; clojure.java.io/copy, e.g. InputStream, Reader, File, byte[],
;; or String, to be fed to the sub-process's stdin.
;; :in-enc option may be given followed by a String, used as a character
;; encoding name (for example "UTF-8" or "ISO-8859-1") to
;; convert the input string specified by the :in option to the
;; sub-process's stdin. Defaults to UTF-8.
;; If the :in option provides a byte array, then the bytes are passed
;; unencoded, and this option is ignored.
;; :out-enc option may be given followed by :bytes or a String. If a
;; String is given, it will be used as a character encoding
;; name (for example "UTF-8" or "ISO-8859-1") to convert
;; the sub-process's stdout to a String which is returned.
;; If :bytes is given, the sub-process's stdout will be stored
;; in a byte array and returned. Defaults to UTF-8.
;; :env override the process env with a map (or the underlying Java
;; String[] if you are a masochist).
;; :dir override the process dir with a String or java.io.File.
;;
;; You can bind :env or :dir for multiple operations using with-sh-env
;; and with-sh-dir.
;;
;; sh returns a map of
;; :exit => sub-process's exit code
;; :out => sub-process's stdout (as byte[] or String)
;; :err => sub-process's stderr (String via platform default encoding)
;;
;;
;;
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
            ;; else
            (let [
                  title (get-in results [:attributes :title])
                  file-id (str
                            (if title
                              (str (sanitize title) "-"))
                            md5) 
                  file-path-base (str compositions-dir "/" file-id) 
                  doremi-script-fname (str file-path-base ".doremi.txt")
                  lilypond-fname (str file-path-base ".ly")
                  fname-with-page1 (str file-path-base "-page1.png")
                  _ (println "fname-with-page1" fname-with-page1)
                  lily2image-command (str "lily2image -f=png -q " lilypond-fname)
                  url (str "compositions/" file-id ".png")
                  ]
              (->> (:lilypond results)(spit lilypond-fname))
              ;; if [ -f "$name_with_page1" ]
              ;;(.exists (as-file "myfile.txt"))
              (println "writing" lilypond-fname)
              (->> x (spit doremi-script-fname))
              (println "running lily2image")
              (let [
							shell-results
              (clojure.java.shell/sh "lily2image" "-f=png" "-q" lilypond-fname)
                    ]
                (println "lily2image returns")
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
              (assoc results :staffNotationPath (str "/compositions/" file-id ".png"))
						 ;; TODO: refactor code	
							)
            )
					
					))
			
			)
    ))


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
;;http://zaiste.net/2014/02/web_applications_in_clojure_all_the_way_with_compojure_and_om/
;;
(defn wrap-dir-index [handler]
  (fn [req]
    (handler
      (update-in req [:uri]
                 #(if (= "/" %) "/index.html" %)))))


;; zaiste recommends foundation as a css frameworks.

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
