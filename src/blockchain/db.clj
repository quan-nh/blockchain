(ns blockchain.db
  (:require [digest]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(def ^:private chain (atom []))
(def ^:private current-transactions (atom []))

(defn get-chain [] @chain)

(s/fdef add-transaction!
        :args (s/cat :transaction :blockchain.spec/transaction)
        :ret integer?)

(defn add-transaction! [transaction]
  (swap! current-transactions conj transaction)
  (-> @chain last :index inc))

(defn hash [block]
  (digest/sha-256 (str block)))

(defn add-block! [proof prev-hash]
  (let [block {:index        (inc (count @chain))
               :timestamp    (System/currentTimeMillis)
               :transactions @current-transactions
               :proof        proof
               :prev-hash    (or prev-hash (hash (last @chain)))}]
    (swap! chain conj block)
    (reset! current-transactions [])
    block))

(defn valid-proof? [last-proof proof]
  (->> (digest/md5 (str last-proof proof))
       (str/starts-with? "0000")))

(defn proof-of-work [last-proof]
  (first (drop-while (comp not #(valid-proof? last-proof %)) (range))))
