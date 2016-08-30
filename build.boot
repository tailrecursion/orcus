(set-env!
  :dependencies '[[adzerk/boot-cljs          "1.7.228-1"]
                  [adzerk/env                "0.3.0"]
                  [adzerk/boot-reload        "0.4.12"]
                  [hoplon/boot-hoplon        "0.1.10"]
                  [hoplon/hoplon             "6.0.0-alpha16"]
                  [org.clojure/clojure       "1.8.0"]
                  [org.clojure/clojurescript "1.7.228"]
                  [tailrecursion/boot-jetty  "0.1.3"]]
  :resource-paths #{"assets" "src/frontend"})

(require
  '[adzerk.boot-cljs         :refer [cljs]]
  '[adzerk.boot-reload       :refer [reload]]
  '[hoplon.boot-hoplon       :refer [hoplon prerender]]
  '[tailrecursion.boot-jetty :refer [serve]])

(task-options!
  speak {:theme "woodblock"})

(deftask dev
  "Build orcus for local development."
  []
  (comp
   (watch)
   (speak)
   (hoplon)
   (reload)
   (cljs)
   (serve :port 8000)))

(deftask prod
  "Build orcus for production deployment."
  []
  (comp
   (hoplon)
   (cljs :optimizations :advanced)
   (target :dir #{"target"})))
