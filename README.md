# Intro 
A client library for the [Gelf](https://github.com/Graylog2/graylog2-docs/wiki/GELF) protocol with a fully compliant and correct implementation (chunking works). 

It has an appender implemented for [timbre](https://github.com/ptaoussanis/timbre), nothing prevents it from being used in any other logging framework. 

# Usage

```clojure
  [com.narkisr/gelfino-client "0.4.2"]
```

Raw client use:

```clojure
(connect)

(send-> "0.0.0.0" {:short_message "i am a unicorn" :message "i am a unicorn" :level 4})

; A chunked message
(send-> "localhost" 
  {:short_message "i am a unicorn" :message (apply str (take 400000 (repeat "I am a unicorn")))})
```

Using timbre appender:

```clojure
(use '[gelfino.timbre :only (gelf-appender)])
(use '[taoensso.timbre :only (set-config! set-level!)])

(set-config! [:appenders :gelf] gelf-appender)
(set-config! [:shared-appender-config :gelf] {:host "graylog2/kibana"})
```

See [api](http://narkisr.github.com/gelfino-client/index.html) docs.

# Transaction id (tid)
 
Gelfino client has the ability to set a logicl transction accross spanning components and threads:

```clojure
  (set-tid id 
    ; all logs with current thread will have :_tid id value    
   )
```

All that is required in order to see the linear log flow of such a transaction is add _tid field to your search query.

# Copyright and license

Copyright [2013] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
