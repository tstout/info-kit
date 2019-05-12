(ns info-kit.state
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]]
            [info-kit.pubsub :refer [sub-evt]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def app-state (reagent/atom {:env :prod}))

(defn update-state [key val]
  {:pre [(keyword? key)]}
  (swap! app-state assoc key val))


(def events
  "Maps event names to matrix index"
  {:fetch-start 0
   :fetch-end   1
   :error       2})

(def state-matrix
  "This matrix maps events to next-state/action pairs based on current state.
  Nil values are interpreted as no-ops."
  ;; TODO - document action fn sig
  {; state       :fetch-start                :fetch-end                   :error
   :idle         [[:loading nil]             [nil nil]                    [nil nil]]
   :loading      [[nil nil]                  [:idle nil]                  [nil nil]]
   :error        [[nil nil]                  [nil nil]                    [nil nil]]})

(defn set-state
  "Replace current state with new-state, if new-state is not nil"
  [ctx new-state]
  (when new-state
    (swap! ctx assoc :current-state new-state)))

(defn cur-state [ctx]
  (:current-state @ctx))

(defn state-pair
  "Returns the next-state/action pair corresponding to the current state."
  [evt-name ctx matrix]
  (->
    (cur-state ctx)
    matrix
    (nth (events evt-name))))

(defn evt-name [evt]
  (-> :evt evt keys first))

(defn invoke-action
  "According to the supplied state matrix, invoke the appropriate action
  and return the next state."
  [context matrix evt]
  (let [e-name (evt-name evt)
        [next-state action] (state-pair e-name context matrix)]
    (prn "current state is:" (cur-state context))
    (prn "next state is: " next-state)
    (when action (action context evt))
    next-state))

(defn evt-handler
  "Listen for events on the :main-bus topic. Push the events though the state matrix
  triggering the appropriate behavior."
  []
  (let [sub-ch (sub-evt :main-bus :sm-ch)
        context (atom {:animation-ch  nil
                       :current-state :idle})]
    (go-loop
      []
      (let [evt (<! sub-ch)]
        (println (str "RX SM event: " (evt-name evt)))
        (->>
          (invoke-action context state-matrix evt)
          (set-state context))
        (recur)))))