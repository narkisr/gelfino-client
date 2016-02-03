(ns gelfino.protocol
  (:require [gelfino.tcp :as tcp]
            [gelfino.udp :as udp]))

(defprotocol GELF
  "This protocol defines api for different ways to send
   messages to graylog."
   (connect [self & params])
   (generate-message [self msg-hash])
   (send-message [self msg-hash]))

(extend-protocol GELF
  TCPSocket
    (connect [self params] (tcp/connect self params))
    (generate-message [self msg-hash] (tcp/generate-message msg-hash))
    (send-message [self msg-hash] (tcp/send-message self msg-hash))
  UDPSocket
    (connect [self params] (udp/connect self params))
    (generate-message [self msg-hash] (udp/generate-message msg-hash))
    (send-message [self msg-hash] (udp/send-message self msg-hash)))
