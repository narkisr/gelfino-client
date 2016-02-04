(ns gelfino.tcp
  (:require [gelfino.helpers :refer [timestamp msg-hash->json]])
  (:import java.net.Socket))

(defrecord TCPSocket [socket-atom last-operation])

(defn connect [s {:keys [host port]}]
  (when @(.socket-atom s) (.close @(.socket-atom s)))
  (reset! (.socket-atom s) (Socket. host port))
  s)

(defn reconnect [s]
  (let [host (.getHostAddress (.getInetAddress @(.socket-atom s)))
        port (.getPort @(.socket-atom s))]
    (connect s {:host host :port port})))

(defn generate-message [msg-hash]
  (-> msg-hash
      msg-hash->json
      (str (char 0))
      (.getBytes "UTF-8")))

(defn need-reconnect? [s ts]
  (> (- ts (:last-op @(.last-operation s)))
     (:timeout @(.last-operation s))))

(defn update-last-operation [s]
  (swap! (.last-operation s) assoc :last-op (timestamp)))

(defn send-message [s msg-hash]
  (let [socket (if (or (.isClosed @(.socket-atom s))
                       (need-reconnect? s (timestamp)))
                 (reconnect s)
                 s)]
    (prn socket)
    (update-last-operation socket)
    (-> @(.socket-atom socket)
        (.getOutputStream)
        (.write (generate-message msg-hash)))))


(def default-timeout 60)
(defn socket 
  ([host port timeout]
   (connect (TCPSocket. (atom nil) (atom {:timeout timeout :last-op (timestamp)}))
            {:host host :port port}))
  ([host port] (socket host port default-timeout)))
