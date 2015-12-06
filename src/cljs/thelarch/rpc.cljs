(ns thelarch.rpc
  (:require-macros
    [javelin.core :refer [defc defc=]])
  (:require
   [goog.net.Cookies]
   [javelin.core]
   [castra.core :refer [mkremote]]))

(defc state {:random nil})
(defc user nil)
(defc error nil)
(defc loading [])

(defc= random-number (get state :random))

(def get-state
  (mkremote 'thelarch.api/get-state state error loading))

(def cks (goog.net.Cookies. js/document))

(defn get-cookies []
  (reduce #(assoc %1 %2 (.get cks %2)) {} (.getKeys cks)))

(def get-user
  (mkremote 'thelarch.api/get-user user error loading))

(defn init []
  (get-user (.get (get-cookies) "access-token")))

(defn logout! []
  (.remove cks "access-token")
  (get-user nil))
