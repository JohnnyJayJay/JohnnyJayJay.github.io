(ns cryogen.now
  "Functions for generating 'now' page parameters"
  (:require [hato.client :as http]))

(defn- fetch-books! [instance user]
  (->> (http/get (str "https://" instance "/user/" user "/books/to-read?page=1") {:as :json :accept :json})
       :body
       :orderedItems
       (take 3)))

(def fetch-bookwyrm-books! (memoize fetch-books!))
