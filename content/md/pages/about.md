{:title "About"
 :layout :page
 :page-index 0
 :navbar? true}

Welcome to Left Fold, my personal corner of the world wide web.

## About Me

My name is Johnny. In many places, I'm also called "JohnnyJayJay" if I came too late to reserve "Johnny" as a username. But just call me Johnny.

I study computer science at the Karlsruhe Institute of Technology (KIT) in Germany. I also work part-time as a Java backend developer and, more recently, as a DevOps guy™.

Over the last couple of years, computer science has captivated me, not just in terms of my career and studies, but also personally. I have dipped my toes into many different areas of the field, especially in programming. And I try to continue to expand my horizon almost every day.

On the other hand, I am highly critical of many current technological developments. I reject the notion that technology is apolitical and that constant expansion is the ultimate good. I'm for privacy, data protection, user control on the technical side, and for ethics, solidarity and community organisation on the social and structural side of technology.

Tech topics don't take up my whole life, however. Here are a few of my other hobbies and interests:

- cooking and baking
- music (most things that are adjacent to: Jazz, Funk, Soul but also: Punk)
- linguistics (I only speak German, English and French, but I love learning about the languages of the world and their relationships)
- video games (particularly single player stuff that lets me make time for myself)
- law (this is part of my studies as my chosen secondary subject)

### Contact

Do you want to talk to me about something? I'm happy to respond to most things, unless they're one of the following:

- trying to recruit or get me involved in some sort of "crypto" or "web3" bullshit
- questions about my personal life outside of the scope that I talk about publicly
- questions asking how to solve some (programming) problem that you have and that is not related to me or my work. Please ask in some public forum, Matrix channel or Discord server made for this purpose. 

The easiest way to message me is via [Matrix](https://matrix.org). My ID there is [@johnny:yatrix.org](https://matrix.to/#/@johnny:yatrix.org). I'll probably also see it if you mention me somewhere on the [Fediverse](https://fediverse.info/) where you can find me under [@johnny@chaos.social](https://chaos.social/@johnny). If you have none of these things, you can also send me an [email](mailto:johnnyjayjay02@gmail.com). 

### OpenPGP

If I cryptographically sign something like an email, a commit or a release, I use [my PGP key](/key.asc), which can be found here and on keyservers. You can also use this key to encrypt your emails to me.

## About this Blog


This is a personal blog where I try to share knowledge, perspectives and insights I have gathered during my process of learning or just things I find interesting. I will write about whatever I feel like writing about - though the primary focus will be technology and programming. I might also use parts of this site to dump links and notes about certain topics. That won't be part of the post feed though.

### About the Name

"Left fold" is the name of an operation commonly found in functional programming. It is a very generic tool to perform some sort of *reduction* over a sequence of *things*.

Here in Clojure:

```clojure
(reduce + [1 2 3])
```

or Haskell:

```haskell
foldl (+) 0 [1 2 3]
```

If the reducing function (like in the above example) is [associative](https://en.wikipedia.org/wiki/Associative_property), this is equivalent to a *right fold*, which also reduces over sequences, using a different strategy. But fundamentally, these two do different things. Right folds are lazy and stall as long as possible before doing their work, while left folds are eager and get things done, even providing easy access to intermediate results. There is a clear winner here!

Jokes aside, there isn't any deep reason behind this name, except that I think it's a cool name and that I like the operation in programming. Oh, and the fact that I feel like a "left" in my blog's title fits me more than a "right".

#### The Symbols

So, what does `𝔽/𝕩` mean? This is a mix of notations from two different [array programming](https://en.wikipedia.org/wiki/Array_programming) languages, [APL](https://en.wikipedia.org/wiki/APL_(programming_language)) and [BQN](https://mlochbaum.github.io/BQN/). `𝔽` is the *reducing function*, `𝕩` is the *sequence*. Both come from BQN where they mean *function* and *right argument* (value) respectively. `/` is the [reduce operator](https://aplwiki.com/wiki/Reduce) in APL. It's actually a right fold (and unfortunately APL doesn't have a left fold), but it looks better (imo) than the correct BQN notation which would be `𝔽´𝕩`.
