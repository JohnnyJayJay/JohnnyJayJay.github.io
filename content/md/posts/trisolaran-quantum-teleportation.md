{:title "\"The Three-Body Problem\" and Quantum Teleportation",
 :tags ["quantum" "scifi" "physics"],
 :layout :post,
 :date "2024-03-09",
 :unlisted? true
 :description "How the Trisolarans broke modern physics"}

I recently finished reading the excellent *Remembrance of Earth's Past* trilogy (perhaps more commonly known by the name of its first entry, *The Three-Body Problem*), a series of science-fiction novels about humanity's reaction to an alien civilisation's plan to invade Earth. In it, the author, Liu Cixin, tells an incredibely rich, nuanced and imaginative story about the future of humankind, in a way that I have never seen in science-fiction before. I was particularly impressed with his creative and unique inspirations drawn from real-world physics and astronomy, which rarely felt arbitrary or pseudo-scientific and made it apparent that he has a good amount of actual knowledge in these fields. One of the physical concepts that play an important role throughout the entire series stuck with me: quantum teleportation. 

⚠ **Major spoilers for the first entry of the trilogy ahead!**

## In 400 years, humanity will have a problem

The basic plot of *The Three-Body Problem* is this: a secret Chinese cold war-era project succeeds in establishing contact with the extraterrestrial system of *Trisolaris*, whose inhabitants are looking for a new planet to colonise due to the hostile conditions in their corner of space. Through this contact, Earth's location is exposed to the  Trisolarans, who immediately head for the solar system in order to conquer it and destroy humankind. But there's a twist: even though they are vastly superior to Earth in terms of technology, their spaceships still need around 400 years to arrive, inadvertently giving humans time to prepare.

Obviously it is in Trisolaris' best interest to prevent Earth from making significant technological progress during this time. A lot could happen in 4 centuries, and the aliens do not have capacities to achieve scientific breakthroughs on their voyage. The speed of exchanging information is capped by the speed of light, as the specific theory of relativity tells us. While that is pretty fast to us, the Trisolarans are so far away from Earth that this still means years of delays between messages. Any sort of coordinated real-time influence using classical communication is therefore impossible. So, given that they are not on Earth yet and the effectiveness of classical communication depends on human cooperation, how could they possibly make sure that Earth stays inferior until they get there? 

## The aliens have a cheat code

One of the major mysteries throughout *The Three-Body Problem*, especially before the future invasion of the alien species becomes known, is the inexplicable and abrupt halt of progress made in research across the entire world. The results of controlled experiments in particle accelerators suddenly become arbitrary, unpredictable and seem to contradict what we thought were laws of nature. After a while, it's clear that Trisolaris has something to do with this, but how? Later, towards the end of the book, we get an answer: sophons.

Sophons are essentially supercomputers in the form of a single proton, i.e. the size of subatomic particles. Being so tiny, Trisolaris was able to accelerate them to a much higher speed than the Trisolaran fleet and consequently have them arrive much sooner on Earth. A handful of protons are, of course, not really detectable, so nobody was aware of their presence. Their primary task was the disruption of experimental physics relating to particles (by simply messing with the experiments on a subatomic level), which explains the aforementioned problems in human research. The explanation for how these devices have been built by Trisolaris is cool, but not of relevance in this post.

Interestingly, the sophons are not some kind of fully autonomous AI, but actually directly controlled by Trisolaris, in real time. They can collect incredibly detailed information from anywhere in the world and share them with Trisolaris *instantaneously*, i.e., with no delay. That is why they're a sort of cheat code: nothing humans say or do is secret anymore, everything can be seen by the hostile aliens. This incredible disadvantage requires humanity to come up with all kinds of crazy and unique strategies to respond to the threat.

But wait a second, didn't I just say that information can't be transmitted faster than the speed of light? How can they interact with Earth in real time if they're so far away?

## Quantum ~~magic~~ mechanics

It's a bit of a sci-fi trope at this point. When in doubt, just call something "quantum" to lend it scientific credibility in the eyes of a layperson. And indeed: sophons, the proton supercomputers of Trisolaris, supposedly communicate in real time through the use of quantum entanglement, according to the book's explanation. So there you have the answer: they defy the laws of physics "because quantum". But there's actually something more to this.

