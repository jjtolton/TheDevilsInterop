(ns metal-interop.playground
  (:require [metal-interop.core :as metal]
            [camel-snake-kebab.core :as csk]
            [metal-interop.ffi :as ffi]
            [tech.v3.datatype.ffi :as dt-ffi]))

(comment
  (ffi/register-methods!)
  (clojure.data.json/read-str
   (dt-ffi/c->string
    (metal/return-wrapper
     (dt-ffi/string->c
      (clojure.data.json/write-str
       {"name" "clojure.core/assoc"
        "data" [{:a 1} 2 3]}))))
   :key-fn
   csk/->kebab-case-keyword))
  

  
