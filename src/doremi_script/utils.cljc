(ns doremi-script.utils
  (:require	
    [clojure.walk :refer [postwalk] ]
    [clojure.string :refer [lower-case split join] :as string] 
    ))

(def debug false)

(defn format-instaparse-errors
  "  Tightens up instaparse error format by deleting newlines after 'of' "
  [z]
  { :pre [string? z]
   :post [(string? %)]
   }
  (if (string? z)
    z
    (let [a (with-out-str (println z))
          ;;_ (println "a is" a)
          [left,right] (split a #"of")
          ]
      (str left "of\n"
           (if right
             (string/replace right #"\n" " "))))))

#?(:cljs
(defn log [& vals]
  (when debug
  (println vals)
    )
))

#?(:clj
(defn log [msg & vals]
    (let [line (apply format msg vals)]
          (locking System/out (println line))))
)

(defn is?[k x]
  {:pre [(keyword? k) ]
   :post [(fn[x] (#{true false} x))] }
  (and (vector? x)
       (= k (first x))))

(defn keywordize-vector[my-vec]
 (postwalk (fn[x] (if (and (vector? x)
                           (string? (first x)))
                      (assoc x 0 (keyword (first x)))
                    x)) my-vec  ))

(defn map-even-items [f coll]
  (map-indexed #(if (zero? (mod %1 2)) (f %2) %2) coll))

(defn get-attribute[pitch my-key]
  (some #(if (when (vector? %) (= my-key(first %))) %)
        pitch))


(defn items
  ; IE (next [:beat [[:pitch "C"] [:pitch "D"]) =>  [[:pitch "C"] [:pitch "D"]]
  "Returns the items, which are always the rest of the vector. "
  ; IE (next [:beat [[:pitch "C"] [:pitch "D"]) =>  [[:pitch "C"] [:pitch "D"]]
  [x]
  { :pre [
          (or (nil? x) (vector? x) (seq? x))]
   :post [(seq? %)]
   }
  (next x))

(defn ^:private attribute-section->map[x]
  {
     :pre [(is? :attribute-section x)]
     :post [ (map? %)]
   }
  (assert (is? :attribute-section x))
  (apply array-map
         (map-even-items #(-> % lower-case keyword)
                         (rest x)))
  )

(defn get-attributes[collapsed-parse-tree]
  (assert (is? :composition collapsed-parse-tree))
  (->> collapsed-parse-tree (filter #(is? :attribute-section %))
       first
       attribute-section->map))

