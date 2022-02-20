(ns ouratrack.config 
  (:require [environ.core :refer [env]]))

(def token
  (env :token))