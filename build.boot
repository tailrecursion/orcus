(set-env!
  :dependencies '[[adzerk/boot-cljs          "1.7.170-3"]
                  [adzerk/env                "0.2.0"]
                  [adzerk/boot-reload        "0.4.2"]
                  [cheshire                  "5.5.0"]
                  [clj-http                  "2.0.0"]
                  [compojure                 "1.4.0"]
                  [hoplon/boot-hoplon        "0.1.10"]
                  [hoplon/castra             "3.0.0-SNAPSHOT"]
                  [hoplon/hoplon             "6.0.0-alpha10"]
                  [org.clojure/clojure       "1.7.0"]
                  [org.clojure/clojurescript "1.7.189"]
                  [tailrecursion/boot-jetty  "0.1.1"]
                  [ring/ring-core            "1.4.0"]
                  [ring/ring-defaults        "0.1.5"]
                  [ring/ring-devel           "1.4.0"]
                  [com.datomic/datomic-pro   "0.9.5344"]]
  :repositories  #(into % [["datomic" {:url      "https://my.datomic.com/repo"
                                       :username (System/getenv "THELARCH_DATOMIC_REPO_USERNAME")
                                       :password (System/getenv "THELARCH_DATOMIC_REPO_PASSWORD")}]])
  :resource-paths #{"assets" "src/backend" "src/frontend"})

(require
  '[adzerk.boot-cljs         :refer [cljs]]
  '[adzerk.boot-reload       :refer [reload]]
  '[hoplon.boot-hoplon       :refer [hoplon prerender]]
  '[tailrecursion.boot-jetty :refer [serve]])

(task-options!
  speak {:theme "ordinance"})

(deftask dev
  "Build thelarch for local development."
  []
  (comp
    (web :serve 'thelarch.handler/app)
    (repl :server true)
    (serve :port 8000)
    (watch)
    (speak)
    (hoplon)
    (reload)
    (cljs)))

(deftask prod
  "Build thelarch for production deployment."
  []
  (comp
    (hoplon)
    (cljs :optimizations :advanced)
    (prerender)))

(deftask make-war
  "Build a war for deployment"
  []
  (comp (hoplon)
        (cljs :optimizations :advanced)
        (uber :as-jars true)
        (web :serve 'thelarch.handler/app)
        (war)))
