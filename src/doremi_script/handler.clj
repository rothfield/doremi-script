(ns doremi_script.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojure.java.io :as io :refer [input-stream resource]]
            [doremi_script.doremi_core :refer [doremi-text->parsed doremi-text->lilypond parse-failed? format-instaparse-errors] ]
            [clojure.string :refer 
             [split replace-first upper-case lower-case join] :as string] 
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.json :as middleware]
            [ring.middleware.etag   :refer [wrap-etag]]
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



(defn draw[txt parse-result img-url]
  (str "<!DOCTYPE html> <html>"
       "<head><style>
       textarea.hidden{display:none}
       textarea {font-size:14px;}
       </style>"
       "<title>Doremi-Script by John Rothfield</title></head> <body><h1>Doremi-Script Letter music system</h1>"
       "<a href='https://github.com/rothfield/doremi-script#readme'>Doremi-Script project home</a>"
       "<form method='post'> <div>Enter music in letter format. (examples:) <ul><li><b>ABC</b>: | CDbDEb EFF#G AbABbB </li><li><b>Sargam</b>:  SrRg GmMP dDnN | -</li><li><b>Hindi</b>: सर ग़म  म'प धऩ (Use underscores for flat notes) </li></ul></div>
       <br/><textarea rows='10' cols='80' name='val'>" 
       txt
       "</textarea><br/>" 
       "<input type='submit' value='Generate staff notation'>"
       "<button  onclick=\"document.getElementById('result').style.display='block'; return(false);\" ' value='Show Lilypond Output'>Show Lilypond Output</button><br/>"
       "<textarea id='result'"
       (if (parse-succeeded parse-result)
         "class='hidden'" ;; hide if no error
         )
       " rows='10' cols='80'>" 
       (format-instaparse-errors parse-result)

       "</textarea><br/>" 
       " </form></body>"
       (when img-url (str
                       "<img onerror='reload_later()' id='staff_notation'  src='" img-url "'"
                       "' >"))
       (when img-url (str 
                       " 
                       <script language='JavaScript' type='text/javascript'> 
                       var t = 5; // Interval in Seconds
                       image = '"
                       img-url 
                       "' //URL of the Image 
                       function reload_later() { 
                       var tmp = new Date(); 
                       var tmp = '?'+tmp.getTime();
                       var tmp =''; 
                       document.images['staff_notation'].src = image+tmp 
                       setTimeout('reload_later()', t*1000) 
                       } 

                       </script>"))



       " </html>")

  )

(def  comments
  (atom [{:author "Pete Hunt", :text "Hey there!"}]))

(defn get-comments[]
  {:body @comments })

;; (pprint (sanitize nil))
;; (pprint (sanitize "/root/H--'"))
(defn sanitize[x]
  (if x
    (-> x (string/replace  #"[^0-9A-Za-z\.]" "_") string/lower-case)))

(defn post-comment[author text]
  (swap! comments conj {:author author :text text})
  {:body @comments })

;; (pprint (-> "SSS" doremi-text->parsed))
;; (pprint (-> "Title: john\n\n|SSS" doremi-text->parsed))
;;(-> "Title: hi\n\nSSS|" (doremi-post true))
(defn doremi-post[val generate-staff-notation]
  (let [md5 (my-md5 val)
        results (doremi-text->parsed val)
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
        (if generate-staff-notation
          (do
        (->> (:lilypond results)(spit lilypond-fname))
        (assoc results :staffNotationPath (str "/compositions/" file-id ".png")))
         results 
          )
      ))))

;; (-> "public/compositions/yesterday.txt" resource slurp)
        ;; (when-not (.exists (io/as-file fname))

(defroutes app-routes
  (GET "/comments.json" [] 
       ;; GET . Return json data
       (get-comments))
  (GET "/load/yesterday.txt"[]
       {:body (-> "public/compositions/yesterday.txt" resource slurp doremi-text->parsed)}
       )

  (POST "/comments.json" [author text]
        ;; Posted as a form submission, return json
        (post-comment author text))
  (GET "/" []
       (draw " " "" nil))

  (POST "/" [src generateStaffNotation] 
        {:body  (doremi-post src (= "true" generateStaffNotation)) }
        )

  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (->  app-routes
      handler/site 
      middleware/wrap-json-response  ;; Converts responses that are clojure objects to json
      ))
