(ns rest.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [rest.apidb :as apidb]
            [ring.util.response :refer [response]]
            [ring.util.http-response :refer :all]
            [ring.middleware.json :refer [wrap-json-response]]
            [clojure.java.io :refer (resource)]
            [selmer.parser :refer [render-file]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.session :refer :all]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]])
  (:gen-class))

(use 'ring.util.json-response)

(defmacro deftmpl
  "Read template from file in resources/"
  [symbol-name html-name]
  (let [content (slurp (resource html-name))]
    `(def ~symbol-name
       ~content)))

(defn get-custom-token [request]
  (get-in request [:headers "x-forgery-token"]))

(defn main-page [req]
  (render-file "templates/index.selmer" {:debug (-> req :component :config :debug)
                                         :csrf-token *anti-forgery-token*}))

(defn people-list [req]
  (try
    (let [people (apidb/get-people)]
      (json-response people))
      (catch Exception e
        (pp/pprint e)
        (bad-request "Error get peole"))))

(defn people-insert [req]
  (try
    (let [body (:body req)
          person (apidb/add-person body)]
        (json-response person))
    (catch Exception e
      (pp/pprint e)
      (bad-request "Error people insert"))))

(defn people-remove [req]
  (try
    (let [body (:body req)
          person (apidb/remove-person (get body "per_id"))]
      (json-response person))
    (catch Exception e
      (pp/pprint e)
      (bad-request "Error person remove"))))

(defn people-change [req]
  (try
    (let [body (:body req)
          person (apidb/update-person body)]
      (json-response person))
    (catch Exception e
      (pp/pprint e)
      (bad-request "Error person changed"))))

(defn checki-person [req]
  (try
    (let [body (:body req)
          person (apidb/check-person body)]
      (json-response person))
    (catch Exception e
      (pp/pprint e)
      (bad-request "Error check-person"))))

(defroutes app-routes
  (GET "/" [] main-page)
  (GET "/api/people" [] people-list)
  (GET "/api/check-person" [] check-person)
  (POST "/api/people" [] people-insert)
  (PUT "/api/people" [] people-change)
  (DELETE "/api/people" [] people-remove)
  (route/not-found "Error, page found!"))


(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (->
                        (wrap-defaults #'app-routes site-defaults)
                        wrap-keyword-params
                        wrap-params
                        wrap-json-response
                        (wrap-json-body routes {:keywords? true})
                        wrap-session)
                       {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
