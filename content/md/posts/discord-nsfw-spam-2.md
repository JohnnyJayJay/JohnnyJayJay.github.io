{:title "How Discord scammers are trying to get GitHub users to install malware",
 :tags ["discord" "github"],
 :layout :post,
 :date "2024-04-11",
 :description "A new attack emerges in the Discord NSFW spam saga"}

Just a few days ago, I posted about [the NSFW spam epidemic on Discord](/posts/discord-nsfw-spam). The gist of it: bots promote servers that promise you NSFW content if you "verify your account" on a phishing site that imitates the Discord login page. I created a throwaway account, intentionally got it phished and gave it access to a single channel on one of my own servers to see what (if anything) the attackers would use it to post. As expected, it didn't take long for it to be used to promote the same kind of phishing scam. But today I saw that it got used to push something different, a scam that *uses* Discord horndogs to help with a *different* attack whose real target seems to be GitHub users.

I'm also posting this as a thread on Mastodon (soon) because it's a fairly short story. Leave your comments there if you have any.

## Story

It begins with NSFW spam much like we already know, except that the server being promoted looks really out of place; instead of having a picture of a woman as an avatar and a name like "NSFW + CHATTING + NITRO💖🍒🍑", it doesn't have a picture and is called "stephaniedavis". I (still) genuinely have no idea why that is. Perhaps the scammers simply forgot to finish their server setup.

In any case, that server doesn't contain anything except for *another* server invite, which allegedly has the goods. That server, in turn, promotes *four* different things that all try to extract value or scam people in different ways:

- links to *MEGA* folders containing stolen OnlyFans content from a handful of creators, behind a URL-shortener that claims it pays people sharing its URLs
- a website allegedly aggregating stolen OnlyFans content and reselling it (presumably, the scam is a really dumb one: you pay them in crypto or via PayPal friends & family and simply never hear from them again)
- two different ways to gain "full access" to the server's content (allegedly):
  - "download and run this exe!" (which, of course, [is malware](https://www.virustotal.com/gui/file/620431a7f8dfdd980179a9726865c3a9062532dfe0cbc920aa92e5947a1acc9c))[^1]\
    <img src="/img/discord-nsfw/malware.jpg" width="600" alt="Screenshot of Discord message, which a zip file and a screenshot showing a simple form attached. The content reads: All the NSFW content you are looking for is here
    – First, download, unzip the file 'Nudes Package Setup.zip' and run 'Nudes Package Setup.exe'
    – Once installed, open the application and enter the affiliate code NSFW-World to unlock an additional 5TB and help us
    – Enjoy!">
  - "create a GitHub account and star and fork this repo!" (...wait, what?)
  
So, obviously what made me raise an eyebrow is that last point. Here's their pitch:

<img src="/img/discord-nsfw/github-guide.jpg" width="900" alt="Screenshot of a Discord message, reading: In this server, we offer you a quick and easy way to access over 2TB of onlyfans videos on mega. No ads, no surprises, you do what you have to do, and you get your mega links directly, usable for life.
How do you do it?
It's really quite simple, you only need to follow 2 steps!
Step 1
First, create an account on github, following the instructions in #create-github-acc (if you already have one, skip to step 2)
Step 2
Add a star and create a fork for the project. To do this, follow the tutorial in #add-star-and-fork
Done, now all you have to do is enter your github username in the channel #validate-username and you'll receive your mega links automatically">

<img src="/img/discord-nsfw/star-and-fork.jpg" width="900" alt="Screenshot of some Discord messages in channel 'add-star-and-fork'. Content: Now click on the following link: https://github.com/Prescoter/Emora-Project (if you don't have a github account yet, create one with the tutorial in #create-github-acc)
Once on the page, click on this button (screenshot of GitHub UI with star button marked with red circle attached)
That's it, you've just sent your star to the github repo, now all you have to do is create a fork, and you're done!">

Ah yes yes, of course, makes perfect sense. A completely reasonable and unsuspicious thing to ask.

Now, what is that repo?

<img src="/img/discord-nsfw/github-repo.jpg" width="900" alt="Screenshot of a repository on GitHub called 'Emora'. Its 'About' reads 'Emora is an OSINT tool like sherlock but with a GUI, which search for accounts by username across social networks' It has 10 commits created by user 'Prescoter' and 2 releases, the last one being from 2 days ago. The repo has 376 stars and 344 forks.">

So, this repo ([link](https://github.com/Prescoter/Emora-Project)) pretends to be the source code for a real intelligence-gathering tool. I've skimmed the code and couldn't find anything suspicious. It essentially just consists of one bigger `.cs` file and some resources for the included Windows forms GUI. I think it's likely that the code is ripped from another (legitimate) project, but I can't say for sure. Assuming the code is indeed completely harmless, the repo's main purpose then is to get people to download/run (one of) the exe files [in the release](https://github.com/Prescoter/Emora-Project/releases/tag/v1.1).

<img src="/img/discord-nsfw/release-malware.jpg" width="400" alt="Screenshot of a GitHub release containing two exe files as assets, 'Emora.Portable.exe' and 'Emora.Setup.exe'">

And sure enough, those [seem to be malware](https://www.virustotal.com/gui/file/19e352ee48e427abee64454eb7236941a8c7bcdf5db3aba853fa5604019755b8). This [github issue](https://github.com/Prescoter/Emora-Project/issues/2#issuecomment-2011930286) gives us an idea of what it *actually* does: it apparently runs a crypto miner. Concerningly, people seem to take the "maintainer's" claims – that this malware is not real or simply a mistake – at face value given the apparent reputation of the project. Well, here's an idea for the future: consider GitHub stars meaningless.

## That's it

So, I suppose there's nothing too surprising in this story at the end of the day. I've reported the repository on GitHub (you should do the same). But I found the setup for this operation pretty out of the ordinary. Genius, really: why buy GitHub bots if you have an completely disconnected, real army of people on Discord who will follow even your most ridiculous order as long as they think they get porn at the end?

[^1]: Look, I've defended people falling for mediocre phishing, but to fall for this you have to be incredibly gullible. There are *so* many red flags here I can't believe anyone is actually doing this.

