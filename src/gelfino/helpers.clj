(ns gelfino.helpers
  (:require [cheshire.core :refer [generate-string]])
  (:import java.util.Date))

(def ^{:doc "The basic gelf message form"} msg-template
  {:version  "1.1" :host  "" :short_message  "" :full_message  "" :level  1})

(defn timestamp
  "UNIX millisecond timestamp.
   Spec: Seconds since UNIX epoch with optional decimal places for milliseconds;
   SHOULD be set by client library. Will be set to NOW by server if absent."
  []
  (.divide (BigDecimal. (.getTime (Date.))) (BigDecimal. 1000)))

(defn gelf-format-hash [msg-hash]
  (merge msg-template msg-hash {:timestamp (timestamp)}))

(defn msg-hash->json [msg-hash]
  (-> msg-hash gelf-format-hash generate-string))
