(ns gelfino.test.client
  (:require 
    [cheshire.core :refer (parse-string)]
    [clj-http.client :as client]
    [gelfino.client :refer (connect send->)])
  (:use midje.sweet))


(def host "192.168.1.10")

(defn query-id [id]
 (str "{\"query\": {\"filtered\": {\"query\": {\"bool\":{\"should\":[{\"query_string\":{\"query\":\"" id "\"}}]}}}}}") )

(defn kibana-search 
  [id]
  (-> 
    (client/post (str "http://" host ":9200/logstash-2014.08.13/_search") {:body (query-id id) :content-type :json })
    :body (parse-string true) 
    :hits :hits count))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(fact "sanity" filters
      (let [id (uuid)] 
        (connect) 
        (send-> host {:short_message id :message (str "the id is" id)})
        (client/post (str "http://" host ":9200/logstash-2014.08.13/_search") {:body all :content-type :json })
        (Thread/sleep 1000); waiting for kibana to get updated
        (kibana-search id) => 1
        )



      )
