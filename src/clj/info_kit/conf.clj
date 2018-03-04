(ns info-kit.conf
  (:require [mount.core :as mount :refer [defstate]]
            [clojure.edn :as edn]
            [clojure.tools.logging :refer [info]]
            [clojure.java.io :as io]))


(defn load-res [res]
  (-> res
      io/resource
      slurp))

(defn load-edn-resource [res]
  (->> res
       io/resource
       slurp
       edn/read-string))