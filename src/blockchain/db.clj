(ns blockchain.db
  (:require [digest]
            [clojure.string :as str]))

(def ^:private chain (atom []))
(def ^:private current-transactions (atom []))

(defn get-chain [] @chain)

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
