(ns orcus.components
  (:require [hoplon.core :as h]))

(defmacro html [& body]
  (let [[_ {:keys [route] :as attr} kids] (h/parse-e (list* '_ body))]
    `(binding [*route* ~route]
       (hoplon.core/html ~(dissoc attr :route) ~@kids))))
