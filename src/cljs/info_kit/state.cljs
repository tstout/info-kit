(ns info-kit.state
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]]
            [order-recon.pubsub :refer [sub-evt]]
            [order-recon.animation :refer [busy-animation lookup-frames tax-replay-frames xml-view-frames]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(def app-state (reagent/atom {:env :prod}))

(defn update-state [key val]
  {:pre [(keyword? key)]}
  (swap! app-state assoc key val))

;; TODO - add state matrix and state machine plumbing

