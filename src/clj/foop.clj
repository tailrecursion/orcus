(ns foop
  (:require [clojure.zip :as zip]))

(def tree
  [{:id 1 :text "my org mode document"}
   [{:id 2 :text "a child"}]
   [{:id 3 :text "another child"}
    [{:id 4 :text "inner dude the first"}]]
   [{:id 5 :text "yet another child"}]])

(def indexed (partial map-indexed list))

(defn node*     [z] (when z (zip/node z)))
(defn children* [z] (when (and z (zip/branch? z)) (zip/children z)))
(defn up*       [z] (when z (or (and (first (zip/up z)) (zip/up z)) z)))
(defn down*     [z] (when z (or (and (first (zip/down z)) (zip/down z)) z)))
(defn left*     [z] (when z (or (and (first (zip/left z)) (zip/left z)) z)))
(defn right*    [z] (when z (or (and (first (zip/right z)) (zip/right z)) z)))

(defn next* [z]
  (when z
    (let [zz  (zip/next z)
          zz' (when zz (zip/next zz))]
      (cond (node* zz)  zz
            (node* zz') zz'))))

(defn prev* [z]
  (when z
    (let [zz  (zip/prev z)
          zz' (when zz (zip/prev zz))]
      (cond (node* zz)  zz
            (node* zz') zz'))))

(comment
  
  (-> (zip/zipper vector? next into tree)
      (zip/next)
      (zip/next)
      (zip/next)
      (zip/next)
      (zip/next)
      (zip/next)
      (zip/node)
      )
  
  )
