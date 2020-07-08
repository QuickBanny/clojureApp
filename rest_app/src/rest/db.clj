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

(defn db-schema-migrated?
  "Check if the schema has been migrated to the database"
  []
  (-> (jdbc/query db
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='persontest'")])
      first :count pos?))

(defn apply-schema-migration
  "Apply the schema to the database"
  []
  (when (not (db-schema-migrated?))
    (jdbc/db-do-commands db
                         (jdbc/create-table-ddl
                          :persontest [[:per_id :serial "PRIMARY KEY"]
                                       [:name "VARCHAR (128)"]
                                       [:male "VARCHAR (1)"]
                                       [:dateofb "DATE"]
                                       [:address "VARCHAR(256)"]
                                       [:policynumber "VARCHAR(256)"]]))))

(apply-schema-migration)

(defn q [table name male dateofb address policynumber]
  (jdbc/query db [(str "select per_id from "table" where name = ? and  male = ? and dateofb = ? and address = ? and policynumber = ?")  name male dateofb address policynumber]))

(defn insert [table record]
  (first (jdbc/insert! db table record)))

(defn select [table]
  (jdbc/query db [(str "select * from " (name table))]))

;(def deleted-all-person (jdbc/execute! db ["DELETE FROM persontest"]))

(defn change [table update current]
  (jdbc/update! db table update current))

(defn delete [table deleted]
  (jdbc/delete! db table deleted))
