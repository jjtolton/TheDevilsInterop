(ns metal-interop.build
  (:gen-class)
  (:require [metal-interop.core :as metal :reload true]
            [metal-interop.ffi :as ffi :reload true]
            [tech.v3.datatype.ffi :as dt-ffi]
            [tech.v3.datatype.ffi.graalvm :as graalvm]
            tech.v3.datatype.ffi.graalvm-runtime))

(defn build! []
  (do (require '[tech.v3.datatype.ffi.graalvm-runtime]
               '[tech.v3.datatype.ffi.graalvm :as graalvm])
      (with-bindings {#'*compile-path* "classes"}
        (graalvm/expose-clojure-functions
         ;;name conflict - initialize is too general
         {#'metal/return-wrapper  {:rettype  :pointer
                                   :argtypes [['input :pointer]]}
          #'ffi/register-methods! {:rettype  :pointer
                                   :argtypes []}}
         'metal/libmetal nil)
        (graalvm/expose-clojure-functions
         {#'ffi/initialize-json {:rettype  :pointer
                                 :argtypes [['input :pointer]]}
          #'ffi/ping-test!      {:rettype  :pointer
                                 :argtypes [['input :pointer]]}}
         'metal/libinit nil))))

(defn -main []
  (build!))

