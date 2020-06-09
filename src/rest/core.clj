(ns rest.core

  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))


(defn main-page [req]
  {:status 200
   :header {"Content-type" "text-html"}
   :body "Hello_world"})

(def people-collection (atom []))

(defn addperson [firstname surname]
  (swap! people-collection conj{:firstname (str/capitalize firstname) :surname(str/capitalize surname)}))

(defn changeperson [firstname surname])

(defn getparameter [req pname] (get (:params req) pname))

(addperson "Functional" "Human")
(addperson "Micky" "Mouse")

(defn request-example [req]
  {:status 200
   :header {"Content-type" "text/html"}
   :body (->>
          (pp/pprint req)
          (str "Request objects: " req))})

(defn people-list [req]
  {:status 200
   :header {"Contente-type" "text/json"}
   :body (str (json/write-str @people-collection))})

(defn add-person [req]
  {:status 200
   :headers {"Content-type" "text/json"}
   :body (-> (let [p (partial getparameter req)]
               (str (json/write-str (addperson (p :fio) (p :male) (p :dateofb) (p :address) (p :policynumber))))))})

(defn remove-person [req]
  {:status 200
   :header {"Content-type" "text/json"}
   :body ()})

(defn change-person [req]
  {:status 200
   :header {"COntent-type" "text/json"}
   :body (-> (let [p (partial getparameter req)]
               (str (json/write-str (changeperson (p :fio) (p :male) (p :dateofb) (p :address) (p :policynumber))))))})

(defroutes app-routes
  (GET "/" [] main-page)
  (GET "/api/people" [] people-list)
  (POST "/api/add_person" [] add-person)
  (PUT "/api/change_person" [] change-person)
  (DELETE "/api/remove_person" [] remove-person)
  (route/not-found "Error, page found!"))


(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
