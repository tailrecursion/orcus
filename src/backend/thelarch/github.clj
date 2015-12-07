(ns thelarch.github
  (:require 
   [adzerk.env      :as env]
   [clj-http.client :as http]
   [cheshire.core   :as json]))

(env/def
  THELARCH_GH_BASIC_CLIENT_ID :required
  THELARCH_GH_BASIC_SECRET_ID :required)

(defn get-access-token [session-code]
  (some-> (http/post "https://github.com/login/oauth/access_token"
                     {:form-params {:client_id THELARCH_GH_BASIC_CLIENT_ID
                                    :client_secret THELARCH_GH_BASIC_SECRET_ID
                                    :code session-code}
                      :headers {"accept" "application/json"}})
          :body
          (json/parse-string true)
          :access_token))

(defn get-user [token]
  (some->
   (http/get "https://api.github.com/user"
             {:query-params {"access_token" token}})
   :body
   (json/parse-string true)))
