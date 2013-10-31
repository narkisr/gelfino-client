(ns gelfino.timbre
  "A Gelf based Timbre appender" 
  (:require [taoensso.timbre :as t])
  (:use 
    [clojure.string :only (join)] 
    [gelfino.client :only (send-> lazy-connect) ]))


(def ^{:doc "A transaction id that can be used to trace back a logical log flow in central logging 
             systems like Kibana/Graylog2" 
       :dynamic true :private true} tid nil)

(defmacro set-tid 
   "Sets tid (transaction id) for current logs,
    this id is later used in search to track a logical transaction (in graylog2 or kibana) of a series of logs.
   "
   [tid* & body]
  `(binding [tid ~tid*] ~@body))

(defn get-tid []
   "Gets current tid" 
   tid
  )

(def ^{:doc "Timbre level to Gelf log level see http://bit.ly/154nolw note that trace isn't defined"}
  levels {:trace -1 :debug 0 :info 1 :warn 2 :error 3 :fatal 4 :unknown 5})

(def ^{:doc "Logging machine hostname"}
  hostname (.getHostName (java.net.InetAddress/getLocalHost)))

(defn- append-tid 
   "Appends transaction id number if exists" 
   [m]
  (if tid (assoc m :_tid tid) m))

(defn format-message 
  "formats message for sending" 
  [{:keys [config level prefix throwable message] :as args}]
    (let [res  {:short_message message :full_message message 
                :level (levels level) :facility "gelfino" :host hostname}]
      (if throwable 
        (merge res {:error (t/stacktrace throwable) :message (.getMessage throwable)})
        res) 
      ))

(def ^{:doc "A Gelf append for Timbre"}
  gelf-appender
  {:doc       "A gelfino based appender"
   :min-level :debug
   :enabled?  true
   :async?    false
   :limit-per-msecs nil 
   :fn 
   (fn [{:keys [ap-config] :as args}]
     (lazy-connect)
     (send-> (get-in ap-config [:gelf :host]) (append-tid (format-message args))))})
