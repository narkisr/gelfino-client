(defproject com.narkisr/gelfino-client "0.8.1"
  :description "A Gelf logging client including a timbre adapter"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.2.1"]
                 [cheshire "5.5.0"]]

  :profiles {:dev {:dependencies [[midje "1.8.3"]
                                  [clj-http "2.0.1"]
                                  [clojurewerkz/elastisch "2.2.1"]]
              
                   :repl-options {:init-ns gelfino.core}
                   :set-version {:updates [{:path "README.md" :search-regex #"\"\d+\.\d+\.\d+\""}]}}}
  
  :plugins  [[lein-ancient "0.6.7" :exclusions [org.clojure/clojure]] 
             [lein-tag "0.1.0"]
             [codox "0.8.10"] 
             [lein-midje "3.1.3"]
             [lein-set-version "0.3.0"]])
