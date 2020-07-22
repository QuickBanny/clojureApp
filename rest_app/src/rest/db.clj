(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.string :as str]
            ;[db.honey :as honey :refer [pr-error]]
            [honeysql.core :as sql]))

(defn env [v]
  (-> v (name)
      (str/upper-case)
      (str/replace #"-" "_")
      (System/getenv)))

(defn db-spec-from-env []
  {:host (env :pghost)
   :port (env :pgport)
   :user (env :pguser)
   :password (env :pgpassword)
   :database (env :pgdatabase)})

(defn database-url [spec]
  (let [conn spec]
    (str "jdbs:postgresql://" {:host conn} ":" (or (:port! conn) (:port conn))
         "/" (:database conn)
         "?user=" (:user conn)
         "&password=" (:password conn))))

(defn connection [db-spec]
  {:connection (jdbc/get-connection {:connection-uri (database-url db-spec)})})

(defn with-connaction-db [db function]
  (if-let [conn (jdbc/db-find-connection db)]
    (function conn)
    (with-open [conn (jdbc/get-connection db)]
      function conn)))

(defn transform-honey-sql [hsql]
  (cond (map? hsql) (sql/format hsql :quoting :ansi)))

(transform-honey-sql{:select [:id]
                     :from [:test]})

(transform-honey-sql {"select" [:id]
                      :from ["test"]})

(defn query [db hsql]
  (let [sql (transform-honey-sql hsql)]
    (try
      (let [res (jdbc/query db sql)]
        res)
      (catch Exception e
        (println :query sql)
        (throw e)))))

(defn insert [db {table :table :as spec} data]
  (let [values (if (vector? data) data [data])]))

(defn deleted [db {table :table} id]
  (->> {:delete-from table
        :where [:= :id id]
        :returning [:*]}))


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
  (jdbc/query db [(str "select per_id from "table" where name = ? and  male = ? and dateofb = ? and address = ? and policynumber = ?") name male dateofb address policynumber]))

(defn insert [table record]
  (first (jdbc/insert! db table record)))

(defn select [table]
  (jdbc/query db [(str "select * from " (name table))]))

;(def deleted-all-person (jdbc/execute! db ["DELETE FROM persontest"]))

(defn change [table update current]
  (jdbc/update! db table update current))

(defn delete [table deleted]
  (jdbc/delete! db table deleted))
