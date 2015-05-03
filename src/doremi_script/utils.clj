(ns doremi-script.utils
  (:require	
    [clojure.string :refer [lower-case split join] :as string] 
    ))

(defn format-instaparse-errors
  "Tightens up instaparse error format by deleting newlines after 'of' "
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

(defn- log [msg & vals]
    (let [line (apply format msg vals)]
          (locking System/out (println line))))

(defn is?[k x]
  {:pre [(keyword? k) ]
   :post [(instance? Boolean %)] }
  (and (vector? x)
       (= k (first x))))

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
   ;;  :pre [(is-a? :attribute-section x)]
   ;; :post [ (map? %)]
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

