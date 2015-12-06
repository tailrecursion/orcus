(ns thelarch.handler
  (:require
   [adzerk.env               :as env]
   [clj-http.client          :as http]
   [cheshire.core            :as json]
   [compojure.core           :refer [defroutes GET]]
   [compojure.route          :as route]
   [ring.middleware.defaults :as d]
   [ring.middleware.cookies]
   [ring.util.response       :as response]
   [castra.middleware        :as castra]
   [castra.core              :refer [*session*]]
   [thelarch.api             :as api]))

(env/def
  THELARCH_GH_BASIC_CLIENT_ID :required
  THELARCH_GH_BASIC_SECRET_ID :required)

(defn gh-access-token [session-code]
  (some-> (http/post "https://github.com/login/oauth/access_token"
                     {:form-params {:client_id THELARCH_GH_BASIC_CLIENT_ID
                                    :client_secret THELARCH_GH_BASIC_SECRET_ID
                                    :code session-code}
                      :headers {"accept" "application/json"}})
          :body
          (json/parse-string true)
          :access_token))

(defroutes app-routes
  (GET "/" req
    (response/content-type (response/resource-response "index.html") "text/html"))
  (GET "/github-callback" {{session-code :code} :params}
    (let [access-token (gh-access-token session-code)]
      (when-let [user (api/gh-user access-token)]
        (api/register! user access-token)
        {:status 302
         :cookies {"access-token" access-token}
         :headers {"location" "http://localhost:8000/login.html"}})))
  (route/resources "/" {:root ""}))

(def app
  (-> app-routes
      (d/wrap-defaults (merge d/api-defaults {:cookies true}))
      (castra/wrap-castra-session "a 16-byte secret")
      (castra/wrap-castra 'thelarch.api)))
