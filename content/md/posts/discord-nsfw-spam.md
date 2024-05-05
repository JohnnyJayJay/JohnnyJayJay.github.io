{:title "What's up with the NSFW spam on Discord?",
 :tags ["discord"],
 :layout :post,
 :date "2024-04-08",
 :description "This just in: horny dudes can't stop falling for phishing",
 :toc true,
 :comments
 {:instance "chaos.social", :author "johnny", :id "112235542897512830"}}

**Disclaimer:** Explicit, pornographic language ahead. However, I've spared you the sight of any NSFW imagery.

If you've been on Discord over the last year or so, it's likely you've seen a message proclaiming something like this (here goes my family-friendly website rating, I suppose):

> Teen Porn and Onlyfan Leaks here 🍑🍒 : \<discord-invite-link\> @everyone

I moderate two servers on Discord that are fairly big (3.5k and 1.3k members respectively). I have seen this kind of spam message, on average, probably once or twice every week for more than a year.

At first, I didn't think much of it. Discord has had an automated spam problem for a long time. I have encountered many different kinds over the 6-or-so years I've been using the platform, from the most primitive (bots sending invite links to promote random servers) to pretty elaborate ones ([bot-*assissted* social engineering to get people to install malware](https://www.howtogeek.com/781369/psa-if-someone-says-try-my-game-on-discord-say-no/)). On that spectrum, I placed the "click here for porn" spam on the very far end of primitiveness – after all, who has spent more than 5 minutes on the world wide web and still falls for this?

After more than a year of more and more random accounts urging me to join their creepy smut servers, I wondered why this was still the most prevalent spam on Discord, and why it hadn't changed one bit. It told me that *something* about this was clearly working for whoever was orchestrating it. So after another recent wave of these messages, I decided to finally look into what and/or who is behind this.

If you don't care about any of this and are just interested in how to prevent this spam for your own server, see [countermeasures](#countermeasures).

## Anatomy of the spam

An instance of this spam goes like this (again, it has not changed one bit over the last year as far as I'm aware):

A user sends a message with the same content in every accessible channel of a Discord server. The message usually contains an invite link to another Discord server and a string of words promising "leaked" pornographic material of (young) women. It also includes "@everyone", which sends a notification to every member of the server (but is disabled by any reasonable administrator of a bigger server). The invite gets embedded automatically in the Discord message, the image of the server shown tends to be a suggestive picture of a woman.

These spam messages are undoubtedly sent by a computer program rather than actual people. The fact that they send the message to every channel they can at more or less the same time is proof enough of that part. But it's not just bots, it's bots using accounts that (used to) belong to actual people.

The accounts disseminating the spam are already a member of the servers at the time of posting spam, i.e. the bots post messages, but do not seem to actively join new servers to do so. This makes (some of) the traditional spam protection on Discord's side ineffective against them: they cannot be detected based on account age or joining behaviour, for example.

**Update (2024-05-05)**: I've seen multiple instances of bots joining (public) servers right before spamming now, so this may not be accurate anymore – perhaps the scammers are trying new ways to increase their reach.

## What happens if you click?

Ok, now for the interesting part. You see the message, decide to be kind of a creep, ignore common sense and click. What happens?

### Hook

First of all, you'll probably be greeted by multiple notifications. You'll see a server like this:

<img src="/img/discord-nsfw/server-1.jpg" width="900" alt="Screenshot of a Discord server named 'NSFW + NITRO + CHATTING 🍑🍒'. On the left, there is the channel list: 'verify', 'help-verify' and 'nsfw'. Two more channel categories are visible: 'OF and leaks' and 'not safe for work <18'. The channel names under those categories are names of OnlyFans creators and celebrities, and porn categories respectively. On the right, there is the member list with two bots at the top and 33 online members with the role 'server access' below. The 'verify' channel is opened. It contains a bot message 'Verification required: to gain access to NSFW + NITRO + CHATTING 🍑🍒 you need to prove you are a human by completing a captcha. Click the button below to get started!' with two buttons 'Verify' and 'Why?' below. The other message in that channel is from an account named 'Lu' with an avatar of a blonde woman. Their two messages say: 'Verify yourself to access the server content' and 'If you need help with verification, checkout #help-verify'">

Here is another example, this server is big enough (roughly 10k members) to have a vanity (custom) invite URL, making it appear more serious:

<img src="/img/discord-nsfw/server-2.jpg" width="900" alt="Screenshot of a Discord server named 'TIKTOKGIRLZ' that looks very similar to the one above. It also has a 'verify' and a 'how-to-verify' channel, and it also has two channel categories like the ones in the other server, one with porn category names, the other with TikTok start names. The open channel is again 'verify' and it shows an identical bot message asking you to complete a captcha. The member list on the side shows 158 online members with the role 'access', most of them advertising the server in their status message.">

The servers have a default channel telling you to "verify" your account in order to gain access to the "locked" channels. All the channels named after celebrities, OnlyFans creators and porn categories (supposedly containing what you've joined for) look like this:

<img src="/img/discord-nsfw/channel-locked.jpg" width="500" alt="Screenshot of a Discord channel named '💖¦goth'. The viewer does 'not have permission to send messages in this channel'. The only message visible is one by 'Lu': 'CONTENT LOCKED 🔒 Verify yourself with #verify to get access to all content!'">

This is all façade. There is nothing to unlock; there simply isn't any other message in that channel. There are more or less 3 *real* ways channels can be "locked" on Discord:

1. The channel is hidden (`VIEW_CHANNEL` is 0) – in that case, you don't see the channel in the list at all.
2. The channel's messages can't be read (`READ_MESSAGES` is 0) – you won't see any messages at all then (and the channel won't be shown in the UI, I believe). 1. implies 2.
3. The channel's message *history* is hidden (`READ_MESSAGE_HISTORY` is 0) – in that case, you can click on the channel but it won't load any messages from before you clicked. You will see new messages as they appear, though (until you leave the channel in the UI again).

The state of the channel in the screenshot above is neither. Everybody has permission to view it and its history, but there's only one message. In fact, there aren't *any* hidden/locked channels on this server, which can be verified using a manual request to the [Get Guild Channels](https://discord.com/developers/docs/resources/guild#get-guild-channels) endpoint.[^1] 

[^1]: This works because weirdly, hidden channels are only truly hidden in the UI but not in the API. So if you have an embarassing private channel name in your public server, be aware that anyone can see it.

At this point you may notice that you're *constantly* getting notifications from the "verification" channel. On the first server I showed, a message pinging @everyone is sent to the "#nsfw-access" channel roughly every 5 minutes and deleted immediately after, thus generating a notification for that channel and reminding you to click on it. The people running this operation really don't want you to forget you joined this server – they want you to go through with the "verification". 

### Line

The "verification" is, as you might imagine, not actually a verification. It is reasonable to be suspicious of any Discord server asking you to verify yourself, since Discord provides built in functionality for this already, both upon account creation and upon joining a server. But what these servers present you with is a three-step phishing attack:

1. There is a bot on the server imitating a legitimate, public Discord bot. In the example below the bot looks like [captcha.bot](https://captcha.bot/), in other servers I have seen bots made to look like "RestoreCord".[^2]

[^2]: The ["real version" of RestoreCord](https://restorecord.com/) seems to play another role in the web of NSFW spam and scam Discord servers – users authorise it to join servers on their behalf, which can then be used to create large servers very quickly. That is probably one of the mechanisms these servers use to circumvent bans and gain thousands of members in a very short time. 

<img src="/img/discord-nsfw/fake-captcha-bot.jpg" width="250" alt="Screenshot of the Discord profile of a bot account named 'Lookup#1848'. It was created on April 5, joined the server on April 6 and uses the icon of 'captcha.bot'.">

2. Interacting with the bot, you obtain a link from a URL shortener that redirects you to a page imitating what the real bot would present you with, but the site is controlled by the attacker. The domain for this site is rotated frequently as these get flagged pretty quickly in things like [Google Safe Browsing](https://safebrowsing.google.com/). Here, it's `lookup.guild-protect.xyz`.

<img src="/img/discord-nsfw/fake-verify-site.jpg" width="800" alt="Screenshot of a website with the URL 'lookup.guild-protect.xyz/verify?data=...'. It shows a copy of the 'captcha.bot' verification website, asking you to 'Login to verify' for the NSFW server.">

The `data` URL parameter here is a Base64-encoded JSON object that apparently tells the site what server to display:

```json
{
  "guildId": "638675319779360798",
  "clientId": "956826156810768404",
  "name": "NSFW + NITRO + CHATTING 🍑🍒",
  "members": "2997",
  "icon": "https://..."
}
```

This seems to be copied from the functionality of the real captcha.bot as well, except that it isn't validated here and you can enter literally anything.

<img src="/img/discord-nsfw/clown-meetup.jpg" width="250" alt="Screenshot of the same website as before, except shows a server named 'Clown Meetup Central' with a clown emoji picture and '999999' members.">

3. When you click to "verify" yourself, you are presented with a fake Discord login page that is a complete copy of the real frontend, except that all API calls have been swapped out to go to the attacker's server instead.

<img src="/img/discord-nsfw/fake-discord-login.jpg" width="800" alt="Screenshot of a replica of the Discord login page on the domain 'lookup.guild-protect.xyz'">

This actually seems to be a more or less complete proxy for the real Discord API – the attacker just logs in on your behalf. Any 2FA or captcha hurdles the attacker encounters logging in are simply forwarded to you as well, so you can solve them yourself. This makes the phishing really smooth, except for two features:

- [2FA with WebAuthn](https://discord.com/blog/how-discord-modernized-mfa-with-webauthn) is phishing-resistant; you won't be able to complete an authentication challenge from a domain that isn't "discord.com". Unfortunately, barely anyone uses WebAuthn.
- If you log in via username and password, you'll have to verify a login from a new location through your email. In my case, the attacker was trying to log in from an IP address in the United States, which (for me) would certainly raise suspicion (although if you've gone this far, you'll probably ignore this red flag as well and just click verify).

Notice that these two points *don't* impact the phishing if you choose the QR login method, because that doesn't require 2FA or the confirmation of a new login location. In this case however, the QR login did not work when I tried it because this part of Discord's API didn't seem to be proxied properly by the malicious website. I *have* seen QR login working on other phishing sites I investigated. 

### Sinker

If you successfully verify yourself, that is, if you successfully allow the attacker to log in to your account by logging in on the fake Discord page, you are redirected to Discord's actual [oauth success URL](https://discord.com/oauth2/authorized). You now also have a "Server Access" role or similar on the server for which you allegedly verified yourself. But to your dismay, you haven't actually gained access to anything, all the channels still look the same as before.

And then... well, nothing else happens. Except that now there is a device logged into your account that doesn't belong to you:

<img src="/img/discord-nsfw/phishing-session.jpg" width="600" alt="Screenshot of the Discord settings showing a logged in device for the account on Windows in Chrome active in the United States less than an hour ago.">

You are basically a sleeper bot now – as long as the attacker isn't logged out of your account, they can pretend to be you on Discord. They can't change your password if you have 2FA enabled, but they can join servers, send messages, all that jazz. I've gone through this process with a throwaway account and initially expected that it would immediately reproduce the same kind of spam that led to its theft. But nothing seems to have happened so far. 

Perhaps phished accounts are not managed fully automatically – there might be humans who look at accounts to determine what they could be used for.[^3] Or perhaps the time to participate in some bot raid hasn't come yet. In any case, *someone* has access to it now, and they're banking on the fact that I didn't notice I got phished; if I had, I could just invalidate their session in my settings!

**Update (2024-04-09):** The account sent a spam message advertising the same scam yesterday evening, i.e. approximately one and a half days after I intentionally compromised it. Unfortunately I do not have access to it myself anymore, so I cannot check whether anything else has been done to it. I'll keep it on my test server to see whether it will be used for other bot spam in the future and update this section if anything else interesting emerges.

**Update (2024-04-10):** Another interesting scam has emerged that isn't directed at Discord users, but at developers! I've decided to write a short [follow-up post](/posts/discord-nsfw-spam-2) about it because it is too much to explain here.

[^3]: A video by YouTuber "No Text To Speech" has a [section](https://www.youtube.com/watch?v=Gc9E_de_jNU&t=692s) showing the behind the scenes of one of these operations, where the scammers get a notification whenever someone falls for it. The story of how access to this scammer-internal server was obtained is pretty interesting too, give it a watch.

## What's the goal?

At first, this looks like a fairly straight-forward case of phishing aimed at a certain (male) audience on Discord. But while looking into this whole thing, I noticed some more weird aspects.

First of all, phishing isn't the only thing these servers are involved in. The bigger, 10k+ member server I showed in the beginning cross-promotes a bunch of other Discord servers that, as far as I can tell, don't all try to steal your account. All of them advertise (stolen) NSFW content. They all have some combination of the following:

- supposed content hidden behind "ad walls" – websites telling you to sign up to something, download and use some app or similar before you can access the actual link
- channels containing actual (stolen) NSFW material[^4]
- promotion of NSFW telegram channels
- promises to get "premium access" (or whatever) if you promote the server elsewhere
- "verification" prompts from legitimate (?) Discord bots

[^4]: Just to make this clear: this is still at best a violation of someone's copyright and at worst, especially if the origin of the material is unknown, distribution of content whose creation or publication involved exploitation or abuse.

The invite links to some of those servers were invalid when I received them, indicating that they had been deleted already (perhaps they were other phishing servers). The unique invite code of another seemed to have been taken over by people who have nothing to do with the NSFW spam/scam business, joining it I was greeted with a text channel of random people chatting and a picture of a smiling woman captioned "I tricked yall! Heyyyyyy", which, admittedly, was pretty funny.

I digress. The point I was getting to is that this spam/scam operation seems to have multiple goals, based on the content of other servers in those networks, and all of them try to extract money in some way or another.

### Insider info

There used to be a section of me speculating about the broader business model of the scam at this point – thoughts on what the big picture might be. However, since writing the first version of this article and its [follow-up](/posts/discord-nsfw-spam-2), I've spoken to a person who, by their own account, has been observing the Discord scamming scene for more than 10 months and was able to tell me a lot more about what's going on behind the scenes. What follows is based on their claims, but it's all plausible to me and I've seen enough evidence to believe the parts I couldn't verify directly.

First, the scammers' primary goal is to get Discord accounts *with payment information in them*. When you pay Discord for [Nitro](https://discord.com/nitro), you can save, for example, your credit card details in your account so it gets renewed automatically. Your personal subscription with Discord is of no interest for the scammers. But they can use the payment settings intended for *that* to buy gift subscriptions for *others*. The business, then, is simple: take over accounts via phishing, make them buy Discord Nitro and sell it on the black market at a lower price than Discord. Accounts without payment information attached to them (like my throwaway) are simply repurposed as spam bots to further promote the scam.

> This may sound stupid, but I can assure you that given the huge quantity of nitros these people are selling, well, they're quite profitable. I've seen one of them sell over 120 yearlies nitro at €12 each in one afternoon, making almost €1,500.

Note: the regular price for Discord Nitro [in the Western world](https://support.discord.com/hc/en-us/articles/4407269525911-Nitro-Localized-Pricing-FAQ) is *100€* per year. 

I also got an invite link to "one of the biggest servers selling stolen nitro", a server called "Wumpus Paradise V3" with 1.5k members. Multiple scammers seem to use it as a marketplace, announcing in their respective channels when they "restock", i.e. when they've stolen enough accounts again to buy Nitro in bulk. Right around the time I joined that server, a scammer going by the name "Baksa" (and who I'm sure is a lovely fellow in person, judging by the fact they put "na/zi" as their pronouns) announced they would soon bulk-buy and sell Nitro monthly subscriptions again. The price for these stolen subscriptions (119 in total) was 2.80€ each in this case (in crypto, of course), which is therefore slightly cheaper than buying it "legitimately" through a VPN in Turkey, where it's currently the cheapest at ₺104.99 (3.03€ at the time of writing).

Lurking in the general chat of that server, I didn't get the impression the people hanging around there were the brightest lights in the harbour. Though I did get to witness some messages that felt rather absurd, like this person being annoyed at the scammer taking so long opening their sales that they even had time to go to church in the meantime:

<figure>
    <img src="/img/discord-nsfw/scam-server-church-msg.jpg" alt="Screenshot of a Discord message in French by a user named 'Lina' telling the scammer they've been waiting (and going to church)">
    <figcaption>"The crypto has been ready for 3h lmao. I've even had time to go to church" – the French style of life</figcaption>
</figure>

Thoughts and prayers.

### Phishing as a Service

Now, one thing about this scam was really interesting to me from the get-go: why does it always look the same? Contrary to what one might think, it's not because the scammers are all working together, but because they're all using the same *phishing service provider*:

> The owner of this server started this type of scam for the first time over a year ago, and has recently created a full phishing service.\
> 95% of scammers (there are over 50 different people behind these servers, and growing) use his phishing service, which does almost everything for them

Yep, that's right. He may not make an effort to appear serious (unlike the guy behind the [stalkerware](https://arstechnica.com/tech-policy/2024/04/billions-of-public-discord-messages-may-be-sold-through-a-scraping-service/) of recent controversy) but there *is* a guy out there who genuinely offers "phishing subscriptions" to low-skill scammers. A telegram channel I've joined serves as a hub for updates on the latest development, new features, new domains and downtimes. Telegram also appears to be the place where subscribers receive notifications whenever someone falls for the scam.

## Countermeasures

If you manage a server and want to block this kind of spam, there is an easy way to do so using Discord's [AutoMod](https://support.discord.com/hc/en-us/articles/4421269296535-AutoMod-FAQ) feature. Just go to Server Settings -> Safety Setup and edit AutoMod. Add a new, custom block rule like this:

<img src="/img/discord-nsfw/block-rule.jpg" width="500" alt="Rule name: 'Block Invite Links'. Use regex patterns for advanced matching: 'discord\.gg/.*', 'discord\.com/invite/.*'. Block message: yes, send alert: yes, time out member: yes, 60 seconds."> 

This will block all invite links unless explicitly allowed (which, in my eyes, is a sensible default), time out members sending them (preventing multi-channel spam) and alerting you about this behaviour in a select channel, where you can then decide whether it's something actionable.

As for how to deal with the accounts disseminating the spam, you should either ban or kick them. I see kicking as a viable option because the phishing victims might get their account back/invalidate the malicious session. Banning is certainly the safe option, though, given that the accounts might be made to rejoin the server or used for different attacks.

If you have another free minute, you can also use it to head over to the server and report the phishing domain it gives you to [Google Safe Browsing](https://safebrowsing.google.com/safebrowsing/report_general/). This service is used by both Chrome and Firefox, and if enough people report the phishing site, users will be warned about it by their browsers. The domain that stole my throwaway account is already gone.

Another thing I *would* suggest [if it weren't so ridiculously difficult](https://www.alphr.com/report-discord-server/) is reporting the phishing server to Discord. Discord should really add a "Report Server" option in its UI.\
The second-best thing you can do is probably reporting the fake "captcha bot" message for phishing. I don't know to which extent that will affect the surrounding server, though.

Finally, some parting thoughts: we need to ask ourselves where we went wrong that so many people are falling for this. People on Discord tend to be young, they often grew up with the world wide web around them. How do they not recognise such blatant scams? And, perhaps more concerningly: why does this kind of creepy and voyeuristic context attract so many people? I'm not a psychologist, but I feel that our (especially young men's) relationship to sexual entertainment has become more and more unhealthy over the last years.
