{:title "Web Design Doesn't Have to Be Complicated"
 :description "I redesigned this blog and tried to not make it suck"
 :date "2023-09-18"
 :layout :post
 :tags ["meta" "web"]}
 
I am not a web designer. In fact, I'm usually pretty bad at creating any sort of visual design. And that's fine, I think. There's always something that you're not good at or that you don't enjoy, and it's not a big deal because there are plenty of other people who *are* good at it. Still, something about the fact that I never really made a decent-looking website myself didn't sit right with me. So I figured I'd take it upon myself to redesign this blog and try to get out of my comfort zone.

## Cryogen, a niche static site generator

This blog is a "static" website, which in this case doesn't mean it has no dynamic elements at all, but that it consists purely of HTML, CSS and JavaScript without any dynamic communication to a server.
Of course, *there is* a server, that's just a requirement of putting anything on the web, but it only serves files as-is. What's nice about static sites is that there are many easy ways to get them published on the web: this blog for example uses [GitHub pages](https://pages.github.com/) and thus doesn't cost me a dime to host. 

Since websites like blogs typically consist of many different pages that use some common layout (like nav bar and footer on this one) and people tend to prefer writing their posts in a language that gets a little less in your way than HTML, there are now *a lot* of tools to *generate* static HTML from Markdown and similar, with things like layout and style typically extracted into a reusable theme. And if you're thinking about making a website where you just want to "put stuff", I recommend going down the path of static site generators as well. It's very convenient, and there are lots of great options out there. If you want something with many features out of the box and many available themes, take a look at [Hugo](https://gohugo.io/) or [Jekyll](https://jekyllrb.com/). If you want a very simple setup, [Eleventy](https://www.11ty.dev/) might be worth a look.

The generator I chose to use for this site is [cryogen](http://cryogenweb.org), and while I think that it does its job just fine, I wouldn't necessarily call it the most beginner friendly since its documentation is a little lackluster and it's not being super actively developed. Honestly, the main reason I picked it was that it's part of the Clojure ecosystem and I therefore feel comfortable hooking into it and extending it. I haven't really had to extend it so far, but it's a nice option to have.

A downside of being a rather niche static site generator is that there aren't many available themes. Back when I installed it, cryogen shipped with three themes, none of which I really liked. The theme that I used before for this blog ([lotus](https://github.com/KingMob/cryogen-theme-lotus)) is now at least part of the distribution. But still: having more choice can't hurt, and making my own theme was the best way to come close to what I wanted.

## Desirable (non)features

Whether a theme is good or bad is up to one's personal taste. Over the years, I've become convinced that most websites are essentially too complicated. Even though a lot of big websites use fancy animations, transitions and what have you, at the end of the day they still serve hyper**text**. Do we really need to put everything behind some react component?

The most extreme form of this sentiment can be found on the hilarious and fantastic [motherfuckingwebsite.com](https://motherfuckingwebsite.com) as well as its [better](http://bettermotherfuckingwebsite.com/) and [perfect](https://perfectmotherfuckingwebsite.com/) iterations. There is [a great cryogen theme](https://github.com/knows-the-cost-of-nothing/detoxified-dark) inspired by these sites that I got the term "nonfeatures" from. I think, in a day and age where every website seems to be stuffed to the brim with useless crap, it's a great term to focus on what's actually important. Here's my list of (non)features that I wanted to have:

- No tracking, no external services. Is there no Google Fonts alternative? Do we really need analytics? Is the CDN for one JavaScript file really necessary? The answer to all of those questions is "no".
- No forced use of JavaScript. There's no good reason why someone who doesn't have JavaScript enabled shouldn't be able to read my posts, which are *literally just text*.
- Automatic light/dark mode (alternatively: not throwing flash grenades at dark mode users). It's 2023 and respecting the user's preference is as simple as `@media (prefers-color-scheme: dark)`.
- No crap that obscures accessibility. Your website may look fancy, but why is it just a bunch of `div`s and `span`s? Start using [the tools](https://www.w3schools.com/html/html5_semantic_elements.asp) we've had for ages already.
- No huge libraries. Bootstrap for a blog? Please no.
- Decent handling of mobile devices. While this is a given if you follow the motherfuckingwebsite doctrine, it is not that simple if you add a bit more style (which is not bad in of itself). I feel like this point in particular is something that nerds like me sometimes tend to treat as irrelevant but the fact is, most people will access your site on mobile. And yes, I bet that percentage is high even for your niche blog about how to build an operating system in Forth, or whatever it is you're doing.

## Presenting: chota

My [chota theme](https://codeberg.org/johnnyjayjay/cryogen-chota) is an attempt at implementing these (non)features. It removes all references to external resources and the only third-party libraries are highlight.js (which you can also just... remove) and [chota](https://jenil.github.io/chota/), the name of the CSS library I used for styling. I actually quite enjoyed working with chota - it only has a couple defaults and a handful of components but all of them were useful:

- `grid`s are flexboxes that remove the pain of flexboxes. You can just declare some rows and some columns and it ends up in the correct place without any extra work. I used those in the footer and the "previous"-"next" links.
- `button`s allowed me to directly turn links into different styles of buttons. Especially useful was the `icon` class, which adds icon support to a button.
- `nav` is what you see at the top of the page, together with some `tabs`.
- `card`s are used for the post previews on the home page.
- `tag`s are used for... well, you guessed it.

Note that this didn't remove manual styling altogether, I still created a bunch of my own classes. But it made the experience pretty straight forward.

The downside to a "micro framework" (as they call it) like chota is that the components (except for the grid) have their own style and websites using them will probably end up looking fairly similar. I don't really have a problem with this though – I like the look of this blog and I don't think I would complain if I saw something similar.

### Tangent: class-free CSS

In a recent exchange on Discord, there were mentions of [water.css](https://github.com/kognise/water.css) and [modern-normalize](https://github.com/sindresorhus/modern-normalize), two CSS stylesheets that serve a single purpose: Provide some sensible defaults for websites using pure semantic HTML. Apparently, there is [a whole list](https://css-tricks.com/no-class-css-frameworks/) of these - and I think that's awesome. With free and simple static page hosting at the tip of our fingers, maybe we should just make more web pages. I mean literal *pages*. Have a certain message to share? Why not just put it in an HTML file and upload it somewhere? And by including one of these dead simple CSS stylesheets, it doesn't even end up looking like trash.

For someone who already knows their way around CSS, replicating what these defaults do won't be difficult, of course. But it could help beginners get a foot in the door and help making things quicker and more consistent for anyone else. The next chance I get, I'll try a bunch of these (but, being the sucker for mildly amusing naming that I am, it's probably going to end up being [Marx](https://codepen.io/mblode/pen/JdYbJj)) and perhaps report back. 

## Future work

I'm not entirely done working on this theme, I have a couple more ideas that I would like to add as (optional) features. I'll just dump them here.

- My theme does not support disqus, the commenting service supported directly by cryogen. I've removed its integration from my blog as well because it tracks everyone without consent (there literally is a button reading "Do not sell my data" in the disqus embed, you can't make this shit up) and the comment UI isn't that great anyway. It certainly isn't *simple*. As an alternative, I'm looking to implement support for [embedding threads from Mastodon](https://carlschwan.eu/2020/12/29/adding-comments-to-your-static-blog-with-mastodon/) (and maybe other Fediverse places in the future) that can be used as comment sections for posts.
- Drew DeVault has a cute little tool called [openring](https://git.sr.ht/~sircmpwn/openring) he uses to automatically link to posts from other blogs at the end of his posts. I think this is a great idea for building a simple network of static blogs and I want to look into adding it to my theme.
- I've been working on a protest against [Web Environment Integrity](https://arstechnica.com/gadgets/2023/07/googles-web-integrity-api-sounds-like-drm-for-the-web/) in the form of a banner at the top of the page when rendered in a browser that supports this "feature". I could add the protest to the theme natively, allowing users to enable it simply using a config option.
- Similar to the previous feature, I've seen the concept of ["inverse anti ad-blocking"](https://stefanbohacek.com/project/detect-missing-adblocker-wordpress-plugin/#resources) banners, i.e. banners that complain if you *don't* have an ad blocker. I find the idea pretty funny at least and perhaps even genuinely useful. 

From this list, the Fediverse comment integration definitely has the highest priority. But until that's added, feel free to head over to the [chota theme's repository](https://codeberg.org/johnnyjayjay/cryogen-chota), try it out yourself if you use cryogen, and send me feedback about the state of this blog. Thanks!
