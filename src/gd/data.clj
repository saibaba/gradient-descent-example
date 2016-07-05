(ns gd.data
  (:require [incanter.core :as i]
            [incanter.io :as io]))

(defn unknown-data []
  (-> (io/read-dataset "data/data.csv" :header true)))

