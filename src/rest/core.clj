(ns rest.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [rest.apidb :as apidb])
  (:gen-class))

(defn main-page [req])


(defn request-example [req]
  {:status 200
   :header {"Content-type" "text/html"}
   :body (->>
          (pp/pprint req)
          (str "Request objects: " req))})

(defn people-list [req]
  {:status 200
   :header {"Contente-type" "text/json"}
   :body (apidb/get-people)})

(people-list {})
(defn people-insert [req]
  {:status 200
   :headers {"Content-type" "text/json"}
   :body (apidb/add-person (req :params))})

(defn people-remove [req]
  {:status 200
   :header {"Content-type" "text/json"}
   :body (apidb/remove-person)})

(defn people-change [req]
  {:status 200
   :header {"Content-type" "text/json"}
   :body (apidb/update-person (req :params))})

(defroutes app-routes
  (GET "/" [] main-page)
  (GET "/api/request" [] request-example)
  (GET "/api/people" [] people-list)
  (POST "/api/people" [] people-insert)
  (PUT "/api/people" [] people-change)
  (DELETE "/api/people" [] people-remove)
  (route/not-found "Error, page found!"))

(people-list {})

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
