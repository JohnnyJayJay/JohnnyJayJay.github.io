{:title "Automatic Updates from OAuth2 Apps on a Static Site",
 :tags ["web" "meta" "clojure"],
 :layout :post,
 :draft? true,
 :date "2024-03-29",
 :description "How I built this blog's 'now' page",
 :comments nil}

- idea (rknight, nownownow), aggregating current status info - orthogonal to social media
- API discovery
  - bookwyrm: didn't look for docs, just threw http requests against the regular URLs (isn't that how it's supposed to work?)
  - spotify: excellent API docs
  - Codeberg/GitHub: not there yet
- bookwyrm: no auth
- spotify: top data read only with oauth2 token with specific scope
- bb script to grab oauth2 token quickly
- spotify access tokens limited to 1h of validity
- concoction to refresh access token
- github personal access token to only access secrets of that one repo
- realisations:
  1. Do it all in Actions workflow: passing outputs from step to step is a complete clusterfuck. pain! (no masking, hard to parse/use in later conditions, ...)
     https://github.com/hmanzur/actions-set-secret
  2. Do it all in clojure: secrets you set while executing a job can't be used later in the job, so refreshing the access token would have no effect!
     => when an access token is expired, the job will fail even though a new access token is set. because it still sees the old one. idea: restart job after refresh? turns out to not be so trivial
     pain: having to use libsodium from clojure - nixos and java native library path = 😬
     life hack: `fd libsodium.so /nix/store/` -> copy path -> `sudo mkdir /usr/lib && sudo ln -s <path> /usr/lib/libsodium.so`
  3. Mix: don't use access token secret, instead output from clojure and set step output (with set-mask)
     https://www.aaron-powell.com/posts/2022-07-14-working-with-add-mask-and-github-actions/
     current solution never reuses access token even if it's still valid... maybe problematic
