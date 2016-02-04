(ns gelfino.client
  "The Gelf client and protocol implementation, can be used in conjunction with any logging frameworks"
  (:require [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io])
  (:import 
    (java.net InetSocketAddress DatagramSocket DatagramPacket)
    java.io.ByteArrayOutputStream 
    java.security.MessageDigest
    java.net.InetAddress
    java.lang.System 
    java.util.Arrays
    java.util.zip.GZIPOutputStream 
    java.util.Date)) 

(defn- max-chunk-size 
  ([] (max-chunk-size :lan)) 
  ([k] ({:wan 1420 :lan 8154} k)))

(def ^Long port 12201)

(def ^:private client-socket (atom nil))

(def ids (atom 0)) 

(defn connect 
  "Creating a datagram udp socket"
  [] 
  (reset! ids 0)
  (when @client-socket (.close @client-socket))
  (reset! client-socket (DatagramSocket.)))

(defn lazy-connect
   "checks if we have a working connection and only connect if there is none" 
   []
   (when-not @client-socket (connect)))


(def ^{:doc "The basic gelf message form" :private true} message-template
  {:version  "1.1" :host  "" :short_message  "" :full_message  "" :timestamp  0 :level  1})

(defn- raw-send 
  "Sending raw bytes through the socket"
  [^"[B" data to]
  (.send ^DatagramSocket @client-socket 
         (DatagramPacket. data (alength data) (InetAddress/getByName to) port)))

(defn- gzip 
  "Compresses a string" 
  [^String message]
  (with-open [bos (ByteArrayOutputStream.) stream (GZIPOutputStream. bos)]
    (.write stream (.getBytes message)) 
    (.finish stream)
    (.toByteArray bos)))

(defn- ++ 
  "Combines two byte arrays to one" 
  [^"[B" f ^"[B" s]
  (let [f-l (alength f) s-l (alength s)
        res (byte-array (+ f-l s-l))]
    (System/arraycopy f 0 res 0 f-l) 
    (System/arraycopy s 0 res f-l s-l) 
    res))
    

(defn- md5 
  "An md5 hash signature on a given token"
  [token]
  (let [hash-bytes (doto (MessageDigest/getInstance "MD5") (.reset) (.update (.getBytes token)))]
    (.toString (new java.math.BigInteger 1 (.digest hash-bytes)) 16)))

(defn- id [] 
  (swap! ids inc)
  (.getBytes (String. (.substring (md5 (str @ids (.getTime (Date.)))) 0 8))))

(defn- chunk-range [c-size len]
  (let [exc (into [] (interleave (range 0 len c-size) (range c-size len c-size)))]
    (partition-all 2 (conj exc (last exc) len))))

(defn- header
  "Forms a Gelf chunk header" 
  [i d t]
  (++ (byte-array [(byte 0x1e) (byte 0x0f)]) (++ d (byte-array [(byte i) (byte t)]))))

(defn- chunks 
  "Generates chunks out of a compressed message"
  [^"[B" comp-m]
  (let [csr (chunk-range (max-chunk-size) (alength comp-m)) d (id)]
    (map (fn [[^Long s ^Long e] i] 
           (++ (header i d (count csr)) (Arrays/copyOfRange comp-m s e))) csr (range))))

(defn ts
  "UNIX millisecond timestamp.
   Spec: Seconds since UNIX epoch with optional decimal places for milliseconds;
   SHOULD be set by client library. Will be set to NOW by server if absent."
  []
  (.divide (BigDecimal. (.getTime (Date.))) (BigDecimal. 1000)))

(defn hash->msg
  "Convert user hash to compressed bytes array"
  [hsh]
  (gzip (generate-string (merge message-template hsh {:timestamp (ts)}))))

(defn send-> 
  "Sends a message m to a Gelf server host to" 
  ([to m] 
   (let [^"[B" comp-m (hash->msg m)]
     (if (> (alength comp-m) (max-chunk-size))
       (doseq [c (chunks comp-m)] (raw-send c to))
       (raw-send comp-m to))))) 


(comment 
  "Basic usage:"

  (connect) 
  (send-> "192.168.3.117" 
    {:short_message "i am a unicorn" :message (apply str (take 400000 (repeat "I am a unicorn")))}) 
  (dotimes [i 5] (send-> "192.168.3.117" {:short_message "i am a unicorn" :full_message "i am a unicorn too" :level 1 :facility "geflino"})) 


  (defn random-string [length]
    (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
      (apply str (repeatedly length #(char (rand-nth ascii-codes))))))) 

