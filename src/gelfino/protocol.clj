(ns gelfino.protocol
  (:require [gelfino.tcp :as tcp])
  (:import [gelfino.tcp TCPSocket]))

(defprotocol GELF
  "This protocol defines api for different ways to send
   messages to graylog."
   (connect [self params] "bind socket to host/port")
   (reconnect [self] "close old socket and open new")
   (generate-message [self msg-hash] "convert msg-hash to format ready to send")
   (send-message [self msg-hash] "generate and send message"))

(extend-protocol GELF
  TCPSocket
    (connect [self params] (tcp/connect self params))
    (reconnect [self] (tcp/reconnect self))
    (generate-message [self msg-hash] (tcp/generate-message self msg-hash))
    (send-message [self msg-hash] (tcp/send-message self msg-hash)))
;  UDPSocket
;    (connect [self params] (udp/connect self params))
;    (reconnect [self] (udp/reconnect self))
;    (generate-message [self msg-hash] (udp/generate-message self msg-hash))
;    (send-message [self msg-hash]) (udp/send-message self msg-hash))
