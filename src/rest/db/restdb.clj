(ns rest.db.restdb
  :(require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "rest/db/sql/rest.sql")

