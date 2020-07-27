(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.string :as str]
            [rest.util :refer :all]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all]
            [honeysql-postgres.format :refer :all]
            [honeysql-postgres.helpers :as psqlh]))

(defn env [v]
  (-> v (name)
      (str/upper-case)
      (str/replace #"-" "_")
      (System/getenv)))

;; (env :dbtype)

;; (def envdb
;;   {:host (env :pghost)
;;    :port (env :pgport)
;;    :user (env :pguser)
;;    :password (env :pgpassword)
;;    :database (env :pgdatabase)})

;; (defn database-url []
;;   (let [conn envdb]
;;     (str "jdbs:postgresql://" {:host conn} ":" (or (:port! conn) (:port conn))
;;          "/" (:database conn)
;;          "?user=" (:user conn)
;;          "&password=" (:password conn))))

;; (defn connection [db-spec]
;;   {:connection (jdbc/get-connection {:connection-uri (database-url db-spec)})})

;; (defn with-connaction-db [db function]
;;   (if-let [conn (jdbc/db-find-connection db)]
;;     (function conn)
;;     (with-open [conn (jdbc/get-connection db)]
;;       function conn)))

;; (defn deleted [db {table :table} id]
;;   (->> {:delete-from table
;;         :where [:= :id id]
;;         :returning [:*]}))

;; (def db {:dbtype "postgresql"
;;          :dbname "test_db"
;;          :host "localhost"
;;          :user "test"
;;          :password "test"})

(def db {:dbtype (env :dbtype)
         :dbname (env :dbname)
         :host (env :dbhost)
         :user (env :dbuser)
         :password (env :dbpassword)
         })

(def person-schema
  [[:per_id :serial "PRIMARY KEY"]
   [:name "VARCHAR (128) NOT NULL"]
   [:male "VARCHAR (1) NOT NULL"]
   [:dateofb "DATE NOT NULL"]
   [:address "VARCHAR(256) NOT NULL"]
   [:policynumber "VARCHAR(256) NOT NULL"]])

(defn honetize [hsql]
  (sql/format hsql))

(defn query [db hsql]
  (let [sql (honetize hsql)]
    (println sql)
    (try
      (let [res (jdbc/query db sql)]
        res)
      (catch Exception e
        (println :query sql)
        (throw e)))))

(defn db-schema-migrated?
  "Check if the schema has been migrated to the database"
  [table]
  (-> (query db {:select [:%count.*]
                 :from [:information_schema.tables]
                 :where [:= :table_name (name table)]})
      first :count pos?))
 
(defn apply-schema-migration [table]
  (when (not (db-schema-migrated? table))
    (jdbc/db-do-commands db (jdbc/create-table-ddl table person-schema))))

(apply-schema-migration :persontest)

;; (def test-q {:select [:per_id]
;;              :from [:persontest]
;;              :where [:= [:name]]})

;; (def test-person {:name "Pavel" :male "M" })

(defn query-first [db & hsql]
  (first (apply query db hsql)))

(defn get-person [select table person]
  (let [where (tr-and-where-sql person)
        res (->> {:select select
                  :from table
                  :where where}
                 (query db))]
    res))

(defn insert [table person]
  (let [hsql (-> (insert-into table)
                 (values person)
                 (psqlh/returning :*))]
    (query db hsql)))

(defn get-all-person [table]
  (->> {:select [:*]
        :from table}
       (query db)))

;(def deleted-all-person (jdbc/execute! db ["DELETE FROM persontest"]))

(defn change [table set]
  (let [trwhere (first (tr-where-sql {:per_id (:per_id set)}))
        hsql (-> (update table)
                 (sset set)
                 (where trwhere)
                 (psqlh/returning :*))]
    (query-first db hsql)))


(defn mdelete [table map-person-id]
  (let [trwhere (tr-and-where-sql map-person-id)
        hsql (-> (delete-from table)
                 (where trwhere)
                 (psqlh/returning :*))]
    (query-first db hsql)))

(-> (delete-from :distributors)
    (where [:did 5 :dname "Gizmo Transglobal"])
    (psqlh/returning :*)
    sql/format)
