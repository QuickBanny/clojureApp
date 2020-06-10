(ns rest.db
  (:require [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hugsql.core :as hugsql]))

(def db {:dbtype "postgresql"
            :dbname "test_db"
            :host "localhost"
            :user "test"
            :password "test"})

(def person (jdbc/create-table-ddl :person [[:id :serial "PRIMARY KEY"]
                                            [:name "VARCHAR (128)"]
                                            [:male "VARCHAR (1)"]
                                            [:dateofb "DATE"]
                                            [:address "VARCHAR(256)"]
                                            [:policynumber "VARCHAR(256)"]]))

(defn check-person [name male dateofb address polocynuber]
  )

(defn update-person [name male dateofb address polocynumber]
  )

(defn remove-perosn [id]
  )

(defn insert-person [name male dateofb address policymber]
  )

(defn insert-multiply-person []
  )

(println person)
(jdbc/execute! db [person])
