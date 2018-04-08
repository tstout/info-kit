(ns info-kit.middleware
  (:require [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (-> handler
      ;;(wrap-defaults site-defaults)
      (wrap-defaults api-defaults)
      wrap-exceptions
      wrap-reload))
