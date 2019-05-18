(ns info-kit.pubsub
  "Convenience wrappers for core.async pub/sub."
  (:require [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def mk-ch
  "A memoized fn for creating named channels"
  (memoize (fn [_] (chan))))

(def evt-publication
  (pub (mk-ch :evt-pub) :topic))

(defn pub-evt
  "Publish an event to a topic."
  [topic evt]
  (put! (mk-ch :evt-pub) {:topic topic :evt evt}))

(defn sub-evt
  "Subscribe to a topic using a specific channel."
  [topic ch-name]
  {:pre [(every? keyword? [topic ch-name])]}
  (let [out-ch (mk-ch ch-name)]
    (sub evt-publication topic out-ch)
    out-ch))