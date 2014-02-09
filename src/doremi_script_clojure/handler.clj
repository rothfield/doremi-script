(ns doremi_script_clojure.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojure.java.io :as io :refer [input-stream resource]]
            [doremi_script_clojure.doremi_core :refer [doremi-text->lilypond] ]
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.etag   :refer [wrap-etag]]
            [ring.middleware.params         :only [wrap-params]]
            [compojure.route :as route]))

(defn my-md5[txt]
  (apply str
         (map (partial format "%02x")
              (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                         .reset
                         (.update (.getBytes 
                                    txt
                                    )))))))

(defn draw[txt parse-result img-url]
  (str "<!DOCTYPE html> <html> <body><h1>doremi-script parser</h1><form method='post'> Enter doremi-script data<br/><textarea rows='10' cols='80' name='val'>" 
       txt
       "</textarea><br/>" 
       "<input type='submit'><br/>"
       "<textarea rows='10' cols='80'>" 
       parse-result  "</textarea><br/>" 
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
       start(); 
       </script>"))


       
      " </html>")

  )
(defroutes app-routes
  (GET "/" [] (draw " " "" nil))
  (POST "/" [val] 
        (let [md5 (my-md5 val)
              fname (str "resources/public/compositions/" md5 ".ly") 
              url (str "compositions/" md5 ".png")
              parsed (doremi-text->lilypond val)
              ]
    ;;[clojure.data.json :as json]
    ;;(with-out-str (pprint x))))
          (if (map? parsed) ;; error
          (draw val (with-out-str (pprint parsed)) nil)
          (do
          (when-not (.exists (io/as-file fname))
            (->> val doremi-text->lilypond (spit fname)))
          ;(str md5 ".ly")
          (draw val (doremi-text->lilypond val)
                url
                ))) 
        ))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
