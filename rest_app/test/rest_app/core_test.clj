(ns rest-app.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [rest.core :refer :all]
            [rest.db :as db]
            [rest.apidb :as apidb]
            [clj-time.coerce :as c]
            [clojure.data.json :as json]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [rest.util :refer :all]))

(def test-person {:name "Pavel2"
                   :male "M"
                   :dateofb {:year 1994 :month 05 :day 22}
                   :address "Kairbekova"
                   :policynumber "12345"})

(def test-person-empty {:name ""})

(def map-date {:year 2020 :month 07 :day 19})

(def key-str-date {"year" 2020 "month" 07 "day" 19})

(def list-output-data (vector{:name "Pavel" :dateofb "2020-07-19T19:00:00Z"}
                             {:name "Pavel22" :dateofb "1994-05-22T18:00:44Z"}))

(def list-output-data-res (vector
                           {:name "Pavel" :dateofb {:year 2020 :month 07 :day 19}}
                           {:name "Pavel22" :dateofb {:year 1994 :month 05 :day 22}}))


(def endpoint "/api/people")

(def handler (-> app-routes
                 (wrap-json-body {:keywords? true :bigdecimals? true})))

(defn mock-request [body uri request-method]
  (-> (mock/request request-method uri)
      (mock/body (cheshire/generate-string body))
      (mock/content-type "application/json")))

(deftest tr-data
  (testing "Transform output data"
      (is (= (transform-output-list list-output-data) list-output-data-res))))

(deftest tr-date
  (testing "Transform DATE"
    (is (= (transform-date map-date) (transform-date key-str-date)))))

(let [handler (-> app-routes
                  (wrap-json-body {:keywords? true :bigdecimals? true}))
      request (mock/request :get "/api/people")
      response (handler request)]
  response)

;(print(mock-request {:name "pavel"} "/api/people" :post))

(deftest rest-test
  (testing "Test GET request to /api/people"
    (let [response (handler (mock-request "" endpoint :get))]
      (is (= (:status response) 200))))
  (testing "Test POST request to /people"
    (let [request (mock-request test-person endpoint :post)
          response (handler request)
          response-body (json/read-str (:body response) :key-fn keyword)
          insert-person-id ((first response-body) :per_id)
          insert-person (first response-body)]
      (is (= (:status response) 200))
      ;; (testing "Test PUT request to /people"
      ;;          (let [request (mock-request insert-person endpoint :put)
      ;;                response (handler request)]
      ;;            (is (= (:status response) 200))))
      (testing "Test DELETE request ti /people"
        (let [request (mock-request {:per_id insert-person-id} endpoint :delete)
              response (handler request)]
          (is (= (:status response) 200))))))
  (testing "Test POST empty person to /people"
    (let [request (mock-request test-person-empty endpoint :post)
          response (handler request)]
      (is (= (:status response) 400)))))

(deftest db-test
  (testing "Test good insert, check and remove in DB"
  (let [insert-person test-person]
    (is (not-empty (apidb/add-person insert-person)))
    (let [person-db (apidb/check-person insert-person)]
      (is (not-empty person-db))
      (is (not-empty (apidb/remove-person (first person-db))))))))
