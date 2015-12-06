(ns thelarch.api
  (:require [thelarch.db :as db]
            [thelarch.github :as gh]
            [castra.core :refer [defrpc *session*]]
            [javelin.core :refer [with-let]]))

(defrpc get-user [access-token]
  (with-let [user (gh/get-user access-token)]
    (swap! *session* assoc :user user)))

(defrpc put-tree [tree]
  {:rpc/pre [(:user @*session*)]}
  (let [login (get-in @*session* [:user :login])]
    @(db/put-tree! login tree)
    (db/get-latest-tree login (get-in tree [0 :uuid]))))
