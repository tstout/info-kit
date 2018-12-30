(ns info-kit.handler
  (:require [compojure.core :refer [GET POST DELETE defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [info-kit.middleware :refer [wrap-middleware]]
            [info-kit.artifact :refer [create-artifact fetch-artifact delete-artifact]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defroutes routes
           (GET "/" [] (loading-page))
           (GET "/about" [] (loading-page))
           (GET "/ping" [] "some json response")
           (POST "/artifact" request (create-artifact (slurp (:body request))))
           (GET "/artifact/:id" [id] (fetch-artifact id))
           (DELETE "/artifact/:id" [id] (delete-artifact id))

           (resources "/")
           (not-found " Not Found "))

(def app (wrap-middleware #'routes))
