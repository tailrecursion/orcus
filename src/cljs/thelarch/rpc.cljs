(ns thelarch.rpc
  (:require-macros
    [javelin.core :refer [defc defc=]])
  (:require
   [javelin.core]
   [castra.core :refer [mkremote]]))

(defc state {:random nil})
(defc user nil)
(defc error nil)
(defc loading [])

(defc= random-number (get state :random))

(def get-state
  (mkremote 'thelarch.api/get-state state error loading))

(def get-user
  (mkremote 'thelarch.api/get-user user error loading))

(defn init []
  (get-state)
  (js/setInterval get-state 1000))
