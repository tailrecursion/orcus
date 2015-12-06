(ns thelarch.handler
  (:require
   [adzerk.env               :as env]
   [compojure.core           :refer [defroutes GET]]
   [compojure.route          :as route]
   [ring.middleware.defaults :as d]
   [ring.util.response       :as response]
   [castra.middleware        :as castra]
   [castra.core              :refer [*session*]]
   [thelarch.db              :as db]
   [thelarch.api             :as api]
   [thelarch.github          :as gh]))

(defroutes app-routes
  (GET "/" req
    (response/content-type (response/resource-response "index.html") "text/html"))
  (GET "/github-callback" {{session-code :code} :params}
    (when-let [access-token (gh/get-access-token session-code)]
      (let [user (gh/get-user access-token)]
        @(db/register! user)
        {:status 302
         :cookies {"access-token" access-token}
         :headers {"location" "http://localhost:8000/login.html"}})))
  (route/resources "/" {:root ""}))

(def app
  (-> app-routes
      (castra/wrap-castra 'thelarch.api)
      (castra/wrap-castra-session "a 16-byte secret")
      (d/wrap-defaults (merge d/api-defaults {:cookies true}))))

;; (defn login! [access-token]
;;   (swap! *session* assoc :user (gh-user access-token)))

;; (defrpc login [access-token]
;;   {:rpc/pre [(login! access-token)]}
;;   (d/pull (d/db conn)
;;           [:user/access-token :user/login :user/avatar]
;;           [:user/access-token access-token]))

;; (defrpc get-user [])
