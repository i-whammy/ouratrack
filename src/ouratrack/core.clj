(ns ouratrack.core
  (:require [clj-http.client :as http]
            [clojure.spec.alpha :as s]
            [ouratrack.config :as config]))

(def ^:private headers {"Authorization" (str "Bearer " (config/token))})

(defn- fetch-sleep-scores [start end]
  (:body (http/get
          (format "https://api.ouraring.com/v1/sleep?start=%s&end=%s" start end)
          {:headers headers
           :as :json})))

(defn- <=date [start end]
  (>= 0 (.compareTo (java.time.LocalDate/parse start)
                    (java.time.LocalDate/parse end))))

(defn- get-duration-date [start end]
  (+ 1 (.between java.time.temporal.ChronoUnit/DAYS
                                     (java.time.LocalDate/parse start)
                                     (java.time.LocalDate/parse end))))

(defn fetch-average-readiness-score [start end]
  )

(s/fdef fetch-average-sleep-score
  :args (s/and (s/cat :start string? :end string?)
               (s/cat :start #(re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}" %) :end #"[0-9]{4}-[0-9]{2}-[0-9]{2}")
               #(<=date (:start %) (:end %)))
  :ret number?)
(defn fetch-average-sleep-score [start end]
  (let [total-sleep-score (->> (fetch-sleep-scores start end)
                               :sleep
                               (reduce (fn [acc sleep] (+ acc (:score sleep))) 0))
        duration-date  (get-duration-date start end)]
    (quot total-sleep-score duration-date)))
