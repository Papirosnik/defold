(ns editor.jfx
  (:require [clojure.java.io :as io])
  (:import [javafx.scene.image Image ImageView]))

(set! *warn-on-reflection* true)

;; Image cache
(defonce cached-images (atom {}))

(defn get-image
  ([name]
   (get-image name nil))
  ([name ^Double size]
   (let [image-key    [name size]
         url          (io/resource (str name))
         cached-image (get @cached-images image-key)]
     (cond
       cached-image cached-image
       url          (let [image (if size
                                  (Image. (str url) size size true true)
                                  (Image. (str url)))]
                        (swap! cached-images assoc image-key image)
                        image)))))

(defn get-image-view
  (^ImageView [name]
   (ImageView. ^Image (get-image name)))
  (^ImageView [name size]
   (let [iv (ImageView. ^Image (get-image name))]
     (.setFitWidth iv size)
     (.setFitHeight iv size)
     iv)))
