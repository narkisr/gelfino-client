(defproject com.narkisr/gelfino-client "0.7.0"
  :description "A Gelf logging client including a timbre adapter"
  :dependencies [
        [org.clojure/clojure "1.5.1"]
        [com.taoensso/timbre "2.6.3"]
        [cheshire "5.0.2"]]

  :profiles {
    :dev {
      :dependencies [
        [midje "1.5.1"]
        [clj-http "1.0.0"]
      ]        
    }            
  }
  
  :plugins  [[lein-tag "0.1.0"] [codox "0.8.10"] [lein-midje "3.0.0"]]
)
