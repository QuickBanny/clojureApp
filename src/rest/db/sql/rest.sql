-- :name create-rest-table :!
CREATE TEBLE IF NOT  EXIST rest (
  id bigserial PRIMARY KEY,
  name varchar NOT NULL,
  male varchar NOT NULL,
  dateofb date NOT NULL,
  address varchar NOT NULL,
  policynumber serial NOT NULL,
  created_at timestamp NOT NULL default current_timestamp
)

--:name drop-rest-table :!
DROP TABLE IF EXIST rest
