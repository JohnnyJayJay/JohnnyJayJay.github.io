{:title "Does an opt-out social media bridge violate the GDPR?",
 :tags ["web" "fediverse" "social media" "law"],
 :layout :post,
 :date "2024-03-03",
 :description
 "On lawfulness through consent and legitimate interests, transparency and other privacy concerns",
 :toc true,
 :comments
 {:instance "chaos.social", :author "johnny", :id "112031806514449928"}}

About 2-3 weeks ago, the Fediverse (the federated social network based on the [ActivityPub](https://en.wikipedia.org/wiki/ActivityPub) protocol) was in quite a stir: one of its users [announced](https://snarfed.org/2024-02-12_52106) that soon, it would be possible to follow and interact with BlueSky users and their posts *from* the Fediverse. This would be made possible using a [third-party application](https://github.com/snarfed/bridgy-fed), a *bridge*, that would automatically relay (public) posts back and forth. The kicker: you wouldn't have to move a muscle to be recognised by the bridge, your Mastodon account would be available on BlueSky as soon as anyone there would try to follow you. If you *didn't* want your posts to be shared on BlueSky, you would have to *opt out*. Needless to say, this caused a major backlash among the largely privacy-minded, tech-oriented fedi crowd.

The centre of this debate was rather vague. People came up with plenty of both good and bad arguments against the proposed modus operandi of the bridge. Among them were personal distrust of BlueSky, safety concerns regarding BlueSky's (lack of) moderation, the insufficiently thought-out mechanism to actually opt out, the lack of consent to begin with and finally, the legality of the whole thing with regards to data protection laws. I will ignore most of these arguments and instead focus only on a question arising from the last: **does/did the BlueSky opt-out bridge violate the General Data Protection Regulation (GDPR)**? 

The main part of this post is about the question of whether the bridge, in the way it was pitched, could be *lawful*, i.e., whether there is a *legal basis* to support an opt-out system. I will then briefly talk about two other points of interest in the GDPR that could be problematic, and finally I'll leave some open questions.

Before I get into it, I want to note two things. First: I am not a lawyer or data protection expert. However, I currently study the GDPR in the context of a university course on data protection law, which is one of multiple courses I take on German and European law. Second, I was a little worried that [Ulrike Hahn](https://fediscience.org/@UlrikeHahn) had already said everything I wanted to say with her [article](https://write.as/ulrikehahn/bridging-to-bluesky-the-open-social-web-consent-and-gdpr) on the topic. Fortunately for me, her focus was on something else. If you want to read more about the notion of consent, the exception for processing sensitive data that has been made public or the privacy policies of mastodon.social and BlueSky, check out her post.

## How is a bridge affected by the GDPR?

The GDPR is a piece of legislation that regulates the *processing* of *personal data*. 
The term personal data isn't exactly easy to nail down but, relevant for us, social media posts definitely count as personal data.
Processing on the other hand is a very broad term, it encompasses pretty much *anything* you do with data. Definitions for these terms can be found in [Article 4](https://gdpr.eu/article-4-definitions/) no. 1 and 2 of the GDPR.

If we apply them to the bridge, processing of personal data occurs because the bridge retrieves, stores and disseminates posts, profile pictures, bios etc. And since this processing affects people in the European Economic Area, the bridge must comply with the GDPR. This answers the question how the GDPR is even part of this discussion.

Now, the next step is to look what the GDPR has to say about the legality of personal data processing. [Article 5(1)a](https://gdpr.eu/article-5-how-to-process-personal-data/) states that processing must be "lawful" and "transparent". As it turns out, whether processing is lawful or not is governed by the principle of "prohibition with the reservation of permission", meaning it is generally *unlawful* unless explicitly permitted. Fortunately, all possible ways to lawfully process personal data are listed exhaustively in [Article 6(1)](https://gdpr.eu/article-6-how-to-process-personal-data-legally/).

## How can a bridge process personal data lawfully?

So, in the last section we learned: the bridge processes personal data and must therefore fulfill one of the conditions of lawful processing in [Article 6](https://gdpr.eu/article-6-how-to-process-personal-data-legally/) GDPR. Of the different conditions, there are really only two that have to be examined more closely: Consent (Art. 6(1)a) and legitimate interests (Art. 6(1)f). Two words that have probably been engrained in your brain after years of trying to dismiss cookie banners. 

### Consent

Does a social network bridge have consent to relay users' posts? Some of the people defending the opt-out approach of the BlueSky bridge argued that, simply by *being* on the Fediverse, you essentially must have consented to your posts being sent to anywhere else, because you don't preemptively control what servers you federate with (unless you configure your Mastodon instance very restrictively or similar). In other words: you consent to your data being sent to random servers joining the Fediverse, so you also consent to your data being sent to BlueSky.

Now, there are a few things to unpack in this argument. As I said in the beginning, I don't want to talk about the moral or ethical aspects, so what I'm left with is "consent" in the legal sense. And, as you may have guessed, this argument doesn't match the meaning of "consent" in the GDPR at all. [Art. 4(1)11](https://gdpr.eu/article-4-definitions/) defines consent as a "specific, informed and unambiguous indication" that a subject agrees to having their personal data processed. This indication must happen through a "statement" or a "clear affirmative action". From this it is already quite clear that nobody consented to the processing of the bridge directly; nobody was asked for consent and "no response" was sufficient to be affected by the processing.

A common misunderstanding of the GDPR is that consent is the *only* way to process data lawfully. We have already learned that this is not the case, but this misconception leads people to believe that we "consent" to data processing by visiting or signing up to a website. And while yes, often we are asked for consent, we are not *always* asked for consent and that is perfectly fine. Would you like to be asked for consent to process your IP address every time you connect to a web server?\
Where this is relevant in the "you already consented"-argument is that it wrongly supposes you *actually gave your consent* to allow your instance to send your data to other servers. There are three problems with this:

- Not all instances use the same privacy policy, so what exactly you did or didn't agree to when signing up to the Fediverse depends on your instance.
- Many instances don't ask for your consent to federate your posts! Dissemination of posts through federation can be justified through legitimate interests or performance of contract (other conditions from [Art. 6(1)](https://gdpr.eu/article-6-how-to-process-personal-data-legally/)) instead.[^1]
- Even if you had given consent for your data to be sent around the Fediverse, it is not at all obvious that this would also be transferable to BlueSky. [Recital 43](https://gdpr-info.eu/recitals/no-43/) of the GDPR seems to indicate that consent would not be considered "freely given" in this case because users are not allowed to give "separate consent" to "different personal data processing operations" even though it would be "appropriate".

[^1]: An argument for this is made in more detail (in German) in section 7.2 of the [legal analysis](https://stiftungdatenschutz.org/praxisthemen/datenschutz-bei-mastodon) ([pdf](https://stiftungdatenschutz.org/fileadmin/Redaktion/Dokumente/Mastodon-Leitfaden/Wissenschaftlicher_Aufsatz_Mastodon_Web-v1.pdf)) of data protection on Mastodon made by the German "Stiftung Datenschutz".

So, clearly, consent in the legal sense was not involved in the opt-out-based bridge system but we also have to be careful to remember that consent is not always necessary for lawful processing. In the case of the bridge, consent *is* an option though! By getting (proper) consent first, the bridge's processing would be lawful.[^2]

[^2]: This [appears to be the approach](https://snarfed.org/2024-02-15_52395) the bridge is taking now. For consent-based processing, there are some additional conditions that have to be respected, in particular the user's right to withdraw consent described in [Art. 7(3)](https://gdpr.eu/article-7-how-to-get-consent-to-collect-personal-data/).

### Legitimate interests

This leaves us with the second way the bridge's processing could be lawful: if the "processig is necessary for the purposes of the legitimate interests pursued by" the bridge ([Art. 6(1)f](https://gdpr.eu/article-6-how-to-process-personal-data-legally/)). But not just that – the "interests or fundamental rights and freedoms of the data subject" must not be of higher importance in such a case. So, a permission based on legitimate interests must always be weighed against the interests and rights of the person whose data is being processed.

"Legitimate interest" is a very broad term. "Legitimate" means something along the lines of compatible with the law, worthy of protection and objectively justifiable. An "interest" can be legal, economical or even purely ideational. So, a legitimate interest in the case of the bridge could be something like: providing the core functionality of the bridge; connecting the Fediverse and BlueSky (executing the fundamental idea of the application). So clearly, it is in the interest of the bridge to send personal data to BlueSky and this interest is not illegitimate.

Since users don't consent to processing based on legitimate interests, they must also be given the right to object according to [Art. 21(1)](https://gdpr.eu/article-21-right-to-object/). This was given by the system - the different ways to opt out were all realisations of the right to object (how well-designed these opt-out mechanisms were is a different question).

So, the bridge has a legitimate interest to process posts for the purposes of sharing them with BlueSky and allows users to object to this processing. It seems like this could be the condition that allows it to process data then? 

## Do the legitimate interests of the bridge justify an opt-out system?

In order to determine whether the legitimate interests allow processing as described in [Art. 6(1)f](https://gdpr.eu/article-6-how-to-process-personal-data-legally/), we ask the following questions:

1. Is the data processing *suitable* for the legitimate interests?
2. Is the data processing *necessary* for the legitimate interests?
3. Do the interests and rights of data subjects outweigh the legitimate interests?

We have basically already determined that yes, the data processing is suitable for the legitimate interests, since it fulfills the core idea of the bridge. We can answer the second question relatively quick as well: there is no alternative to meet the purpose of a bridge without processing posts, so it is indeed necessary for the legitimate interests.

Which brings us to the third and unfortunately most complicated question. In a real court case, you could get really specific here, refer to different rights of the controller and data subject, make arguments as to why one outweighs the other etc. I am not qualified to do this. In my view, the answer to the question is *yes*, the legitimate interest is outweighed by the users' rights and interests. Not because the interests are weak (for the bridge, they are existential), but because of the way a bridge operates in a social network.

[Recital 47](https://gdpr-info.eu/recitals/no-47/) can be read to support this (emphasis mine):

> The legitimate interests of a controller [...] may provide a legal basis for processing, provided that the interests or the fundamental rights and freedoms of the data subject are not overriding, taking into consideration the reasonable expectations of data subjects based on their relationship with the controller. Such legitimate interest could exist for example where there is a **relevant and appropriate relationship** between the data subject and the controller in situations such as where the data subject is a client or in the service of the controller. At any rate the existence of a legitimate interest would need careful assessment including whether a data subject **can reasonably expect** at the time and in the context of the collection of the personal data that **processing for that purpose may take place**. 

The relationship between an average user of the Fediverse and the bridge is nonexistent, strengthening the case that a user may not reasonably expect their posts to be processed by a bridge. As a user, you've made a post with the expectation that it is transmitted via the ActivityPub protocol. You can reasonably expect that any server that federates with yours receives the post, but can you reasonably expect that your post is shared across any other unspecified network? Put simply: is this "what you signed up for?"

Even if you think so, what really weakens the case for legitimate interest based permission, in my opinion, is the fact that it is completely feasible to implement a consent-based system that protects your interests just as well (minus "more users!") and that doesn't require you to weigh against users' fundamental rights.

All that said – legal professionals in this area will likely have some more detailed and/or different opinions. As usual with the law, it is not black and white.

## Other possible compliance issues

I have established now that I believe an opt-out bridge lacks a *legal basis* for lawful processing based on Article 6 of the GDPR. But while this "opt-out" problem was the main element of the discussion around this topic, it's not the only potential issue with the GDPR.

### Lack of transparency

If you have a right to object but you don't even know your data is being processed, something is clearly not right. That is why the GDPR obligates processors to adequately *inform* subjects. This is another angle from which the opt-out system could be attacked.

In this case, [Art. 14](https://gdpr.eu/article-14-personal-data-not-obtained-from-data-subject/) applies, because the bridge gets the data from your or its own instance, not from you directly. The information that must be provided to the user is quite extensive. In the case of a bridge, it would make the most sense to send the user a direct message the first time they get on the bridge's radar, informing them about their right to object (Art. 14(2)c), the legitimate interests for processing (Art. 14(2)b) and so on. There are exceptions to the obligation to provide this information (Art. 14(5)), but likely none of them apply here. 

To be fair, this obligation is also pretty difficult for Fediverse instances that get data from other instances to comply with, since spamming users with a message for every new instance is probably not the desirable outcome of all this. Nonetheless, the bridge in question had, as far as I can tell, made no efforts in the direction of providing its data subjects with the detailed information required by Art. 14, so it definitely fails this test.

### BlueSky is located in the USA

The GDPR is restrictive when it comes to data transfers outside of the European area. The reason is clear: the GDPR is one of the strongest data protection laws in the world, so being allowed to process European data *somewhere else* without restriction would create a loophole. Transfer to third countries is essentially only allowed if

- the European commission (the executive branch of the EU) decides that the data protection standards in a country are "adequate" ([Art. 45](https://gdpr.eu/article-45-adequacy-decision-personal-data-transfer/))
- "appropriate safeguards" are in place, in particular enforceable rights ([Art. 46](https://gdpr.eu/article-46-appropriate-safeguards-personal-data-transfers/))
- some other exception applies, such as explicit consent given, performance of contract, ... ([Art. 49](https://gdpr.eu/article-49-when-can-personal-data-be-transfered/))

The United States are a curious case in the list of third countries. Right after the GDPR was adopted in 2016, the commission issued an adequacy decision based on the "EU-US Privacy Shield", effectively allowing data transfers to the US. In 2020, this decision was deemed invalid by the European Court of Justice, reaching the conclusion that data privacy in the US, even with the supposed guarantees of the "privacy shield", was not sufficient.
In 2022, the commission tried again – this time, with regulation called "EU-US Data Privacy Framework". Around a year after this became law in the US through an executive order by Joe Biden, the commission issued [another adequacy decision](https://commission.europa.eu/law/law-topic/data-protection/international-dimension-data-protection/eu-us-data-transfers_en) in July 2023. 

Privacy activists remain unconvinced that this improved the situation, given that the US still doesn't have unifying federal privacy laws. It is expected that the [activist](https://en.wikipedia.org/wiki/Max_Schrems) who struck down the two prior EU-US agreements is going to attack this one as well.

So what does this mean for the bridge? Well, right now it means that it is allowed to send data to BlueSky, since the adequacy decision is in place. It is unclear, however, whether BlueSky is in full compliance with the privacy framework. And if the privacy framework were deemed to be inadequate by the court too, the bridge would likely have no legal basis for data transfer anymore. This is in contrast to, say, European Mastodon instances sending your posts to American Mastodon instances, because they can argue they are allowed to do so in order to fulfill their side of their service contract with you ([Art. 49(1)b](https://gdpr.eu/article-49-when-can-personal-data-be-transfered/)).


## Open questions

### Sensitive data

One thing I ignored in this post are "special categories" of personal data. Per [Art. 9(1)](https://gdpr.eu/article-9-processing-special-categories-of-personal-data-prohibited/), this includes things like medical data, data on sexual orientation or religion. These categories are generally banned from processing, even *on top of* the general processing ban, meaning that you need to meet additional, more restrictive conditions to process these types of data. This is of interest because posts on social media are "user-generated", meaning you, as a service provider, cannot decide upfront whether you will process special categories or not. As soon as someone posts "i'm gay", you're in Art. 9. Whether bridges can process such data anyway because people post it in public (Art. 9(2)(e)) is debatable. The aforementioned [article](https://write.as/ulrikehahn/bridging-to-bluesky-the-open-social-web-consent-and-gdpr) by Ulrike Hahn talks about this.

### Further obligations

Besides the already mentioned obligation to provide subjects with information, there are many other obligations for data processors. Most of these obligations work pretty much the same as for Fediverse instances that receive foreign data through federation. In particular, they must respond to users' requests to access the data stored on them and requests to delete that data. There are likely some subtle differences between regular Fediverse instances and a bridge to BlueSky, with regards to how such requests need to be handled. For example, if a user from a different instance asks your instance to delete one of their posts, you can expect them to delete it on their own first (so that the deletion propagates automatically).[^3] But with BlueSky this may not be justifiable anymore since you didn't intend to publish your post on BlueSky in the first place.

### Other processing principles

The last thing I would like to note before wrapping up is that there are many more requirements than just "lawfulness" for data processing, such as purpose limitation, data minimisation and storage limitation, all listed as basic principles in [Art. 5(1)](https://gdpr.eu/article-5-how-to-process-personal-data/). For a full analysis of the legality of the BlueSky bridge, it would therefore also be necessary to assess the way it is designed and implemented, how it stores its data, how secure it is, if it does anything *other* than bridging etc. I did not do that here and basically assumed that there is nothing unexpected or out of the ordinary regarding the other aspects of the system – i.e. it only relays posts and does so in a secure way while only collecting and storing what is absolutely necessary for a reasonable amount of time. The principles of data processing often play intertwined roles, so just making these assumptions is not a viable option for cases that require more rigor than answering "is this allowed in theory?". I just didn't want to leave you with the impression that only "lawfulness" matters. 

[^3]: See section 8.4 of the document mentioned in footnote 1.
