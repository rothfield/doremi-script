(ns doremi-script.subscriptions
  (:require-macros 
    [reagent.ratom :refer [reaction]]
    )
  (:require 
    [re-frame.core :refer [register-sub]]
    [reagent.core :as reagent]
    ))

(register-sub :lilypond-source
              (fn [db _]  
                (reaction (:lilypond-source @db))))

(register-sub :supports-utf8-characters
              (fn [db _]  
                (reaction (:supports-utf8-characters @db))))

(register-sub :environment
              (fn [db _]  
                (reaction (:environment @db))))

(register-sub :links
              (fn [db _]  
                (reaction (:links @db))))

(register-sub :mp3-url
              (fn [db _]  
                (reaction (get-in @db [:links :mp3-url]))))

(register-sub :staff-notation-url
              (fn [db _]  
                (reaction (get-in @db [:links :staff-notation-url]))))

(register-sub :parse-xhr-is-running
              (fn [db _]  
                (reaction (:parse-xhr-is-running @db))))

(register-sub :ajax-is-running
              (fn [db _]  
                (reaction (:ajax-is-running @db))))

(register-sub :online
              (fn [db _]  
                (reaction (:online @db))))

(register-sub :doremi-text
              (fn [db _]  
                (reaction (:doremi-text @db))))

(register-sub :composition
              (fn [db _]
                (reaction (:composition @db))))

(register-sub :composition-kind
              (fn [db _]
                (reaction (:composition-kind @db))))

(register-sub :current-entry-area
              (fn [db _]
                (reaction (:current-entry-area @db))))
(register-sub :render-as
              (fn [db _]
                (reaction (:render-as @db))))
(register-sub :error
              (fn [db _]
                (reaction (:error @db))))
(register-sub :parser
              (fn [db _]
                (reaction (:parser @db))))

(register-sub :key-map
              (fn [db _]
                (reaction (:key-map @db))))


