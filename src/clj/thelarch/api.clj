(ns thelarch.api
  (:require [thelarch.db :as db]
            [thelarch.github :as gh]
            [castra.core :refer [defrpc *session*]]))

(defrpc get-user [access-token]
  (gh/get-user access-token))

;; (defrpc get-tree [uuid]
;;   (let [db (d/db conn)]
;;     (if (-> '[:find ?id
;;               :in $ ?uuid
;;               :where
;;               [?id :node/uuid ?uuid]
;;               [?id :node/root? true]]
;;             (d/q db uuid)
;;             ffirst)
;;       (hydrate db uuid)
;;       (throw (ex-info "Tree not found" {:uuid uuid})))))

;; (defrpc put-tree! [tree]
;;   (d/transact conn (tree->txes test-tree)))
