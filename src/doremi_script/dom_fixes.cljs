(ns doremi-script.dom-fixes
  (:require	
    [doremi-script.utils :refer [log] ]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    ))

(defn add-right-margin-to-notes-with-pitch-signs[context] 
  ;; TODO: pitch_sign was matching nothing. 
  (let [items (sel "span.note_wrapper *.alteration") ]
    (dorun (map (fn[item]
                  (let
                    [parent (dommy/parent item)]
                    (dommy/set-style! (dommy/parent item) 
                                      :margin-right
                                      (str (dommy/px item :width) "px")
                                      )
                    )) items))))


(defn add-left-margin-to-notes-with-left-superscripts[]
  ;; TODO: Raise height of ornament if it is over a barline!!!
  (let [items (sel "span.note_wrapper *.ornament.placement_before")
        ]
    (dorun (map
             (fn[item]
               (let
                 [parent (dommy/parent item)]
                 (dommy/set-style! item 
                                   :margin-left
                                   (str (* -1 
                                           (dommy/px item :width)) "px"))
                 (dommy/set-style! parent 
                                   :margin-left
                                   (str (*  
                                          (dommy/px item :width)) "px"))
                 )) items))
    ))

(defn add-right-margin-to-notes-with-right-superscripts[]
  ;; Not sure why it adjusts note_wrapper and not pitch??
  (log "add-right-margin-to-notes-with-right-superscripts")
  (let [items (sel "span.note_wrapper *.ornament.placement_after") ]
    (dorun (map
             (fn[item]
               (let [parent (dommy/parent item)
                     width (dommy/px item :width)
                     ]            
                 (dommy/set-style! (dommy/parent item)
                                   :margin-right
                                   (str width "px"))
                 )) items))
    ))



(defn adjust-slurs-in-dom[context]
  (comment "html looks like"
           [:span.measure
            [:span.beat.looped
             [:span.note_wrapper ]]
            [:span#0.slur
             ]
            [:span.note.pitch "S"]
            [:span.note_wrapper
             {:data-begin-slur-id "0"}]
            [:span.note.pitch "R"]])
  (let [items (sel "span[data-begin-slur-id]")]
    (dorun 
      (map (fn[item]
             (let
               [ dom-id (dommy/attr item :data-begin-slur-id)
                slur (.getElementById js/document dom-id)
                rect1 (dommy/bounding-client-rect item)
                rect2 (when slur (dommy/bounding-client-rect slur))
                width (when slur (- (:right rect1) (:left rect2)))
                ]
               (log "adjust-slurs-in-dom" dom-id slur rect1 rect2 width)
               (when slur
                 (dommy/set-style! slur :width
                                   (str width "px"))
                 ))) items))))

(defn fallback-if-utf8-characters-not-supported[context]
  ;;; TODO
  ;;; See doremi.coffee from previous version
  )



(defn expand-note-widths-to-accomodate-syllables[context]
  (let [ items  (sel :.syl)]
    (dorun (map-indexed 
             (fn[idx item]
               (when-not (= idx (dec (count items))) ;; omit last syllable on line
                 (let
                   [
                    syl (dommy/text item)  ;; Move to react component css. hyphen
                    ends-word (not= (last syl) "-")
                    extra (if ends-word 5 0)
                    next-item (get items (inc idx)) 
                    pitch (some (fn[x] (when (not= -1 (.indexOf (dommy/class x) "pitch")) x))
                                (-> item dommy/parent dommy/children array-seq) )
                    rect1 (dommy/bounding-client-rect item)
                    rect2 (dommy/bounding-client-rect next-item)
                    ] 
                   (when false  (comment "pitch" pitch) (comment "next-item" next-item) (comment "rect1" rect1) (comment "rect2" rect2))
                   (when 
                     (and (= (:top rect1) (:top rect2))
                          (> (+ extra (:right rect1))
                             (:left rect2)))
                     ;;($note.css("margin-right", "" + (existing_margin_right     + syl_right - next_left + extra + extra2) + "px"));
                     (dommy/set-style! pitch :margin-right 
                                       (str (+ extra
                                               (- (:right rect1)
                                                  (:left rect2)
                                                  )) "px")) 
                     ))))
             items
             ))))

(defn dom-fixes[this]
  (expand-note-widths-to-accomodate-syllables this)
  (add-right-margin-to-notes-with-right-superscripts)
  (add-left-margin-to-notes-with-left-superscripts)
  (add-right-margin-to-notes-with-pitch-signs this)
  (adjust-slurs-in-dom this)
  (fallback-if-utf8-characters-not-supported this)
  )

