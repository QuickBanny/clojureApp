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
            [selmer.parser :refer [render-file]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.session :refer :all]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [cheshire.core :as cheshire]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]])
  (:gen-class))

(use 'ring.util.json-response)

(defn main-page [req]
  (render-file "templates/index.selmer" {:debug (-> req :component :config :debug)
                                         :csrf-token *anti-forgery-token*}))

(defn people-list [req]
  (try
    (let [people (apidb/get-people)]
      (json-response people))
      (catch Exception e
        ;(pp/pprint e)
        (bad-request "Error get people"))))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn people-insert [req]
  (try
    (let [body (cheshire/generate-string (:body req))
          map-body (json/read-json body)
          true-keys (contains-many? map-body :name :male :dateofb :address :policynumber)]
      (if (and true-keys (empty (apidb/check-person map-body)))
        (json-response (apidb/add-person map-body))
        (bad-request "TRUE Person in DB")))
    (catch Exception e
      ;(pp/pprint e)
      (bad-request "Error people insert"))))

(defn people-remove [req]
  (try
    (let [body (cheshire/generate-string (:body req))
          map-body (json/read-json body)
          person-id (map-body :per_id)
          person (apidb/remove-person person-id)]
      (json-response person))
    (catch Exception e
      ;(pp/pprint e)
      (bad-request "Error person remove"))))

(defn people-change [req]
  (try
    (let [body (cheshire/generate-string (:body req))
          map-body (json/read-json body)
          person (apidb/update-person map-body)]
      (json-response person))
    (catch Exception e
      ;(pp/pprint e)
      (bad-request "Error person changed"))))

(defn check-person [req]
  (try
    (let [body (:query-params req)
          person (apidb/check-person body)]
      (json-response person))
    (catch Exception e
      ;(pp/pprint (:query-params req))
      ;(pp/pprint e)
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
                        (wrap-defaults #'app-routes (merge site-defaults
                                       {:security {:anti-forgery false}}))
                        (wrap-json-body routes {:keywords? true})
                        wrap-keyword-params
                        wrap-params
                        wrap-json-response
                        ;wrap-anti-forgery
                        wrap-session)
                       {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
