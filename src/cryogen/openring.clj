(ns cryogen.openring
  (:require [clojure.java.shell :as sh]))

(defn generate-webring! [{:keys [source-file template-file output-file]}]
  (->> (sh/sh "go" "run" "-C" "openring" "openring.go" "-S" source-file :in (slurp template-file))
       :out
       (spit output-file)))
