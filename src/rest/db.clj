(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [clj-time.core :as t]
            [rest.db :as db]
            [clj-time.coerce :as c]))

(def db {:dbtype "postgresql"
            :dbname "test_db"
            :host "localhost"
            :user "test"
            :password "test"})

(def person-sql (jdbc/create-table-ddl :persontest [[:per_id :serial "PRIMARY KEY"]
                                            [:name "VARCHAR (128)"]
                                            [:male "VARCHAR (1)"]
                                            [:dateofb "DATE"]
                                            [:address "VARCHAR(256)"]
                                            [:policynumber "VARCHAR(256)"]]))

(defn check-person [name male dateofb address policynumber]
  (let [person (jdbc/query db ["SELECT name, per_id FROM persontest WHERE name = ?"
                               name])] person))

(def all-person (jdbc/query db ["SELECT * FROM persontest"]))

(def deleted-all-person (jdbc/execute! db ["DELETE FROM persontest"]))


(defn update-person [update current]
  (jdbc/update! db :persontest update current))

(defn remove-person [deleted]
  (jdbc/delete! db :persontest deleted))

(defn insert-person [name male dateofb address policymber]
  (if (= () (check-person name male dateofb address policymber))
    (jdbc/insert! db :persontest
                {:name name :male male
                 :dateofb (c/to-sql-date dateofb) :address address
                 :policynumber policymber})))

;(def exec-table (jdbc/execute! db [person-sql]))

(c/to-sql-date "2019-02-01")
