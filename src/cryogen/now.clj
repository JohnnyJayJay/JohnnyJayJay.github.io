(ns cryogen.now
  "Functions for generating 'now' page parameters"
  (:require [hato.client :as http]))

(defn- fetch-books! [instance user]
  (->> (http/get (str "https://" instance "/user/" user "/books/reading?page=1") {:as :json :accept :json})
       :body
       :orderedItems))

(def fetch-bookwyrm-books! (memoize fetch-books!))

(defn- fetch-spotify! [access-token]
  (zipmap
   [:tracks :artists]
   (map
    #(-> (str "https://api.spotify.com/v1/me/top/" % "?limit=3&time_range=short_term")
         (http/get {:as :json :oauth-token access-token})
         :body
         :items)
    ["tracks" "artists"])))

(def fetch-spotify-top! (memoize fetch-spotify!))
