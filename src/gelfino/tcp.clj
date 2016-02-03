(ns gelfino.tcp
  (:require [gelfino.helpers :refer [msg-hash->json]])
  (:import java.net.Socket))

(defrecord TCPSocket [socket-atom])

(defn connect [a {:keys [host port]}]
  (when @(.socket-atom a) (.close @(.socket-atom a)))
  (reset! (.socket-atom a) (Socket. host port)))

(defn generate-message [msg-hash]
  (-> msg-hash
      msg-hash->json
      (str (char 0))
      (.getBytes "UTF-8")))

(defn send-message [s msg-hash]
  (-> @(.socket-atom s)
      (.getOutputStream)
      (.write (generate-message msg-hash))))
