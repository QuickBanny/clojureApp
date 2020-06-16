(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [clj-time.core :as t]
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

(defn q [table name male dateofb address policynumber]
  (jdbc/query db [(str "select per_id from "table " where name = ? and  male = ? and dateofb = ? and address = ? and policynumber = ?")  name male dateofb address policynumber]))

(defn insert [table record]
  (first (jdbc/insert! db table record)))

(defn select [table]
  (jdbc/query db [(str "select * from " (name table))]))

(select :persontest )
;(def deleted-all-person (jdbc/execute! db ["DELETE FROM persontest"]))

(defn change [table update current]
  (jdbc/update! db table update current))

(defn delete [table deleted]
  (jdbc/delete! db table deleted))

(defn insert-person [table record]
  (if (= () (q (name table) (get record :name) (get record :male) (get record :dateofb) (get record :address) (get record :policynumber)))
    (jdbc/insert! db table record)))


;(def exec-table (jdbc/execute! db [person-sql]))

