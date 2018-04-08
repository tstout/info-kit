(ns info-kit.middleware
  (:require [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]))

(defn wrap-middleware [handler]
  (wrap-defaults handler api-defaults))
