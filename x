(defn pprint-results[x]
  (if (:_my_type x)
    (with-out-str (json/pprint x :key-fn json-key-fn))
    (with-out-str (pprint x))))
