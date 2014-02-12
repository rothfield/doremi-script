(ns doremi_script.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojure.java.io :as io :refer [input-stream resource]]
            [doremi_script.doremi_core :refer [doremi-text->lilypond] ]
            [clojure.pprint :refer [pprint]] 
            [ring.middleware.etag   :refer [wrap-etag]]
            [ring.middleware.params         :only [wrap-params]]
            [compojure.route :as route]))
  ;; (wrap-file "compositions")

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
       "<title>Doremi-Script by John Rothfield</title> <body><h1>Doremi-Script Letter music system</h1>"
       "<a href='https://github.com/rothfield/doremi-script#readme'>Doremi-Script project home</a>"
       "<form method='post'> <div>Enter music in letter format. (examples:) <ul><li><b>ABC</b>: | CDbDEb EFF#G AbABbB </li><li><b>Sargam</b>:  SrRg GmMP dDnN | -</li><li><b>Hindi</b>: सर ग़म  म'प धऩ (Use underscores for flat notes) </li></ul></div>
       <br/><textarea rows='10' cols='80' name='val'>" 
       txt
       "</textarea><br/>" 
       "<input type='submit' value='Generate staff notation'><br/>"
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
         (when true
          ;; (when-not (.exists (io/as-file fname))
            (->> val doremi-text->lilypond (spit fname)))
          ;(str md5 ".ly")
          (draw val (doremi-text->lilypond val)
                url
                ))) 
        ))
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
 (->  (handler/site app-routes)))
