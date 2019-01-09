(ns info-kit.artifact
  (:require [clojure.edn :as edn]
            [info-kit.db-io :as db-io]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [ring.util.response :refer [response created not-found]])
  (:import (java.text SimpleDateFormat)))

(def ^:dynamic *env* :prod)

;;
;; In the interest of non-clojure clients, all post body and get results
;; are JSON encoded instead of EDN.
;;
(def df (SimpleDateFormat. "EEE MMM d HH:mm:ss zzz yyyy"))

(defn to-json
  "Convert EDN to JSON, with special handling of created date/time"
  [edn]
  (json/write-str edn :value-fn (fn [k v] (if (= k :created) (.format df v) v))))

(defn from-json [json]
  (json/read-str json :key-fn keyword))

(defn create-artifact
  "Create a new artifact. The request map must contain the following:
  {:name string
   :tags vector of tags to associate with the artifact
   :body string}
  "
  [request]
  (let [req-map (from-json request)]
    (log/infof "Received create request: '%s'" (str req-map))
    (->>
      req-map
      (merge {:env *env*})
      db-io/new-artifact
      (str "artifact/")
      created)))

(defn artifact-location
  "Creating an artifact returns the id of the new artifact as
  a Location header. Use this fn to get the value"
  [resp]
  (-> resp :headers (get "Location")))

(defn artifact-id [location]
  (->
    location
    (str/split #"/")
    last
    Integer/parseInt))

(defn create-tag
  "Create a new tag with the specified name. Returns the
  id of the new tag."
  [tag-name]
  (->>
    *env*
    (db-io/add-tag tag-name)
    first
    ((keyword "scope_identity()"))))

(defn assoc-tags [artifact-id tag-ids]
  (db-io/assoc-tags {:env *env* :artifact-id artifact-id :tags tag-ids}))

(defn artifacts-by-tag [tag-id]
  (->>
    *env*
    (db-io/artifacts-by-tag tag-id)
    to-json))

(defn update-artifact [request]
  (let [req-map (from-json request)]
    (log/infof "Received update request: '%s'" (str req-map))
    (->>
      req-map
      (merge {:env *env*})
      db-io/update-artifact
      (str "artifact/")
      created)))

(defn fetch-artifact [id]
  (log/infof "Received read request for artifact: %s" id)
  (if-let [result (db-io/read-artifact {:env *env* :id id})]
    (to-json result)
    (not-found "artifact not found")))

(defn delete-artifact [id]
  (log/infof "Received delete request for artifact: %s" id)
  (db-io/delete-artifact {:env *env* :id id}))

(comment
  (in-ns 'info-kit.artifact)
  (def create-req (info-kit.conf/load-res "create-artifact.json"))
  (def create-resp (create-artifact create-req))
  (def new-artifact-id (-> create-resp artifact-location artifact-id))


  (info-kit.repl/start-server)                              ;; if not already running
  (info-kit.sys/start :dev)

  (-> new-artifact-id fetch-artifact from-json)

  (assoc-tags new-artifact-id [new-tag])


  (update-artifact (to-json {:id new-artifact-id :body "Updated text!!!!!"}))

  )