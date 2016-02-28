(ns gelfino.timbre
  "A Gelf based Timbre appender" 
  (:require [taoensso.timbre :as t]
            [clojure.string :refer [join]]
            [gelfino.client :refer [send-> lazy-connect]]
            [gelfino.protocol :as pr]))

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
   tid)
  

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
  [{:keys [level ?err_ vargs_ msg_ ?ns-str hostname_ timestamp_] :as args}]
  (let [msg (force msg_) ?err (force ?err_)
        res  {:short_message msg :full_message msg :level (levels level) :host (force hostname)}]
    (if ?err (merge res {:error (t/stacktrace ?err) :message (.getMessage ?err)}) res)))

(defn appender
  "Usage:
    (appender {:host \"127.0.0.1\"
               :port 12201
               :socket-constructor gelfino.tcp/socket
               :min-level :warn})"
  [{:keys [host port socket-constructor min-level]}]
  (let [socket (socket-constructor host port)]
    {:min-level (or min-level :debug)
     :enabled? true
     :async? false
     :rate-limit nil
     :output-fn :inherit
     :fn (fn [data] (pr/send-message socket (append-tid (format-message data))))}))

(defn gelf-appender [{:keys [host min-level]}]
  {:min-level (or min-level :debug)
   :enabled?  true
   :async?    false
   :rate-limit nil 
   :output-fn  :inherit
   :fn (fn [data]
         (lazy-connect)
         (send-> host (append-tid (format-message data))))})
