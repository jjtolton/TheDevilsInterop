(ns metal-interop.util
  (:require [clojure.java.io :as io]
            [camel-snake-kebab.core :as csk])
  (:import java.io.ByteArrayOutputStream
           java.util.Base64))

(defmacro with-err-str
  "Evaluates exprs in a context in which *err* is bound to a fresh
  StringWriter.  Returns the string created by any nested printing
  calls."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))

(defn json->clj [x]
  (clojure.data.json/read-str x :key-fn csk/->kebab-case-keyword))

(defn clj->json [x]
  (clojure.data.json/write-str x :key-fn csk/->camelCaseString))

(defn bytes->string [bytes]
  (String. bytes java.nio.charset.StandardCharsets/UTF_8))

(defn pprint-clj->json [data]
  (clojure.data.json/pprint-json data :key-fn csk/->camelCaseString))