You see: quantum mechanics, i.e. our idea of how subatomic physics work, is weird. *Very* weird. So weird in fact, that world renowned physicists [have said](https://www.nytimes.com/2019/09/07/opinion/sunday/quantum-physics.html) things like "I think I can safely say that nobody understands quantum mechanics". Certainly weird enough for Albert Einstein to think that our understanding of it can't be right, even though it works.

There are essentially two things that make the theory of quantum mechanics weird:

1. Quantum mechanics is not *realistic* in the philosophical sense: observing a system influences its state, which means that there is no state independent from observation. In other words: what you measure will never be what it was *before* you measured it. This conflicts with our intuitive reasoning that the result of a measurement must already exist before measuring.
   - this *non-realism* comes from the concept of *superposition*, the idea that a quantum system can be in multiple states *at once* 
2. Quantum mechanics is not *local* in the sense of the theory of relativity: interacting with one system can influence another system *instantaneously*, no matter how far apart they are. Remember that relativity forbids *anything* travelling faster than the speed of light. But if I interact with a particle and that influences a particle on the other end of the universe without delay, something has happened that does not respect this speed limit!
   - this *non-locality* comes from the concept of *entanglement*, the idea that two quantum systems can be physically separate but still influence each other

As I said, quantum entanglement is the explanation for sophons in *The Three-Body Problem*. The gist of it is simple: 

1. You take two quantum particles (such as photons or protons)
2. You entangle these particles such that the state of one determines that of the other
3. You keep one and send the other off to Earth
4. Now when the one on Earth is observed/measured, you instantaneously (faster than light!!!) see the result on your side.

Now just expand this system, use more entanglement and more particles and you've built an information teleporter. Is that right?

<!-- Maybe another heading for answer to question about instantaneous sophon communication and intro to quantum mechanics? before "Einstein was wrong" with EPR-Paradox, Bell's theorem -->

## Einstein was wrong

As described earlier, quantum mechanics is neither *realistic* nor *local*, two very important cornerstones of what we now call *classical* physics. Essentially, classical theories, i.e. theories that conform to the principles of realism and locality, are "reasonable". The output is always determined by the input, you can view systems in isolation. This is nice.

Einstein wanted to have a classical theory for quantum systems, so he was convinced that there had to be something missing from quantum mechanics as we know it. To put Einstein's thoughts very bluntly: surely, there must be some piece we're missing that, if added to quantum mechanics, would make the whole thing... make sense? That piece would then give us a theory that does not conflict with the theory of relativity (which is both realistic and local). This idea became known as the [Einstein-Podolsky-Rosen (EPR) paradox](https://en.wikipedia.org/wiki/Einstein%E2%80%93Podolsky%E2%80%93Rosen_paradox). Although like most things we call "paradoxes" it wasn't actually one; it was an argument that if the entire physical world can be modeled classically, quantum mechanics must be incomplete.

How do you respond to an argument like that? We never found a missing piece. But how do we *know* for sure that we're *not* missing something from quantum mechanics? This is where physicist John Bell came to the rescue.

Put simply: he took the EPR paradox ("there must be a classical quantum theory that adheres to realism and locality") and derived a mathematical statement from it. This gave him the following statement: "If there is a classical theory for quantum systems, then this statement must be true" and conversely "If this statement is *not* true, there *is no* classical theory for quantum systems".

At first, this answered the EPR paradox only via thought experiment; later, it was confirmed in the real world as well. Bell and physicists after him were able to show that the statement he derived from the EPR paradox was *not* true for quantum systems, so the efforts to find a quantum theory that "makes sense" as imagined by Einstein and his pals had been futile. There simply is no such theory.[^1] This is [Bell's theorem](https://en.wikipedia.org/wiki/Bell%27s_theorem): to model how quantum systems work, your theory must be either non-realistic or non-local (or both).

[^1]: I think it's interesting how this argument among physicists about whether there is a classical quantum theory or not resembles the argument among mathematicians about whether we can define the natural numbers using axiomatic logic. The first would have given us a kind of "god equation" about the universe, while the second would have given us proof for everything that is true. Not too long apart from each other, both hopes were utterly destroyed: one by Bell, the other by [Gödel](https://en.wikipedia.org/wiki/G%C3%B6del%27s_incompleteness_theorems).


## Quantum information theory ruins all the fun
