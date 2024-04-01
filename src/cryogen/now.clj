(ns cryogen.now
  "Functions for generating 'now' page parameters"
  (:require [hato.client :as http]
            [caesium.crypto.box :as crypto])
  (:import (java.util Base64)))

;; TODO: include author name (requires additional fetch of authors[0] for each book)
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

(defn- printerr [& msg]
  (binding [*out* *err*]
    (apply println msg)))

(defn refresh-spotify-token! [{:keys [github-token repo refresh-token client-id client-secret refresh-secret]}]
  (printerr "Refreshing spotify token")
  ;; Refresh spotify access token & get new refresh token
  (let [{access-token :access_token refresh-token :refresh_token}
        (-> (str "https://accounts.spotify.com/api/token")
            (http/post {:as :json
                        :content-type :x-www-form-urlencoded
                        :basic-auth {:user client-id :pass client-secret}
                        :form-params {:grant_type "refresh_token"
                                      :refresh_token refresh-token}})
            :body)]
    ;; if a new refresh token was generated, write it back as a github secret
    (when refresh-token
      (printerr "New refresh token received; fetching GitHub public key")
      (let [gh-base-url (str "https://api.github.com/repos/" repo "/actions/secrets/")
            ;; Get public key for secret encryption
            {:keys [key_id key]} (-> (str gh-base-url "public-key")
                                     (http/get {:as :json :oauth-token github-token})
                                     :body)
            key (.decode (Base64/getDecoder) ^String key)]
        (printerr "Updating github secret")
        (http/put
         (str gh-base-url refresh-secret)
         {:content-type :json
          :form-params
          {:encrypted_value (-> refresh-token
                                (.getBytes)
                                (crypto/box-seal key)
                                (->> (.encodeToString (Base64/getEncoder))))
           :key_id key_id}
          :oauth-token github-token})))
    (println access-token)))
