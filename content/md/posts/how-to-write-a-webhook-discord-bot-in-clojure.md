{:title "How To Write a Webhook Discord Bot In Clojure"
 :description "Exploring the new(ish) ways to interact with the Discord API"
 :date "2022-02-03"
 :layout :post
 :tags ["clojure" "web" "discord"]
 :toc true}
 
I write a lot of apps for the messenger [Discord](https://discord.com), specifically bots. It's a big part of how I learned programming to begin with and I still enjoy it to this day. Last year, I wrote quite a few small and simple bots: [instant-poll](https://github.com/JohnnyJayJay/instant-poll) (creates polls in your Discord server), [xkcdiscord](https://github.com/JohnnyJayJay/xkcdiscord) (displays [xkcd](https://xkcd.com) comics) and [beepl](https://github.com/JohnnyJayJay/beepl) (translates messages via [deepl](https://deepl.com)).\
They all have something in common: they're written using a new(-ish) way to interact with Discord. It's a way that is very efficient, clean and simple compared to the traditional way of using the Discord API, which is why I like it so much. Even after over a year, this method is still not on every Discord developer's rader. Subsequently, I frequently get the question "How do these bots work?" - answering this question will be my mission for this post.

## The Traditional Way

For years, the Discord Bot API consisted of two parts: the *gateway* and the *REST* API. You can probably picture what the REST API is if you've ever worked with one for a different service: for almost every action you can do in Discord, there is a URL you can make an HTTP request to to execute it. The gateway API is a bit more complicated. It is a WebSocket connection to Discord that is mainly used to receive events, such as "A Message was sent" or "A user has changed their nickname". Since the end of 2020, you also receive "interaction"-events over the gateway, which offer new, bot-exclusive interfaces for user interaction. For example, you can now use the native *slash commands* instead of parsing every message to check if it's directed at your bot.

Interactions make a novel connection between the gateway and the REST API. When users run a slash command or click on a message button, they expect something to happen. As such, interaction events are the only events that expect a "response" by nature. Unfortunately, there is no notion of request - response in the WebSocket protocol; a WebSocket is just a bidirectional, continuous stream of data. Thus, Discord added [a new endpoint](https://discord.com/developers/docs/interactions/receiving-and-responding#create-interaction-response) that **must** be called within 3 seconds of an interaction event, otherwise the user will see an "Application did not respond" message.

So, in essence, to make a Discord bot the traditional way, you need several things: an HTTP client, a WebSocket client and of course, ideally, a library that provides abstractions on top of these specifically for interfacing with Discord. You listen to incoming events and "respond" to them via separate HTTP requests.

## The New Way

As hinted at earlier, the new *interactions* implement a request-response scheme - only this time the requests come from a user - by extension, Discord - and the responses come from your bot, not the other way around. Sure, this scheme had been superimposed by bot developers on, for example, certain message events before, but this time it is actually *enforced* by Discord and a real property of the API. In other words: in constrast to interactions, Discord doesn't *expect* you to "respond" to other events (it is not even well-defined what that would mean).\
WebSocket + HTTP in combination do not provide a natural request-response mechanism. You know what does though? HTTP in isolation. 

With the introduction of interactions, Discord also added a way to write your bot as a web server: Discord makes a HTTP request *to you* for each incoming interaction, and you answer with an interaction response. This makes it very nice to develop simple, focused, self-contained apps for Discord. All you need to know is how to handle HTTP requests.

Let's go briefly over how this works in practice.
1. You have a web server that takes requests at some URL (you need an SSL certificate for the domain, Discord only allows HTTPS).
2. You set that URL on your bot's [settings page](https://discord.com/developers/applications).
3. You validate incoming requests for authenticity (to make sure the requests come from Discord). To do that, you need to verify a ED-25519 signature header in the request.

These steps are also described in the [official documentation](https://discord.com/developers/docs/interactions/receiving-and-responding#interactions-and-bot-users). The hardest part of this process is verifying signatures correctly. Fortunately, there are libraries for this that deal with the details for you, one of which I have written myself. 

In the following sections, I will show you how to create, program and run a webhook-based bot in Clojure from start to finish.

## Prerequisites
### Creating a Discord app
If you haven't done that yet, you'll need to head over to your [applications page](https://discord.com/developers/applications) and create an application by clicking on "New Application". Then, in the app configuration, navigate to the "Bot" tab and create a bot user for the app.

In order to interact with it, you must also add your newly created bot to a **server**. In your applications page, go to `OAuth2` -> `URL Generator` and check the boxes like so:

![](/img/webhookbot/url_generator.png)

Then, navigate to the generated link in your browser and select a server you want it to join.

### Setting up a local testing environment
With regular Discord bots, you connect to *Discord*. Conversely, with a webhook-based bot, Discord connects to *you*. Since your app is just a web server, if you want to test it locally, you'll have to open your network to the outside world somehow. This can be a real piece of work - you really don't want to open any ports for this and you especially don't want to set up a DNS record *and* an SSL certificate for your local machine.

The easiest solution to test your webhook-bot locally is [ngrok](https://ngrok.com). This tool allows you to effortlessly create a URL that routes HTTP traffic to some port on your local machine. After installing ngrok, I recommend signing up for an account on their website and adding your account's auth token via `ngrok authtoken` (otherwise your URLs will expire after 2 hours). Then, run `ngrok http 8090 --region eu` in a terminal (replace `eu` with the most suitable, supported region identifier). You will then see a screen displaying information about the connection, including the ngrok URL which will look something like `https://7344-2a02-8071-22a1-8000-af6d-f76b-ab83-ae38.eu.ngrok.io`. Make sure to grab the one with `https`, not `http` and leave ngrok running.

You can now go to your application's page and paste the URL in the "Interactions Endpoint URL" field.

![](/img/webhookbot/endpoint_url.png)

However, when trying to save your settings, you'll still get an error. If you look in the ngrok console, you'll see that there have been two requests to the URL, both of which failed with a `502 Bad Gateway` response. This is of course because the bot is not running yet. We'll return to this later and save the settings when it is actually up and running.


## Writing your first webhook Discord bot

Because I found myself repeating the same boilerplate code for every new bot I wrote, I decided to write a [template](https://github.com/JohnnyJayJay/discord-http-bot) that lets you generate a ready-to-go Discord app. To follow along, you'll need to install [Leiningen](https://leiningen.org) and [Clojure](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

First, open a terminal and type `lein new com.github.johnnyjayjay/discord-http-bot first-webhook-bot`. This should generate a new Clojure project in the `first-webhook-bot/` directory. It contains a number of files and utilities:
- `src/` contains the source code. At the time of creation, there are 2 namespaces: `handler` and `command`. `handler` contains the boilerplate code needed to set up a compliant web server. `command` contains your command definitions.
- `config/config.edn` is where the bot configuration goes. Most importantly, your `:public-key`.
- `Dockerfile` and `docker-compose.yml` contain configurations to run the app via Docker (more on that later).

Of course, these are all just defaults and you can adjust them as you like. The point is to provide something that can be run (almost) out of the box.

### Running the bot from the REPL

You'll want to work on Discord bots the way you should work on most Clojure projects: using live development in a REPL, i.e. having a running instance that you modify and extend continuously. For that, you'll need to know just 2 things:

1. How to start a REPL and hook it up to your project 
2. How to send source code (or pieces of source code) to the REPL

How you do these things depends on the editor you're using. If you *don't* know how to do this yet, I urge you to go through your [environment setup](https://gist.github.com/yogthos/be323be0361c589570a6da4ccc85f58f#environment-setup). In this tutorial, I'm using Emacs + Cider.

Here are the steps to get it up and running: 
1. Copy your application's **public key** (you can find it right above the "interactions endpoint URL" from earlier)
2. Enter that key into `config/config.edn`: `{:public-key "pub-key here"}`
3. Start a REPL in your project (Emacs: `cider-jack-in`)
4. Load the `handler` namespace/send it to the REPL (Emacs: `cider-load-buffer` when in the file)
5. Run `(def stop (-main))` in the REPL. 

That last part actually starts the server. It also defines a function `stop` that you can call to stop the server again (i.e. `(stop)`).

*Now* you should be able to save the interactions endpoint URL that gave you errors earlier (the ngrok URL). If everything worked correctly, you should see two new requests in the ngrok console: one with a success (200) response and one with an unauthorized (401) response. These requests come from Discord - they make sure that you handle their requests (in particular, verify their signature) correctly before saving the URL.

### Registering a slash command
Your bot is now up and running, but it has no functionality yet. Let's change that by registering a slash command.

In `command.clj`, you'll find the definition of `greet-command`. Right below it, there are some commented forms that you can use as guidance to (re-)register commands. We will use [discljord](https://github.com/discljord/discljord) for this. First, load the entire namespace into the REPL (like you did before with `handler`). Then, run the forms inside `comment` in order. Of course, you first have to replace the placeholders for your bot token (which you can find on your application page under the "Bot" tab) and the guild id (right click the server you added your bot to -> Copy ID. If this option doesn't appear for you, make sure you've enabled "Developer Mode" in your Discord settings). Once you've run those forms, you should see a response from Discord that shows you your newly created command. 

**This is just a snipped to help you get started**. You **should not** keep this in your file if you want to make the code public because you'll leak your token.

Later, when you want to make your bot public, you'll want to add your commands as global commands, not just for a specific server. You can do this the exact same way, except that you use `bulk-overwrite-global-application-commands!` and remove the `"guild-id"` argument. Note that it takes some time for global commands to update (up to one hour).
For now, try to run `/greet` in your server to verify everything worked.

### Defining command handlers

In `command.clj`, you can see the definition of an example command handler for the `greet` command:

```clj
(defhandler greet-handler
  ["greet"]
  _interaction
  [user]
  (-> {:content (str ":wave: Hey there" (when user (str ", <@" user ">")) \!)}
      rsp/channel-message))
```

As you can see, you can define command handlers using `defhandler`. This is a macro that comes from my Discord interactions library [slash](https://github.com/JohnnyJayJay/slash), which facilitates slash command and general interactions handling.
- `greet-handler` is the public var under which the handler will be accessible.
- `["greet"]` is the "path" to the command. In this case it just means it matches `/greet ...` commands (where "..." are the command options).
`["greet" "foo"]` would match `/greet foo ...`. Limited pattern matching is also possible: `["greet" more]` matches `/greet foo ...`, `/greet bar ...` etc.
- `_interaction` is a symbol that will be bound to the entire interaction object, which contains information like the user who ran the command, the server it was run in as well as interaction id and token needed for further requests. It is not needed in this case and therefore marked as `_` unused. A different use of this parameter might look like this: `{:keys [id token member]}` (map destructuring).
- `[user]` is the vector of command options. In this case, `user` will be bound to the value provided for the option `user` of the greet command. If this parameter is not a vector but something else, it will be treated as a binding for a map from option keys -> option values.
- Everything following is the body of the handler, which may contain code that uses the bindings above. In this case it creates a response object (message) saying "👋 Hey there!" if the person who ran it did not set the `user` option or "👋 Hey there, @User!" if they *did* specify something for the `user` option
 
A handler should return an interaction response (a Clojure map whose structure is that described in [the official documentation](https://discord.dev/interactions/receiving-and-responding#interaction-response-object-interaction-response-structure)). slash helps you by providing functions that construct compliant response objects. `(rsp/channel-message {:content "xyz"})` turns into `{:type 4 :data {:content "xyz"}}`, for example. Of course, you're not obligated to use these functions, but they make your code easier to understand and remove magic values.\
One common addition to a regular channel message as a slash command response is to make it ephemeral, which can be done by adding a `rsp/ephemeral` call at the end:
```clj
(-> {:content (str ":wave: Hey there" (when user (str ", <@" user ">")) \!)}
    rsp/channel-message
    rsp/ephemeral)
```
This will make it so the message is only shown to the user who executed the command. Of course, for this particular command, this doesn't make much sense, but you can try it nonetheless. Just reload the handler form (in Emacs: `cider-eval-defun`) and when you run `/greet` now, it should appear in an ephemeral message.

### Implementing your own commands and features

And now for the fun part: the rest is up to you. To add/change the available slash commands, just (re)define the commands like `greet-command`. To learn the structure of slash commands, you should read [the official documentation](https://discord.com/developers/docs/interactions/application-commands). You can then add and change the handlers in your editor without having to restart the server. Just reload the relevant functions in the REPL. Finally, some library recommendations for stuff you might end up needing:

- [mount](https://github.com/tolitius/mount) is a great, simple way to manage state in your app
- [datalevin](https://github.com/juji-io/datalevin) is a good database
- [discljord](https://github.com/discljord/discljord) wraps the Discord API, giving you access to more advanced functionality such as followup messages or editing/deleting your interaction responses proactively. It also gives you helper functions for checking permissions. It is already included as a `:dev` dependency in `project.clj`, to use it in production, simply move the dependency to the top level `:dependencies` key.
- [http-kit](https://github.com/http-kit/http-kit) is already included in the project's dependencies to act as the underlying web server. But it also includes functionality to *make HTTP requests*.

## Deploying your app

At the end of the day, you'll want your app running 24/7. To do that, you need a VPS or something similar. The following shows how I deploy my apps on a Ubuntu VPS.

### Setting up the project on your server
The first step is to get the project files over to your server. The easiest way to do this is to make your project a git repository and push it to [GitHub](https://github.com) or similar. If you registered your slash command like shown above, make sure your bot token isn't in the file anymore! Then you can simply clone the project on your VPS. Edit `docker-compose.yml` to use some free port on your system:

```yaml
services:
  server:
    ...
    ports:
      - "7778:8090"
```

Here, I chose port `7778`.

You can then run `docker-compose up -d` to run the app.

### Configuring a reverse proxy with nginx
The way I usually deploy these apps is using [nginx](https://nginx.org/en/) as a reverse proxy. This makes it easy to run multiple webhook bots on the same server and to configure HTTPS. Assuming you own a domain `foo.bar` that points to your server's IP via an A record, you can create an nginx config in `/etc/nginx` like so:

```nginx
server {
    server_name foo.bar;
    
    location /my-bot/interactions {
        proxy_pass http://127.0.0.1:7778;
        proxy_http_version 1.1;

        proxy_redirect off;
        proxy_buffering off;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Request-Id $request_id;
        proxy_set_header Upgrade $http_upgrade;
    }
}
```

To obtain an SSL certificate for `foo.bar`, I recommend installing [certbot](https://certbot.eff.org/). On their website, you can enter your software (in this case nginx) and OS (in this case Ubuntu) and it will tell you what you have to run to get a certificate. Conveniently, it will also adjust and complete your config for you.

After running `nginx -s reload`, you should now be able to reach `https://foo.bar/my-bot/interactions` via your browser, where it should tell you that only POST requests are allowed. If that is the case, there is only one last thing to do: to set that URL as the interactions endpoint URL for your bot in the developer portal (like you did before with the ngrok URL). You now have a running bot.

To apply updates from your git repo, use `docker-compose down && git pull && docker-compose up --build -d`.

## Conclusion
Although Webhook-based bots have more limitations than traditional bots, they can make your code cleaner and more concise, especially if the bot you write is simple and works on a request-response basis.
Of course, gateway-based bots will keep their position as a general purpose tool for Discord apps. But I hope I could teach you something new today and maybe even give you some motivation to play around with this technology.
