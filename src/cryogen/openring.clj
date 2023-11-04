(ns cryogen.openring
  (:require [cryogen.shell-util :refer [sh]]))

(defn generate-webring! [{:keys [source-file template-file output-file]}]
  (spit
   output-file
   (sh "go" "run" "-C" "openring" "openring.go" "-S" source-file :in (slurp template-file))))
