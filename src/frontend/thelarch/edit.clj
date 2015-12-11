(ns thelarch.edit)

(defmacro defn+ [name doc bind & body]
  `(def ~name (with-meta (fn ~bind ~@body) {:doc ~doc})))
