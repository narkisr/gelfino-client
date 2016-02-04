(ns gelfino.core
  (:require [gelfino.tcp :as tcp]
            [gelfino.udp :as udp]
            [gelfino.protocol :refer :all]
            [gelfino.timbre :as timbre]))

(def tcp-socket tcp/socket)
(def udp-socket udp/socket)
