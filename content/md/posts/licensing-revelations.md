{:title "Making Sense of Software Licensing"
 :description "You're probably doing it wrong somehow."
 :date "2023-06-21"
 :layout :post
 :tags ["licensing" "clojure" "spigot"]
 :comments {:instance "chaos.social" :author "johnny" :id "110582723242784973"}}
 
 A while back (my god, has it really been two and a half years? 😟), I wrote an [article](https://leftfold.tech/blog/posts/adopting-clojure-in-a-hostile-environment/) about writing [Spigot](https://www.spigotmc.org/wiki/about-spigot/) plugins in [Clojure](https://clojure.org). In that post, I presented what could be considered a proof of concept for how you can approach this unusual combination.

I also noted (prominently) that this was almost unheard of, i.e. that there were barely any traces on the internet of anyone trying this. In my conclusion, I wrote:

> However, I would definitely be interested in a more elaborate wrapper for Spigot written in Clojure and this will not be my last time writing a plugin in this language.

What I did not know at the time: writing Spigot plugins in Clojure is, well... a legal impossibility. For the same reasons, I technically was not allowed to distribute the code I wrote in this context.

Since September last year I went from being absolutely clueless about licensing to feeling like I opened Pandora's box. I've become fascinated by how complex software licensing is and how, at the same time, most open source developers who don't work on huge projects either a) get it completely wrong or b) don't give it much thought.

I hope that I can motivate some people who are serious about software development to start doing some research into licensing. And even if I can't, I'll at least use this post to explain why probably nobody will (or *should*) ever write serious Clojure software that involves Bukkit/Spigot/Paper. Excuse me if I'm rambling a bit in this one – this post is essentially a summary of what I've learned over the last months combined with a few personal thoughts on the topic. 

**Note: I am not a lawyer, neither have I got professional experience in this area.** I am just some guy who started reading license texts and doing some research. So take what I say with a grain of salt.

## Why License Your Code?

First things first, so we're all on the same page: it is absolutely essential to understand **how important** it is for an open source project to have a license. Without a license, the default laws regarding copyright in your country apply, which can effectively render your work unusable from a legal standpoint. 

In Germany, not having a license means that *nobody* except you is allowed to copy, modify or redistribute your code ([§§ 15 ff.](https://www.gesetze-im-internet.de/urhg/__15.html), [§ 69c](https://www.gesetze-im-internet.de/urhg/__69c.html) UrhG). Only some very limited exceptions apply to that rule, like citations ([§ 51 UrhG](https://www.gesetze-im-internet.de/urhg/__51.html)). Even then, the requirements for such exceptions can be very strict – it is in no way comparable to [Fair Use](https://en.wikipedia.org/wiki/Fair_use) in the United States, for example.\
If you publish your code on a website like GitHub, other users are [granted additional rights](https://docs.github.com/en/site-policy/github-terms/github-terms-of-service#5-license-grant-to-other-users), even if you don't make this explicit. However, these rights are still very limited. Crucially, they are confined in GitHub as a platform and don't permit modifications or redistribution. In the case of German law, this results in the funny restriction that you're allowed to download code from a repository on GitHub, but *not compile it* ([§ 69c UrhG](https://www.gesetze-im-internet.de/urhg/__69c.html)) if there is no additional license agreement.

Now, the specifics of what I've mentioned may only apply to Germany, but the "heavy copyright protection by default"-baseline can be found in the laws of many (most?) countries. And even if you lived in *The Kingdom of Stallman*, a hypothetical country where all written software is GPL-licensed by default, no sane person would ever bother to study the laws *of every country of every contributor of every dependency of every*... you get the idea. 

To put it another way: if you just put your code out there without granting visitors any explicit rights, you should assume that people who would otherwise be interested in your project will avoid it for that reason alone. And, as an avid open source enthusiast, this is surely not what you intend.

## The World of Licensing

In the last section I was probably mostly preaching to the choir – I'm assuming most readers already knew why declaring a license is important. But, be honest, do you *actually* consider your options and make an informed choice when applying one to your project? If you have a "default", have you thought thoroughly about its implications and whether it fits every one of your projects?

I think it's worth (re)examining these questions if you've never really read much into it.

My intentions for this section are not to convince you of adopting any particular license or to force any opinion on you. What I would like to do instead is to give you an overview of the options you may want to consider and give some context about the schools of thought behind them.

### Permissive Licenses

Permissively licensed software probably makes up the biggest share of licensed source code on the internet today. The essential idea of permissive licenses is that they allow you to do pretty much anything with the code with very few conditions. Most commonly, these conditions include providing a license and copyright notice when redistributing the original code. This can be found in the [MIT License](https://mit-license.org/), the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) and the [3-clause BSD License](https://opensource.org/license/BSD-3-clause/), among many others.

Sometimes, permissive licenses don't impose *any* conditions on the person obtaining the license. This is usually considered to be a [Public Domain](https://en.wikipedia.org/wiki/Public_domain) dedication, meaning the authors forfeit their copyright entirely (as far as that is legally possible wherever they are). Examples for these include [The Unlicense](https://unlicense.org/) and the [Zero-Clause BSD License](https://opensource.org/license/0bsd/)

What they all have in common is that they allow you to redistribute code *under different or additional terms*. This means, for example, that you can take an Apache-licensed piece of software, extend, modify or otherwise use it in your program and then license this program as a whole in any way you want, as long as you include a notice for the parts you didn't create yourself.

Of course, this makes it very simple to use your software for many purposes because the chances of license incompatibilities are really low. This is even true for [proprietary](https://en.wikipedia.org/wiki/Proprietary_software) use. In effect, permissive licenses are attractive for people who want their software to be used by everyone (including people effectively restricting its use in their own work) and/or who want to ensure compatibility with other software.

### Copyleft

As hinted at previously, the main point of criticism with permissive licenses is that they allow entities with more money/time than you to "take over" your project by making better or more changes to the software than yourself, therefore pulling users to their (proprietary) versions and, in the worst case, eventually making your initial open source project completely obsolete. In fact, this is a pretty good summary of what used to be [Microsoft's official strategy](https://en.wikipedia.org/wiki/Embrace,_extend,_and_extinguish) to eliminate open source competition.

In software licensing, there is a move to prevent this, and it is called copyleft.

Broadly speaking, a copyleft clause in a license requires that at the very least, modifications of the licensed software are released under the same license. In a very simple way: if you modify copyleft software, you have to keep its license, even for your own changes. Nobody can take copylefted code and release their own versions under a proprietary license. Thus, users retain the rights you granted them with your original work.

The most prolific family of copyleft licenses comes from the [Free Software Foundation](https://www.fsf.org/): the [GNU Licenses](https://www.gnu.org/licenses/licenses.html) (GPL, LGPL, AGPL). They all follow the rough idea described above, although with varying strictness. On top of the requirement to release modifications, copies etc. under the same license, the [General Public License (GPL)](https://www.gnu.org/licenses/gpl-3.0.html) also makes this a requirement for work that merely *links* to the licensed software (think adding a library to your Java project). The [Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl-3.0.html) goes even further – it is essentially the GPL with an added clause that closes a "loophole" in the GPL: the GPL is only effective for software distributed directly, but not if its functionality is, for example, put behind a web server. The [Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.html) goes in the other direction and is specifically made for libraries that should be allowed to be used by proprietary code, making it the least strict of them all.

There are some other weak copyleft licenses that usually copyleft copies and modifications of your own code (in some degree), but allow other uses under different terms. The most notable example is probably the [Mozilla Public License (MPL)](https://www.mozilla.org/en-US/MPL/), but also the [Eclipse Public License (EPL)](https://www.eclipse.org/legal/epl-2.0/), the first version of which [is used by Clojure](https://clojure.org/community/license). I'll come back to this one in particular later...

The obvious downside to copyleft is that people cannot just do anything anymore or use any license when using your software – compliance with copyleft licenses is typically more work or at least [more complex](https://www.gnu.org/licenses/gpl-faq.html#AllCompatibility) than with permissive licenses, especially if multiple kinds of copyleft are involved. Through the requirement that code has to be distributed *under the same license*, GPL 2.0 code [cannot be combined with](https://www.gnu.org/licenses/gpl-faq.html#v2v3Compatibility) GPL 3.0 code, for instance.

The upsides should be equally obvious. Even with weak copyleft you ensure that nobody can "hide" improvements to your software (in most cases), while stronger copyleft fully dedicates your code and its uses to open source (this is perhaps where your personal political or philosophical beliefs come in).

### Post Open Source, Ethical Source

This is the part where it gets a little more controversial and even more convoluted. You see, the licenses I've talked about so far weren't created in a vacuum – they reflect certain beliefs about *how the software world should work*. If you're not familiar with this story, let me give you a quick explanation.

What's most broadly accepted as "open source" today is [defined by the Open Source Initiative (OSI)](https://opensource.org/osd/). The OSI [started](http://web.archive.org/web/19981206185148/http://www.opensource.org/history.html) as a sort of splinter group of the [Free Software](https://www.gnu.org/philosophy/free-sw.en.html) movement in the 90s. The "open source" people split off from the "free software" people mainly due to ideological disagreements: free software stood for a rigid thought framework focusing on morality, while open source wanted to appease the industry and mostly argue for practical benefits of licensing software permissively. The OSI undoubtedly succeeded in their mission, at least to some degree, while the FSF became less relevant and still [doesn't like them](https://www.gnu.org/philosophy/open-source-misses-the-point.html) to this day.

However, open source and free software don't disagree on everything. On the contrary, they believe very much the same thing in practice, when it comes to licensing: anyone should be able to use software in any way they want; i.e., unconditional freedom. Who the user is and what they use it for does not matter – [even if it is "evil"](https://opensource.org/faq/#evil). This is a fairly radical approach, yet it has found mainstream acceptance. It makes sense, then, that there is opposition to it. 

This opposition is not homogenous and there is no uniting movement that is as significant as free software or open source, but perhaps the biggest contender for open source alternatives is [ethical source](https://ethicalsource.dev) which created the [Hippocratic License](http://firstdonoharm.dev/). This license makes restrictions based on human rights principles and, in its latest version, is even customisable (you can add "modules" imposing further restrictions). There is also the [Anti-Capitalist License](https://anticapitalist.software), which, contrary to what you might expect, is not anti-commercial but rather restricts its terms to use in freelancing, worker-owned businesses (coops) and education. Both the Hippocratic License and the Anti-Capitalist License have FAQs that elaborate on the rationale behind their creation. There is also [this article](https://logicmag.io/failure/freedom-isnt-free/) by Wendy Liu that touches on these ideas.

You could summarise the above mentioned approaches as "revisions" of the concept of open source: they are not open source or free software as understood by the OSI or the FSF, but they are still very much against proprietary software. Other alternatives or "post open source" practices are more about protest than anything else. Remember what I said about the importance of using a license? Well, there are some people who [disagree with even that](https://lu.is/blog/2013/01/27/taking-post-open-source-seriously-as-a-statement-about-copyright-law/) and make a statement by refusing to provide one on purpose. Others express their disapproval of licensing culture by using licenses that are "vulgar", like the [Do What The Fuck You Want to Public License (WTFPL)](http://www.wtfpl.net/).

The common theme in this category is the belief that open source as it exists today is essentially a failure, be it political, philosophical or otherwise. As a developer used to modern open source culture, one's initial reaction to such subversions may be one of dismissiveness, but I believe it is important to keep an open mind and always challenge the norms that brought us to where we are today. That's the primary reason I'm shining a light on this here. 

## Understanding Licenses
  
At the end of the day, a good choice, in my opinion, considers at least the following factors:

- practical considerations (how do you actually want your software to be used?)
- common practice (what are common choices in the community you're developing for?)
- personal beliefs (do you have any philosophical beliefs about licensing?)

Most importantly, though: actually read and understand whatever you're considering before you settle on anything. Read the license texts and accompanying, official material such as rationales and FAQ. It may seem boring or dry, but more often than not, licenses are actually written in a way that should make them fairly easy to understand for laypeople.

Do not ignore the licenses of your dependencies – ideally, get a tool that automatically collects licensing information from your dependency tree. Make sure you comply with those licenses. Do you actually include copyright notices when creating distributions of your software that include libraries, for example? For things like this, it is especially important to understand the differences licenses make (or don't make) between linking to code, modifying code and redistributing code, and whether you act on source code or some compiled form. Sometimes, these things underly different conditions that you should be aware of.

Make use of standards and guides that help you and others. Look into [SPDX](https://spdx.dev/) and SBOM generation. Consider following licensing guidelines like [REUSE](https://reuse.software/) – they make it easier to keep track of what you need to do.

## Minecraft, GPL and Clojure

We now get to the part that actually made me want to write this post – a realisation that some licensing is just broken forever.

In December 2010, a modded version of the official Minecraft server by the name of Bukkit was released. More precisely, it consisted of the modded server implementation (CraftBukkit) and an API (Bukkit) that programmers could use to write plugins that extend or change the server's behaviour. The original authors made an impactful decision right at the beginning: they licensed the Bukkit API [under the GPL](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/6635eba303c5513e52961fd315726394235e9903) and CraftBukkit [under the LGPL](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/5c37168fc3c52e36b6fff0dc72806ac81345cd1f). Since you *link* to the Bukkit API but not directly to CraftBukkit, this was a rather strange choice (the LGPL provides exceptions specifically for linking to libraries). What's more curious though is that *just two days after* these decisions were made, the creator noted [in a forum thread](https://bukkit.org/threads/what-license-is-the-bukkit-project-under.154/) that 

> Before you say anything we are aware that using the GPL will force plugin authors to GPL their plugin too and are considering changes to our choice of licensing. **Though this has no legal weight**, you can be rest assured that we will NOT be forcing people to GPL or open source their code and are looking for the best licensing for us to use.

Despite the statement "our license WILL likely change", the license never changed. To this day, Bukkit (and all software that is based on it, such as Spigot, Paper and more) are in this weird legal limbo where a) it is heavily questionable whether the GPL can even be technically applied [because CraftBukkit links to proprietary code](https://www.gnu.org/licenses/gpl-faq.html#GPLIncompatibleLibs) and b) there are no licensing terms in practice anyway because of the collective decision to not enforce them. Unfortunately, as the Bukkit author states in the above quote, this "consensus" does not really mean anything. Any current copyright holder of the Bukkit software (this includes all former Bukkit contributors) *could* turn around and take legal action against the widespread license violations from people writing plugins for Bukkit, Spigot etc.
Would this be seen as arbitrary and unfair, after all this time? Sure. But whoever were to take such a dramatic step would probably still be in the right (and [they would not be the first](https://blog.jwf.io/2020/04/open-source-minecraft-bukkit-gpl/)).

Enter the [EPL 1.0](https://www.eclipse.org/legal/epl-v10.html), Clojure's license. Clojure, like other JVM languages, is essentially just an additional JAR at runtime. This means that, by writing a Clojure program, you link to EPL 1.0-licensed code. This in itself does not impose very heavy restrictions on the licensing of your code since the EPL "doesn't care" about the code you wrote, just about the code originally licensed under the EPL (weak copyleft). Here's where this becomes a problem though: linking to a GPL project (such as Bukkit) requires you to license the *entirety* of your project under the GPL. This, unfortunately [does not work with the EPL](https://www.eclipse.org/legal/eplfaq.php#GPLCOMPATIBLE), making it practically impossible to use the GPL in the Clojure ecosystem (which has been a point in the [broader criticism](https://www.juxt.pro/blog/prefer-mit/) of using the EPL for your own code).

Did this cause me to start panicking and to quickly wipe the code I wrote off the face of this earth? No. And I won't unless someone complains about it who is in a position to. Seeing the state of Bukkit-based plugin development, it is not very likely that anyone will ever take this seriously.

It did, however, make me think about how messed up this is. We see licenses as the fundamental mechanism for conveying our ideas about how software should be written. Licenses are the tiny bit of leverage we have in the broken world of copyright. Yet most of us don't really seem to grasp what's going on. A license, for most, is a file you add to your repo, and that's it. And look, I get it: part of what's so messed up is the fact that this shit is complicated as hell. And it's not exactly an exciting topic either (unless you're a nerd with an affinity for dry legal stuff...).

I don't have a solution for this problem. All I know is that there is something wrong with the copyright system we have today and with our economic perspective on software. The least we can do right now is to become more aware of how software licensing works and how to make it easier for fellow developers to understand the licensing of our projects so we can hopefully work together to fix the mess eventually. 
