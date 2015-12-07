(ns thelarch.components)

(defmacro with-route [route & body]
  `(binding [*route* ~route] ~@body))
