(ns mashape1.prod
  (:require [mashape1.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
