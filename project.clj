(defproject com.narkisr/gelfino-client "0.8.0"
  :description "A Gelf logging client including a timbre adapter"
  :dependencies [
     [org.clojure/clojure "1.6.0"]
     [com.taoensso/timbre "4.0.1"]
     [cheshire "5.0.2"]]

  :profiles {
    :dev {
      :dependencies [
        [midje "1.6.3"]
        [clj-http "1.1.2"]
        [clojurewerkz/elastisch "2.1.0"]
      ]        
    }            
  }
  
  :plugins  [[lein-ancient "0.6.7" :exclusions [org.clojure/clojure]] 
             [lein-tag "0.1.0"] [codox "0.8.10"] [lein-midje "3.1.3"]]
)
