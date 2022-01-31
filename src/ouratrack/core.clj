(ns ouratrack.core
  (:require [clj-http.client :as http]
            [environ.core :refer [env]]
            [clojure.spec.alpha :as s]))

(def ^:private token (env :token))
(def ^:private headers {"Authorization" (str "Bearer " token)})

(defn- fetch-sleep-scores [start end]
  (:body (http/get
          (format "https://api.ouraring.com/v1/sleep?start=%s&end=%s" start end)
          {:headers headers
           :as :json})))

(defn- <=date [start end]
  (>= 0 (.compareTo (java.time.LocalDate/parse start)
                    (java.time.LocalDate/parse end))))

(s/fdef fetch-average-sleep-score
  :args (s/and (s/cat :start string? :end string?)
               (s/cat :start #(re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}" %) :end #"[0-9]{4}-[0-9]{2}-[0-9]{2}")
               #(<=date (:start %) (:end %)))
  :ret number?)
(defn fetch-average-sleep-score [start end]
  (let [total-sleep-score (->> (fetch-sleep-scores start end)
                               :sleep
                               (reduce (fn [acc sleep] (+ acc (:score sleep))) 0))
        duration-date (+ 1 (.between java.time.temporal.ChronoUnit/DAYS
                                     (java.time.LocalDate/parse start)
                                     (java.time.LocalDate/parse end)))]
    (quot total-sleep-score duration-date)))
