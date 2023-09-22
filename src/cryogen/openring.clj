(ns cryogen.openring
  (:require [clojure.java.shell :as sh]))

(defn generate-webring! [{:keys [source-file template-file output-file]}]
  (let [{:keys [exit err out]} (sh/sh "go" "run" "-C" "openring" "openring.go" "-S" source-file :in (slurp template-file))]
    (if (zero? exit)
      (spit output-file out)
      (binding [*out* *err*]
        (println err)
        (System/exit exit)))))
