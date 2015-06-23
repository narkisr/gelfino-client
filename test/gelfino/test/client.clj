(ns gelfino.test.client
  (:require 
    [cheshire.core :refer (parse-string)]
    [gelfino.client :refer (connect send->)]
    [gelfino.timbre :refer (set-tid gelf-appender)]
    [taoensso.timbre :refer (merge-config! set-config! set-level! refer-timbre)]
    [clojurewerkz.elastisch.rest :as esr]
    [clojurewerkz.elastisch.rest.document :as esd]
    [clojurewerkz.elastisch.query :as q]
    [clojurewerkz.elastisch.rest.response :as esrsp]
    [clojure.pprint :as pp])
  (:use midje.sweet)
  (:import java.text.SimpleDateFormat java.util.Date java.util.UUID) 
  )

(refer-timbre)

(def host "192.168.3.10")

(def url (str "http://" host ":9200"))

(defn uuid [] (.replace (str (UUID/randomUUID)) "-" ""))

(defn kibana-search [id field]
  (let [conn (esr/connect url) date (.format (SimpleDateFormat. "yyyy.MM.dd") (Date.))]
    (get-in 
      (esd/search conn (str "logstash-" date) "logs" :query (q/term field id)) [:hits :total])))

(fact "raw send" :kibana :raw
  (let [id (uuid)]
    (connect) 
    (send-> host {:short_message id :full_message (str "the id is " id)}) => nil
    (Thread/sleep 10000); waiting for kibana to get updated
    (kibana-search id :short_message) => 1))

(fact "timbre send" :kibana :timbre
  (let [id (uuid)]
    (println id)
    (merge-config! {:appenders {:gelf (gelf-appender {:host host})}})
    (set-tid id (info "testing timbre"))
    (Thread/sleep 10000); waiting for kibana to get updated
    (kibana-search id :tid) => 1))

