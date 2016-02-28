(ns gelfino.udp
  (:require [gelfino.helpers :refer [msg-hash->json]])
  (:import 
    (java.net InetSocketAddress InetAddress DatagramSocket DatagramPacket)
    (java.nio ByteBuffer channels.DatagramChannel)
    java.io.ByteArrayOutputStream 
    java.security.MessageDigest
    java.lang.System 
    java.util.Arrays
    java.util.Date
    java.util.zip.GZIPOutputStream)) 

(defrecord UDPSocket [socket-atom ids])

(defn init-channel [addr]
  (let [r (DatagramChannel/open)]
    (.. r (socket) (bind (InetSocketAddress. 0)))
    (.connect r addr)
    (.configureBlocking r false)
    r))

(defn connect [s {:keys [host port]}]
  (when @(.socket-atom s) (.close @(.socket-atom s)))
  (reset! (.socket-atom s) (init-channel (InetSocketAddress. host port)))
  s)

(defn reconnect [s]
  (let [addr (.getRemoteAddress @(.socket-atom s))]
    (.close @(.socket-atom s))
    (reset! (.socket-atom s) (init-channel addr))))

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

(def max-chunk-size {:lan 8154 :wan 1420})

(defn- chunk-range
  "Form sequence of tuples (start-byte end-byte)"
  [c-size len]
  (let [exc (into [] (interleave (range 0      len c-size)
                                 (range c-size len c-size)))] 
   (partition-all 2 (conj exc (last exc) len))))

(defn bytes->buffer [bytes-msg]
  (let [bb (ByteBuffer/allocate (alength bytes-msg))]
    (.put bb bytes-msg)
    (.flip bb)
    bb))

(defn- id [s] 
  (swap! (.ids s) inc)
  (.getBytes (String. (.substring (md5 (str @(.ids s) (.getTime (Date.)))) 0 8))))

(defn- header
  "Forms a Gelf chunk header" 
  [i d t]
  (++ (byte-array [(byte 0x1e) (byte 0x0f)]) (++ d (byte-array [(byte i) (byte t)]))))

(defn- chunks 
  "Generates chunks out of a compressed message"
  [s ^"[B" comp-m]
  (let [csr (chunk-range (max-chunk-size :lan) (alength comp-m)) d (id s)]
    (map (fn [[^Long s ^Long e] i] 
           (bytes->buffer (++ (header i d (count csr)) (Arrays/copyOfRange comp-m s e))) csr (range)))))

(defn send-chunk [s data]
  (-> @(.socket-atom s) (.write data)))

(defn generate-message [s msg-hash]
  (let [bytes-msg (gzip (msg-hash->json msg-hash))]
    (if (> (alength bytes-msg) (max-chunk-size :lan))
        (chunks s bytes-msg)
        [(bytes->buffer bytes-msg)])))

(defn send-message [s msg-hash]
  (doseq [chunk (generate-message s msg-hash)] (send-chunk s chunk)))

(defn socket [host port]
  (connect (UDPSocket. (atom nil) (atom 0)) {:host host :port port}))
