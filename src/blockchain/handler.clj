(ns blockchain.handler
  (:require [blockchain.db :as db]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def node-identifier "uid")

(defn new-transaction [transaction]
  (db/add-transaction! transaction))

(defn mine []
  (let [last-block (last (db/get-chain))
        proof (db/proof-of-work (:proof last-block))]
    (db/add-transaction! {:sender    "0"
                          :recipient node-identifier
                          :amount    1})
    (db/add-block! proof (db/hash last-block))))

(defroutes app-routes
           (GET "/mine" [] mine)
           (POST "/transactions/new" {transaction :body} new-transaction)
           (GET "/chain" [] (let [chain (db/get-chain)]
                              {:chain  chain
                               :length (count chain)}))
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults api-defaults)))
