;;; clojure.lang.Compiler$CompilerException: java.lang.IllegalArgumentException: No single method: _setup of interface: cljs.repl.IJavaScriptEnv found for function: -setup of protocol: IJavaScriptEnv, compiling:(cemerick/piggieback.clj:149:5)
;;; To compile cljc files I did the following:
;;; Added [org.clojure/clojurescript "0.0-3211"]
;;; and commented out boot-cljs-repl


(set-env!
 :source-paths    #{"src"}
 :src-paths    #{"src"}
 :resource-paths  #{"resources/public"}
 :dependencies '[
 [adzerk/boot-cljs  "0.0-2814-4" :scope "test"]
;; [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT"]
 [adzerk/boot-reload    "0.2.4"      :scope "test"]
 [pandeiro/boot-http    "0.6.1"      :scope "test"]
 [org.clojure/data.csv "0.1.2"]
 [org.clojure/clojure "1.7.0-beta2"]
;; [org.clojure/clojure "1.6.0"]
 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
 [com.lucasbradstreet/instaparse-cljs "1.3.5"]
 [org.clojure/clojurescript "0.0-3211"]
 ;;[org.clojure/clojurescript "0.0-3126"]
 [prismatic/dommy "1.0.0"]
;;; [servant "0.1.3"]
 [reagent "0.5.0"]])

(set-env! :target-path "app_target")

(require
 '[boot.pod :as pod]
 '[boot.util :as util]
 '[clojure.java.io :as io]
 '[adzerk.boot-cljs      :refer [cljs]]
 ;;'[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]])

(def pod-deps '[[asset-minifier "0.1.6"]])

		(deftask build []
		 (comp (speak)

			(cljs)
		 ))

		(deftask run []
		 (comp (serve)
			(watch)
;;			(cljs-repl)
			(reload)
			(build)))

		(deftask production []
		 (task-options! cljs {:optimizations :advanced
			;; pseudo-names true is currently required
			;; https://github.com/martinklepsch/pseudo-names-error
			;; hopefully fixed soon
			:pseudo-names true})
		 identity)

		(deftask development []
		 (task-options! cljs {:optimizations :none
			:unified-mode true
			:source-map true}
			reload {:on-jsload 'doremi-script.app/init})
		 identity)

		(deftask dev
		 "Simple alias to run application in development mode"
		 []
		 (comp (development)
			(run)))


		(defn- make-minifier-pod
		 []
		 (pod/make-pod (update-in pod/env [:dependencies] into pod-deps)))

		(deftask css-min
		 "Minify CSS files."
		 []
		 (let [tmp (temp-dir!)
			pod (future (make-minifier-pod))]
			(with-pre-wrap fileset
			 (empty-dir! tmp)
			 (doseq [[in-path in-file] (->> (input-files fileset)
																	(by-ext [".css"])
																	(map (juxt tmppath tmpfile)))]
				(let [out-path (.replaceAll in-path "\\.css$" ".min.css")
				 out-file (io/file tmp out-path)]
				 (util/info "Minifying %s -> %s...\n" in-path out-path)
				 (pod/with-call-in @pod
					(asset-minifier.core/minify-css ~(.getPath in-file) ~(.getPath out-file)))))
			 (-> fileset (add-resource tmp) commit!))))
