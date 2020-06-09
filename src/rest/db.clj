(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbs :as jdbs]))

(def spec
  (pg/pool
   {:host "localhost:5432/rest"
    :dbname "restdb"
    :user "admin"
    :password ""}))


