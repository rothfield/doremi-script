(ns doremi_script.hello)
(defn ^:export greet [n]
  (str "Hello " n))
