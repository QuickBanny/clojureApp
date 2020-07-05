(ns rest-app.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [rest.core :refer :all]
            [rest.db :as db]
            [rest.apidb :as apidb]
            [clj-time.coerce :as c]
            [clojure.data.json :as json]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

(def atom-person (atom {:name "Pavel2"
                        :male "M"
                        :dateofb {:year 1994 :month 05 :day 22}
                        :address "Kairbekova"
                        :policynumber "12345"}))

(deftest rest-test
  (testing "Test GET request to /api/people"
    (let [response (app-routes (-> (mock/request :get "/api/people")))]
      (is (= (:status response) 200))))
  (testing "Test POST request to /people"
    (let [handler (-> app-routes (wrap-json-body {:keywords? true :bigdecimals? true}))
          json-body (cheshire/generate-string @atom-person)
          request (-> (mock/request :post "/api/people")
                      (mock/body json-body)
                      (mock/content-type "application/json"))
          response (handler request)
          body     (json/read-str (:body response) :key-fn keyword)
          insert-person-id (body :per_id)]
      (is (= (:status response) 200))
      (let [handler (-> app-routes (wrap-json-body {:keywords? true :bigdecimals? true}))
            json-body (cheshire/generate-string @(atom {:per_id insert-person-id}))
            request (-> (mock/request :delete "/api/people")
                        (mock/body json-body)
                        (mock/content-type "application/json"))
            response (handler request)]
        (is (= (:status response) 200))))))

(deftest db-test
  (testing "Test good insert, check and remove in DB"
  (let [insert-person @atom-person]
    (is (not-empty (apidb/add-person insert-person)))
    (let [person-db (apidb/check-person insert-person)]
      (is (not-empty person-db))
      (is (not-empty (apidb/remove-person (get (first person-db) :per_id))))))))
