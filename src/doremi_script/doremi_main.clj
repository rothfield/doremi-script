(ns doremi_script.doremi_main
  (:gen-class)
  (:require	
    [clojure.string :refer 
     [split replace-first upper-case lower-case join] :as string] 
    [ring.adapter.jetty :only [run-jetty]]
    [clojure.java.io :as io :only [input-stream resource]]
    [doremi_script.handler :only [app]]
    [doremi_script.doremi_core :only [parse-failed? format-instaparse-errors 
                                    doremi-text->lilypond]]
;;    [instaparse.core :as insta]
;;    [clojure.string :refer 
;;     [split replace-first upper-case lower-case join] :as string] 
;;    [clojure.zip :as zip]
;;    [clojure.java.io :as io :refer [input-stream resource]]
;;    [clojure.pprint :refer [pprint]] 
;;    [clojure.walk :refer [postwalk]]
    ))

(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  ;; cp% runs current form. vim-fireplace
  (set! *warn-on-reflection* true)
  (use 'doremi_script.main :reload) (ns doremi_script.main) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (print-stack-trace *e)
  (pst)
  )


(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (join "\n" seq)))))

(defn process-main-file[fname]
  (let [my-file
        (io/as-file fname)
        out-file-name (str fname ".ly")
        out-file (io/as-file out-file-name)
        ]
    (if-not (.exists my-file)
      (println fname " not found")
      (try
        (let [x (-> my-file slurp (doremi_script.doremi_core/doremi-text->lilypond nil))
              ]
          (if (doremi_script.doremi_core/parse-failed? x)
            (-> x doremi_script.doremi_core/format-instaparse-errors println)
            (do (println "Saving " out-file-name)
                (->> x (spit out-file)))
            ))
        (catch Exception e (println (str "Error:" (.getMessage e))))
        )
      )))


(defn usage[]
  (println "Usage:")
  (println " java -jar doremi.jar file1 file2 ... ")
  (println "or use java -jar - to read from standard input")
  )


(defonce server (fn[] 
   (ring.adapter.jetty/run-jetty (var doremi_script.handler/app)
              {:port 3000 :join? false})))

(defn stop[]
  (.stop server))
(defn main-aux[args]
  (cond  (empty? args)
        (usage)
        (= "-s" (last args))
        (server)
        (= "-" (last args))

        (try
          "Read from stdin. Writes results to stdout"
          (let [parsed (doremi_script.doremi_core/doremi-text->lilypond (get-stdin) nil)]
            (if (doremi_script.doremi_core/parse-failed? parsed)
              (println (doremi_script.doremi_core/format-instaparse-errors parsed))
              (println parsed))
            )
          (catch Exception e (println (str "Error:" (.getMessage e))))
          )
        true
        (pmap process-main-file args)
        ))

(defn get-stdin[]
  (with-open [rdr (java.io.BufferedReader. *in* )]
    (let [seq  (line-seq rdr)
          zz (count seq)]
      (apply str (join "\n" seq)))))

(defn process-main-file[fname]
  (let [my-file
        (io/as-file fname)
        out-file-name (str fname ".ly")
        out-file (io/as-file out-file-name)
        ]
    (if-not (.exists my-file)
      (println fname " not found")
      (try
        (let [x (-> my-file slurp (doremi_script.doremi_core/doremi-text->lilypond nil))
              ]
          (if (doremi_script.doremi_core/parse-failed? x)
            (-> x doremi_script.doremi_core/format-instaparse-errors println)
            (do (println "Saving " out-file-name)
                (->> x (spit out-file)))
            ))
        (catch Exception e (println (str "Error:" (.getMessage e))))
        )
      )))


(defn usage[]
  (println "Usage:")
  (println " java -jar doremi.jar file1 file2 ... ")
  (println "or use java -jar - to read from standard input")
  )




;; Called from OS
(defn -main[& args]
  (main-aux args)
  )


