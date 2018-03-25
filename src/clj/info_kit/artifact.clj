(ns info-kit.artifact
  (:require [clojure.edn :as edn]
            [info-kit.db-io :as db-io]
            [taoensso.timbre :as log]
            [ring.util.response :refer [response created]]
            [info-kit.db :refer [h2-local]]))

(defn create-artifact [request]
  (let [req-map (edn/read-string request)]
    (log/infof "Received create request: '%s'" (str req-map))
    (->>
      req-map
      (merge {:db-spec h2-local})
      db-io/new-artifact
      (str "artifact/")
      created)))

(defn update-artifact [])
(defn read-artifact [])

(defn delete-artifact [])