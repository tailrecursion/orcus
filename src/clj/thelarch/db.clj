(ns thelarch.db
  (:require [datomic.api :as d]))

(def db-name (System/getProperty "user.name"))
(def db-uri  (str "datomic:dev://localhost:4334/" db-name))
(def conn    (d/connect db-uri))

(def schema
  [;; nodes
   {:db/ident :node/text
    :db/doc "The node's title."
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :node/rank
    :db/doc "The index of the node with respect to its siblings."
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :node/parent
    :db/doc "The parent entity id."
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :node/uuid
    :db/doc "The node's UUID"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :node/status
    :db/doc "The status keyword such as :todo, :in-progress, :complete"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :node/due-date
    :db/doc "The node's due date"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   ;; lists
   {:db/ident :node/root?
    :db/doc "True if this is a root node and has a :root/title attribute."
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :list/versions
    :db/doc "Versions of the root named by a UUID"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}
   {:db/ident :version/created-at
    :db/doc "When this was tree was created."
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   ;; users
   {:db/ident :user/login
    :db/doc "The user's login"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}
   {:db/ident :user/last-login
    :db/doc "The last time the user logged in"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :user/token
    :db/doc "The user's GitHub access token"
    :db/id (d/tempid :db.part/db)
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(def test-tree
  [{:uuid #uuid "d7162151-c521-42f6-82ee-f686a4e2697b"
    :text "my org mode"}
   [{:uuid #uuid "d4cff8b0-da0d-4788-a12d-d522ff4f1edc"
     :text "a child"
     :status :in-progress
     :due-date #inst "2015-12-05T17:44:44.511-00:00"}]
   [{:uuid #uuid "f322deac-783e-4c68-b7ee-78eb2a30aabb"
     :text "another child"
     :status :in-progress
     :due-date #inst "2015-12-05T17:44:44.511-00:00"}
    [{:uuid #uuid "a64f2d05-db25-4d1c-a042-f8cb1c9a0bb7"
      :text "inner dude the first"
      :status :complete}]]
   [{:uuid #uuid "8295b35d-2294-4a8b-b01c-ba0dfc7cd75c"
     :text "yet another child"
     :status :complete}]])

(defn attrs->tx-attrs [attrs]
  (reduce-kv #(assoc %1 (keyword "node" (name %2)) %3) {} attrs))

(defn tree->txes
  ([node]
   (-> (tree->txes nil 0 node)
       (update 0 dissoc :node/parent :node/rank)
       (update 0 merge {:node/root? true})))
  ([parent-eid rank [attrs & kids]]
   (let [id (d/tempid :db.part/user)]
     (into [(merge (attrs->tx-attrs attrs)
                   {:db/id id
                    :node/parent parent-eid
                    :node/rank rank})]
           (mapcat (partial tree->txes id)
                   (range)
                   kids)))))

(defn reset-db! []
  (d/delete-database db-uri)
  (d/create-database db-uri)
  (d/transact conn schema)
  (d/transact conn (tree->txes test-tree)))

(defn children [db parent-uuid]
  (->> (d/q '[:find ?uuid ?rank
              :in $ ?parent-uuid
              :where
              [?parent-id :node/uuid ?parent-uuid]
              [?eid :node/parent ?parent-id]
              [?eid :node/uuid ?uuid]
              [?eid :node/rank ?rank]]
         db
         parent-uuid)
       (sort-by peek)
       (mapv first)))

(defn pluck [m]
  (reduce-kv #(assoc %1 (keyword (name %2)) %3) {} m))

(def public-attrs
  [:node/uuid :node/text :node/status :node/due-date])

(defn hydrate [db uuid]
  (let [root (d/pull db public-attrs [:node/uuid uuid])]
    (into [(pluck root)]
          (map (partial hydrate db)
               (children db uuid)))))

(defn register! [gh-user]
  (let [tx [{:db/id (d/tempid :db.part/user)
             :user/last-login (java.util.Date.)
             :user/login (:login gh-user)}]]
    @(d/transact conn tx)))

