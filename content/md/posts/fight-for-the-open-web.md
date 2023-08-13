{:title "Resist Google's Attempts to Ruin the Web"
 :description "It's time to ditch Chrome, once and for all"
 :date "2023-04-22"
 :layout :post
 :tags ["web" "meta"]
 :draft? true}

I suppose it is not news anymore that the 2020s seem to be the decade of everything on the web turning to shit. Really, it's been just a constant stream of everyone getting screwed over at every corner.
The most ancient discipline of getting-screwed-over, so-called "digital rights management" (DRM), continues strong this year with Google proposing incredibly invasive content restriction for the web.

## What's Going On?

In the last few decades, tech companies have made it their mission to exercise as much control over the users of their products as possible.
Through an intricate design of "attestation", "trust" and supposed "protection", they have managed to effectively take the ownership of things we *used to* own from us.

We don't truly *own* most devices we buy – there are mechanisms in place to prevent us from using them in a way that doesn't benefit the manufacturers. 
We certainly can't repair, modify, root, bypass our own phones, computers, cars or printers anymore or when some of these things are still *technically* possible, it is met with more and more corporate resistance.

Similarly, we don't *own* our software anymore. So many things these days are "cloud-based", require a subscription or use some other form of DRM that makes sure we don't legally own what we posess.

But this isn't enough for these corporations. There is a thing that bugs them: they can't entirely prevent you from abandoning their ecosystems because a lot of those ecosystems are based on open standards that can be implemented by non-profits, individuals and communities. This is a problem for them: if people are still *free to choose* what devices, tools and browsers they use to begin with, the corporations run a risk of losing their fight in the future and miss the opportunity to tighten their grip on the world even more.

## Web Environment Integrity

Lucky for them, they seem to have found a solution, or at least a new stage of power escalation. They call it ["Web Environment Integrity"](https://github.com/RupertBenWiser/Web-Environment-Integrity).
This new API will allow websites to "verify" that you are using a "trusted" device, operating system and browser environment. Who decides what counts as "trusted", you ask? Well, you're never going to believe it, but it is in fact those same corporations that have a financial interest in forcing you to use their products.\
Even with the stated benefit that this will make the web more "safe" (and ignoring the fact that companies like Google effectively [produce malware](https://www.gnu.org/proprietary/proprietary.html) themselves), this proposal adds a backdoor for anyone wanting to take control of most of the web by marking unfavourable browser extensions, browsers, operating systems, devices as "not trusted". "Oh, you're using something that we don't control? Sorry, that's unsafe. For your own protection, we will block it."

The proposal suggests mechanisms to prevent this from happening, but they are laughable and undermine the idea itself. They can essentially be condensed as: "To prevent the system from killing the open web, we'll have to stop using the system sometimes". Gee, thanks.

At the end of the day, WEI adds yet another way to [force computers to rat you out](https://pluralistic.net/2023/08/02/self-incrimination/#wei-bai-bai).

## Resist

No matter what the intentions of the authors were or if they managed to fool themselves into believing that creating this leverage for autocratic control freaks is somehow a good thing, we have to fight it. We won't be able to prevent it from being implemented (Chromium already [went ahead and did it](https://github.com/chromium/chromium/commit/6f47a22906b2899412e79a2727355efa9cc8f5bd)) and Google certainly doesn't care about even the loudest opposition. After all, the average Joe using Chrome and browsing social media probably won't even notice the change! But using this observation as a reason to dimiss concerns is silly and dangerous. WEI may not have immediate consequences for everybody, but it is another step towards the goal of taking away the last bits of freedom we have in the world of technology and killing legitimate tools just because they don't benefit those in power.\
Google *already has* immense control over the web as it stands – they run cloud services, DNS, the de facto entirety of internet search as well as some of the most used websites in the world. That, and a shitload of traffic goes through their servers because every other site uses Google services like fonts, analytics or advertisements. With WEI, the speed of this centralisation will only accelerate. Now, they want to control who can participate in the web to begin with.

I therefore call to anyone reading this: make noise, tell other people about it. Stop using Chromium, Chrome and other browsers that stand behind this idea. My suggestion is to switch to Firefox, the *only remaining* browser basis not driven by greed and profit. Mozilla is [outspoken](https://github.com/mozilla/standards-positions/issues/852#issuecomment-1648820747) against WEI – apparently, they still give a shit. We'll see how that develops, but for now, they are allies and have a decent track record as such.

For all we know, WEI might still be stopped in its tracks – After all, we haven't heard statements from other influential companies yet (although we certainly [won't be able to count on Apple](https://httptoolkit.com/blog/apple-private-access-tokens-attestation/)), and it will be interesting to see what jurisdictions with decent anti-trust law have to say. If not the ultimate goal, the effect of this project is the elimination of competition in web technology and access, and entities like the EU might not be too happy about that. But this is all just speculation for now; we *already* know that this is a *bad thing* and must be stopped.

## A Protest Message

WEI wasn't introduced with a lot of fanfare: it started as an innocuous, vaguely worded Markdown file on a GitHub repository of a Google employee. But without responding to ciriticism and discussions (as promised in the original proposal), Google unilaterally decided to fast-track the feature into Chromium. Hence, most people aren't even aware of what's happening yet – not even all web developers! Shouting about it in our little bubble of free software nerds therefore won't do much.

We need to spread awareness and tell people what's going on. The best way to do this is directly on our websites. This is why I created [my own form of protest](https://github.com/JohnnyJayJay/wei-protest): it detects whether the browser the user is accessing your website with supports WEI, and if it does, a protest message (with a non-technical explanation) is displayed. The user can then expand this message to include a FAQ about WEI and the protest. 
The message can show either as a permanent banner on top of your page or as a full-on content blocker. It's up to you.

I am going to integrate this protest into this blog, and you are free to follow. If you run a website and you would like to participate in the protest, you can find [instructions](https://github.com/JohnnyJayJay/wei-protest#take-part) for how to do just that in the repository. You can also [help with translation](https://github.com/JohnnyJayJay/wei-protest#translations) of the protest message and FAQ - I want to make this as international as possible. Your collaboration in this project is appreciated. 

## Final Words

"Web Environment Integrity" is a euphemism for an idea that, in reality, *destroys* the foundations of the web. We have lost many battles to DRM before but this one might be one of the most important ones ever. If victorious, WEI will lead to censorship, worse accessibility as well as more vendor lock-in and corporate control. Essentially, there will be no hiding place from corporations seeking to extract profit from us anymore. This is wrong and evil and must be prevented. True web *integrity* means keeping the web an open system. 
