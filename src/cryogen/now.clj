(ns cryogen.now
  "Functions for generating 'now' page parameters"
  (:require [hato.client :as http]
            [caesium.crypto.box :as crypto])
  (:import (java.util Base64)))

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

(defn update-gh-secret! [github-token url ^String plaintext key key-id]
  (http/put
   url
   {:content-type :json
    :form-params
    {:encrypted_value (-> plaintext
                          (.getBytes)
                          (crypto/box-seal key)
                          (->> (.encodeToString (Base64/getEncoder))))
     :key_id key-id}
    :oauth-token github-token}))

(defn refresh-spotify-token! [{:keys [github-token repo refresh-token client-id client-secret refresh-secret access-secret]}]
  (let [gh-base-url (str "https://api.github.com/repos/" repo "/actions/secrets/")
        ;; Get public key for secret encryption
        _ (println "Fetching GitHub public key")
        {:keys [key_id key]} (-> (str gh-base-url "public-key")
                                 (http/get {:as :json :oauth-token github-token})
                                 :body)
        key (.decode (Base64/getDecoder) ^String key)
        ;; Refresh spotify access token & get new refresh token
        _ (println "Refreshing spotify token")
        {access-token :access_token refresh-token :refresh_token}
        (-> (str "https://accounts.spotify.com/api/token")
            (http/post {:as :json
                        :content-type :x-www-form-urlencoded
                        :basic-auth {:user client-id :pass client-secret}
                        :form-params {:grant_type "refresh_token"
                                      :refresh_token refresh-token}})
            :body)]
    ;; write spotify tokens back to github secrets
    (println "Updating github secrets")
    (when refresh-token
      (update-gh-secret! github-token (str gh-base-url refresh-secret) refresh-token key key_id))
    (update-gh-secret! github-token (str gh-base-url access-secret) access-token key key_id)))
