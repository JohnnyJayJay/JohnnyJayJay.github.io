{:title "Automatic Updates from OAuth2 Apps on a Static Site",
 :tags ["web" "meta" "clojure"],
 :layout :post,
 :date "2024-04-01",
 :description "How I built this blog's 'now' page",
 :comments nil}

Recently, I [stumbled upon](https://rknight.me/blog/the-web-is-fantastic/) the idea of ['now' pages](https://nownownow.com/about) on personal websites. I was looking for things to add to mine because I was (and still am!) pretty motivated to work on something related to this site. The idea of a now page is to tell visitors about what you're currently doing – what you're working on, what music or literature you're enjoying or what's otherwise happening in your life.

I like this idea and consequently started working on [my own now page](/pages/now). One of the difficulties with such a page is maintenance: will you have the stamina and discipline to update it regularly? Especially if you want to tell people about stuff that changes rather frequently – like what you're watching, reading, listening to – this would probably be too tiring to keep up for a long time, for me at least.

So, for my page I wanted to use different APIs to aggregate the data I wanted to display automatically but still keep this a static site.

## API Discovery

The three initial things I was looking to include on my now page were the books I'm reading (via [Bookwyrm](https://joinbookwyrm.com)), the music I'm listening to (via [Spotify](https://spotify.com)) and the (code) projects I'm working on (via [Codeberg](https://codeberg.org) and perhaps [GitHub](https://github.com)). At the time of writing, I have not implemented the latter yet. 

I hadn't really worked with any of these APIs before, so the first step was to figure out how they worked. I didn't immediately find any documentation for Bookwyrm's API, but that turned out to be irrelevant anyway. Turns out, the API URLs are the exact same as in the (public!) Web UI, and you can just set the `Accept` header to get JSON from them instead of HTML:

```bash
curl --header 'Accept: application/json' https://bookwyrm.social/user/johnny/books/reading
```

This gave me a pretty self-explanatory [ActivityPub](https://docs.joinbookwyrm.com/activitypub.html) object containing further URLs that let me paginate the list of books in the "reading" list of my account. For me, I only needed access to a limited number of books (and it's not like I would ever read 20 books at the same time anyway), so fetching only the first page was sufficient for me: `https://bookwyrm.social/user/johnny/books/reading?page=1`.

So that went pretty smooth, how about Spotify?

As you may imagine, Spotify's API is not public (which makes perfect sense for the kind of data I wanted to fetch). Their API is also completely separate from their Web UI. Luckily though, their [documentation](https://developer.spotify.com/documentation/web-api) is excellent – it has introductions, tutorials, guides and extensive (I'm assuming OpenAPI-based) docs for all their endpoints. The one of interest to me was the [Get User's Top Items](https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks) endpoint, which would return my current top artists and tracks.

This endpoint is locked behind authentication with a token that has the `user-top-read` OAuth2 scope. It meant a bit of annoyance: I would have to create an OAuth2 app in Spotify's developer portal and go through the OAuth2 `authorization_code` flow manually once, to get an access token for my account.[^1]

[^1]: I ended up writing a small [babashka script](https://gist.github.com/JohnnyJayJay/6c21379afb2f2de05303b2c39bac3dd6) to make this a bit more convenient – it starts a web server that only performs the authentication logic and echoes the token response from the OAuth2 provider.

Unfortunately, Spotify OAuth2 access tokens are only ever valid for 1 hour at a time and you have to [refresh](https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens) them using a separate refresh token once they expire. This is a reasonable security measure under normal circumstances but pointless in my case, where I really just wanted a low-permission token for myself that would be stored securely in a GitHub Actions secret.

## Automating OAuth2 authentication in CI

I already had a GitHub actions workflow for this page that ran once a day, periodically. It essentially only consisted of three steps: generate the [openring](https://git.sr.ht/~sircmpwn/openring/) selection of recommended blog posts, generate the site using cryogen and deploy it to GitHub pages. The idea was to extend this procedure by storing the refresh token I had previously obtained as well as my Spotify OAuth2 app's client ID and secret in GitHub Actions secrets and have the workflow use them. 

After some trial and error, going back and forth between implementing everything directly through steps in GitHub Actions and writing my own helper functions in Clojure, I settled with the following:

- The Actions workflow calls a Clojure function I wrote as a step
- The Clojure function receives the refresh token and client credentials for spotify as well as a GitHub API token
- The function refreshes the spotify token
  - it prints the new access token to stdout 
  - if there is a new refresh token, it updates the corresponding GitHub Actions secret
- The step [masks](https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#masking-a-value-in-a-log) the outputted access token and sets it as a [step output](https://docs.github.com/en/actions/using-jobs/defining-outputs-for-jobs) for use by the site generation step 

This is what that step ended up looking like:

```yaml
name: Refresh spotify token
id: spotify
run: |
  TOKEN=$(clojure -X:refresh-spotify-github-secrets :github-token '"${{ secrets.BLOG_SECRET_TOKEN }}"' \
    :repo '"${{ github.repository }}"' :refresh-token '"${{ secrets.SPOTIFY_REFRESH_TOKEN }}"' \
    :client-id '"${{ secrets.SPOTIFY_CLIENT_ID }}"' :client-secret '"${{ secrets.SPOTIFY_CLIENT_SECRET }}"' \
    :refresh-secret '"SPOTIFY_REFRESH_TOKEN"')
  echo "::add-mask::$TOKEN"
  echo "access-token=$TOKEN" >> "$GITHUB_OUTPUT"
```

It calls the `refresh-spotify-github-secrets` alias for my Clojure function, passing all the necessary data from existing secrets. The execution of this alias only outputs the new access token, which gets masked in the second command to censor all future logs containing it. It is then set as a step output `access-token`, which is accessed by the blog building step like so:

```yaml
name: Build blog
...
env:
  SPOTIFY_ACCESS_TOKEN: ${{ steps.spotify.outputs.access-token }}
```

This is what the function I wrote for this looks like:

```clojure
(ns cryogen.now
  (:require [hato.client :as http]
            [caesium.crypto.box :as crypto])
  (:import (java.util Base64)))
  
;; ...

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
```

A lot of the ceremony in this code comes from the fact that GitHub requires you to manually encrypt secret values using [libsodium](https://doc.libsodium.org/) before sending them to the API. In total, it's just 1-3 requests:

- one POST to the Spotify token endpoint
- if Spotify returns a new refresh token, a GET to get the GitHub public key for encryption, then a PUT to set the new secret value.

Side note: I really, really like the [hato](https://github.com/gnarroway/hato) library for HTTP requests. It's just a wrapper around Java's own new(ish) HTTP client, so it doesn't introduce any new dependencies, plus it has direct support for all the common things you'll need from a requests library (response body coercion, Basic auth and OAuth2, form encoding, ...).

### libsodium bindings on NixOS

[caesium](https://github.com/lvh/caesium) is a Clojure bindings library for libsodium. Being on NixOS makes it a bit of a pain to use JVM bindings to native libraries, because the JVM's default search path for native libraries (`/lib`, `/usr/lib`, ...) is not compatible with NixOS. I was too lazy to fix this properly (e.g. by writing a Nix flake or adjusting the JVM library path) and instead opted for the following hack:

```bash
# Find libsodium installation and copy its path
fd libsodium.so /nix/store/
sudo mkdir /usr/lib && sudo ln -s /nix/store/<path> /usr/lib/libsodium.so
```

Yeah, you may scold me for that one.
