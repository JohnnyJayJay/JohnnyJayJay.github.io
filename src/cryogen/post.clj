(ns cryogen.post
  (:require [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [cryogen.shell-util :refer [sh]]
            [hato.client :as http])
  (:import (java.io File PushbackReader)))

(defn post-status! [oauth-token instance lang visibility content]
  (http/post
   (str "https://" instance "/api/v1/statuses")
   {:oauth-token oauth-token
    :as :json
    :form-params {:status content
                  :visibility visibility
                  :language lang}}))

(defn update-front-matter! [post instance username id]
  (let [post-file (io/file post)
        [front-matter lines]
        (with-open [buf-reader (io/reader post-file)
                    pb-reader (PushbackReader. buf-reader)]
          [(edn/read pb-reader) (doall (drop 1 (line-seq buf-reader)))])]
    (with-open [buf-writer (io/writer post-file)]
      (pprint (assoc front-matter :comments {:instance instance :username username :id id}) buf-writer)
      (doseq [line lines]
        (.write buf-writer line)
        (.newLine buf-writer))
      (.flush buf-writer))))

(defn fedi-post! [{:keys [instance lang visibility post oauth-token-file]}]
  (when-not (.exists (io/file post))
    (println "Cannot find post file " (prn-str post))
    (System/exit 1))

  (let [oauth-token (sh "gpg" "--decrypt" oauth-token-file)
        masto-post-file (File/createTempFile "masto-post-" ".txt")]
    (.. (ProcessBuilder. [(or (System/getenv "EDITOR") "nano") (.getAbsolutePath masto-post-file)])
        inheritIO
        start
        waitFor)
    (let [masto-content (slurp masto-post-file)]
      (io/delete-file masto-post-file true)
      (println "Mastodon post to create:")
      (println "--------")
      (println masto-content)
      (println "--------")
      (println "Press any key to confirm.")
      (read-line)
      (let [{{:keys [id] {:keys [username]} :account} :body} (post-status! oauth-token instance lang visibility masto-content)]
        (update-front-matter! post instance username id)))))
