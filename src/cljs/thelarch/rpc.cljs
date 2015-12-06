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

(defn pr-ex [ex]
  (when-let [stack (and ex (.-serverStack ex))]
    (.groupCollapsed js/console "RPC error: %s" (:message ex))
    (.error js/console stack)
    (.groupEnd js/console)))

(def get-state
  (mkremote 'thelarch.api/get-state state error loading))

(def cks (goog.net.Cookies. js/document))

(defn get-cookies []
  (reduce #(assoc %1 %2 (.get cks %2)) {} (.getKeys cks)))

(def get-user
  (mkremote 'thelarch.api/get-user user error loading))

(defn init []
  (get-user (.get (get-cookies) "access-token")))

(defc tree nil)

(def put-tree
  (mkremote 'thelarch.api/put-tree tree error loading {:on-error pr-ex}))

(defn logout! []
  (.remove cks "access-token")
  (get-user nil))
