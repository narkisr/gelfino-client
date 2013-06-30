(defproject com.narkisr/gelfino-client "0.4.2"
  :description "A Gelf logging client including a timbre adapter"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.taoensso/timbre "1.5.2"]
                 [cheshire "5.0.2"]]
  
  :plugins  [[lein-tag "0.1.0"] [codox "0.6.4"]]

  :profiles {:dev {:dependencies [ ]}}

)
