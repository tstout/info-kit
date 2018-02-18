(ns info-kit.prod
  (:require [info-kit.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
