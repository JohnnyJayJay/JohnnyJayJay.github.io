{:title "Adopting Clojure in a Hostile Environment"

 :description "Introducing Clojure and exploring to which degree we can use it to improve broken Java APIs"

 :date "2021-01-25"
 :layout :post
 :tags ["clojure" "spigot" "java"]
 :klipse true
 :toc true}

If you've ever developed plugins for the Minecraft server software Spigot, you can probably come up with a list of questionable API behaviour and design choices made by Spigot fairly quickly.  Pain points that have made your experience with the API unpleasant or at the very least inconvenient at some points during development.

For example, here are a couple of things that come to my mind:

- API incompleteness - for many features you have to turn to the vanilla server classes, which are undocumented, obfuscated and generally a pain to deal with

- Compatibility issues - writing plugins that are compatible across the board is usually a pain despite the efforts of Spigot and its API version policy

- Lack of high-quality documentation

- Sometimes, things are just deprecated without any replacement (even though they still have viable and relevant use cases - e.g. `Bukkit.getOfflinePlayer(String)`)

- API inconsistencies. A few cases to illustrate:
  
  - Sometimes, "no item" is represented as `null`, sometimes as `ItemStack(Material.AIR)`
  
  - Some methods make and return defensive copies, some do not and others mix  both somehow and it's usually not clear why (`ItemStack#getItemMeta()`,  `InventoryClickEvent#getCurrentItem()`, `BlockDropItemEvent#getItems()`)
  
  - A high "event **priority**" will cause your event listener to run **later** than others

And I could go on, but I think you get the idea. 

Note that **I do not** blame Spigot for all of these issues  - a lot of it is, in some regards, unavoidable because it is inherited from the vanilla server and game (Spigot is a modification of that, after all).  

Still, Spigot is an undeniable mess. Its entire API consists of bloated, complicated and tightly coupled types and broken systems, which make it relatively hard to see through and reason about for pretty much anyone.

On the other hand, we have [Clojure](https://clojure.org/): a language that appears to work best in environments that are as functional, pure and simple as the language itself. At first glance, Spigot seems to be the polar opposite of those ideals.

So how do we bring them together? Let's find out.

## A Simple Language

This article is about Clojure, and yet most of the readers probably don't know the language or have never used it. That would be expected considering you rarely see it in the Minecraft development community, in contrast to, say, [Kotlin](https://kotlinlang.org). Why? Well, Kotlin is very close to Java conceptually and specifically designed to replace Java code without much revision.

Clojure on the other hand is *very* different from Java. I will introduce the basic syntax, ideas and concepts of Clojure just so we're on the same page (or at least closer) when talking about writing Bukkit plugins with Clojure. 

### Clojure's Characteristics

Clojure is defined by 3 main characteristics:

1. **It is a [Lisp](https://en.wikipedia.org/wiki/Lisp_(programming_language)).**

2. **It is functional.**

3. **It runs on existing platforms.**

#### Lisp

Being a dialect of Lisp entails a few things:

- Clojure is a **dynamic** language - there are no type checks at compile time like in Java and you can do meta-programming at runtime without reflection.

- Clojure is **homoiconic** - sounds complicated - what it means is that Clojure **code is itself represented as data structures** in the language. So where Java and Python interpret code as text or a stream of tokens, Clojure interprets code as lists, vectors and maps. This also means that its syntax is probably completely different from what you are used to.

- **Manipulating code** using other code is trivial in Clojure - this derives from the second point. Code is data and data is code, so writing macros for example is very similar to writing regular functions.

#### Functional Programming

Functional programming is a huge topic in itself and a thorough introduction to it is not within the scope of this article. Clojure mainly follows these ideas:

- There is atomic data - numbers, characters, strings - and there is composite data - lists, vectors, maps and sets containing other data. **All data is immutable in Clojure**, meaning you can't modify data after you've created it.

- **Functions** should be **pure** - functions take data, do computation and then return data again. Ideally, they don't care about the outside world. They don't manipulate global variables or state like object fields in Java.

- **Functions** are also **first-class**, meaning you can return functions from functions and pass functions to functions. 

You may notice how I wrote "should be" for function purity - that's because Clojure doesn't enforce this rule and the compiler will not complain at you for doing too much I/O, unlike in languages that are considered pure like Haskell. 

Technically, there is no one forcing you to use Clojure's data structures either - they are just regular objects on the platform Clojure runs on, so you could just as well pass around references to mutable Java objects for example. This will of course be important when writing plugins.

#### Platforms

Unlike some other Lisps, Clojure doesn't introduce an own runtime, but rather leverages existing and established platforms like the JVM, JavaScript or the Microsoft CLR. This is nice because it means it automatically benefits from improvements to the runtime without moving a finger and it can also use the ecosystem of the platform (e.g. use Java libraries). The drawback is that the runtime can't be tailored to the needs of the language - but most of the time this isn't a problem since platforms like the JVM are already pretty versatile.

### Clojure's Syntax

That was a lot of information. Let's do something practical and get to know Clojure's syntax in a small crash course.

#### Atomic Data

You've got everything you would expect from other languages: booleans, integers (by default `long`s), floats/doubles, chars, strings and null.

```klipse-clj
; This is a comment
(do 
  (println true false) ; boolean literals
  (println 42) ; integer/long literal
  (println 2.67324) ; double literal
  (println \x) ; char literal
  (println "Hello!") ; string literal (may also span multiple lines)
  (println nil)) ; nil is the word for "null" or "None" in Clojure
```

The `do` above wraps all the expressions contained in its body into a single one (sort of like statement blocks `{}` in Java and similar). You wouldn't need this if you put the code in a file and ran it like that, but the evaluator on this website can only eval one expression at a time, so that's why it's there.

Also, you may be wondering why you see a second `nil` at the end of the output. This is because every expression returns a value, and `do` returns the value of the last expression in its body. Functions that do not typically return anything, like `println`, return `nil` in Clojure and that's what you see at the end.  Put a number or a string behind `(println nil)` and check out the difference!

Additionally, Clojure has a data type for identifiers like `println` called "symbol". Symbols are used to reference a value somewhere else (like a function in the case of `println`) and are resolved to that value when evaluated. If you actually want a `Symbol` and not whatever the symbol would be resolved to, you can quote `'` it to tell the language "do not evaluate this, treat it as-is". You'll see another use for this shortly.

```klipse-clj
; This is a local variable "declaration", binding the symbol num to the value 5 to be accessible in the scope of let
(let [num 5] 
  (println "I resolve to my value:" num)
  (println "I don't:" 'other-num) ; See what happens if you remove the quote ' here
  (println "I am a dynamically created symbol:" (symbol "foo")))
```

Related to symbols, there is also one other unusual type called "keyword". Keywords are essentially symbols that evaluate to themselves. This results in there being only one keyword instance for each unique keyword in your program. They are often used as keys in maps or as enums.

```klipse-clj
(println :foo :bar :baz) ; Keyword literals
```

There are also a couple of types/literals that exist on the JVM but not in Clojure's JavaScript implementation ClojureScript (which is running on this page):

```clojure
; number literals automatically become BigInt/BigDecimal when they're too large for regular primitive numbers
327487328432674632864324323264712321123124143321523
; Ratios for full-precision rational numbers are a thing
1/3
```

#### Composite Data

So far, so good. Then, there are the composite data types I mentioned earlier - they can contain any other types of data.

##### Vectors

```klipse-clj
[1 true 35.5 \A]
```

The structure above is a **vector**, recognisable by its square brackets. It's basically the equivalent of an `ArrayList` in Java. It has fast index-based access, you "add" elements at the end and it is generally your default choice for fixed sequential data.

You may have noticed that the entries of the vector above are not separated by commas. This is because commas are whitespace in Clojure and are typically only used if it becomes hard to read otherwise, like key-value pairs in maps. Try adding commas to the vector and you'll see that it doesn't make a semantic difference.

##### Maps

```klipse-clj
{:key "value",
 "foo" 65,
  true false}
```

Clojure's map is the data structure you will probably deal with the most, since it's used where you would typically use classes in Java. Take this example:

```java
// Person.java
public class Person {
    private final String firstName, lastName;
    private final Date dateOfBirth;
    private final List<Address> addresses;

    // Constructors, getters, ...
}

// Address.java
public class Address {
    private final String streetName;
    private final String houseNumber;
    private final int postalCode;

    // Constructors, getters, ...
}
```

This is pretty standard Java code for business logic and data modelling.

In Clojure, you do all of this without any user-defined types, in this case just using maps to represent data objects and vectors for lists.

```klipse-clj
{:first-name     "Eileen"
 :last-name      "Lowery"
 :date-of-birth  #inst "1967-09-25"
 :addresses      [{:street-name "Main Street"
                   :house-number "87A"
                   :postal-code 12345}
                  {:street-name "Brownton Road"
                   :house-number "123"
                   :postal-code 54321}]}
```

Using only a few data structures to do most of your operations has many advantages: you can now use all existing map operations and transformations on your "own" types, for example.

Note that by default, those maps are a type of hash map, so the order of entries is not defined.

Another note: see the `#inst` there? That is called a reader macro and will expand to  code that creates a new `Date` object during initialisation. Generally, `#` means reader macro and you can even define your own.

##### Sets

There is not that much to say about sets - they work pretty much like maps, just that they map keys to themselves.

```klipse-clj
#{"I am a set" 434 \u2705 :another-entry}
```

You can call `get` and `contains?` on maps and sets alike for key-based lookup:

```klipse-clj
(do 
  ; This is a top-level/global variable definition
  (def colors #{:red :green :blue}) 
  (def me {:name "Johnny", :occupation "Student", :age 123.4})

  (println (contains? colors :red))
  (println (get colors :purple :not-found)) ; get with default value (try omitting it) 
  (println (colors :red :not-found)) ; this behaves exactly like get - you can treat sets like functions
  (println (:red colors)) ; you can even use keywords as functions - also works like get
  (println)
  ; same things work on maps
  (println 
   (contains? me :age)  
   (get me :happiness ":(") 
   (me :occupation) 
   (:name me))) 
```

Interestingly, all of the above functions also work on vectors. These data structures can all be seen as "associative types": maps associate arbitrary keys with arbitrary values, sets associate arbitrary keys with themselves and vectors associate indices with arbitrary values.

##### Lists

Even though you rarely use them to actually store data, lists still play one of the most, if not *the* most important role in Clojure. You denote list literals using parentheses:

```klipse-clj
'(1 "hello" true)
```

But hold on, why are we quoting `'` this list here? If you remove this quote, you will notice an error telling you that you can't call `1` as a function. 

That is because **lists, by default, act like operators for calling functions**. You've seen this plenty of times with `println` now: When we call that function, we're doing nothing more than putting it in a list together with its arguments.

Remember the term I introduced earlier? This is "Homoiconicity". The code `(println "Hello, World")` is, technically speaking, a list of the symbol `println` and the string `"Hello, World"` and it gets interpreted as such.  Only when we *evaluate* the data is `println` resolved to a function and called with the argument `"Hello, World"`. Our code is just a list!

**You now know pretty much all of Clojure.** 

No, seriously. The data primitives and composites are the entirety of the language. There are no baked-in operators or keywords like in other languages, only a handful of reader macros and "special forms" like `do`, `let` or `def`. But even those do not introduce new syntax; they are also just data, first and foremost.

## An Easy Language?

Okay, all right, perhaps saying you now know all of Clojure is a bit of an exaggeration. "You're now able to read Clojure" would probably be more accurate. After all, there is more to a language than just syntax. 

But I do think you don't need much more to get an impression of what Clojure is all about: simplicity. There is minimal syntax with a minimal amount of exceptions to the rules and the core ideas are not complex at all. Does that make it necessarily *easy*? No, because ease and simplicity are two distinct concepts, as Rich Hickey, the creator of Clojure, explains in [this great talk](https://www.youtube.com/watch?v=oytL881p-nQ). 

I will try to explain as much as possible going forward, but please don't feel discouraged if you don't understand everything immediately. 

Nevertheless, here are already some resources for further reading (and watching) about Clojure - I will link them at the end of the article again as well.

- [Clojure for Java Programmers Part 1 - Rich Hickey - YouTube](https://www.youtube.com/watch?v=P76Vbsk_3J0)

- [Clojure - Learn Clojure](https://clojure.org/guides/learn/syntax)

- [Clojure - About Clojure](https://clojure.org/about/rationale)

- [Clojure beginner resources](https://gist.github.com/yogthos/be323be0361c589570a6da4ccc85f58f)

## The Goal

The goal for me when I first tried adopting Clojure in Spigot plugins was to test the waters, make something small and simple, yet something that involves some interesting design decisions. I settled with a [Connect Four](https://en.wikipedia.org/wiki/Connect_Four) plugin that allows players to play the little game against each other in an inventory. 

Here are a few reasons why this plugin could be seen as a some sort of proof of concept:

- The basics of Bukkit programming are required - commands, listeners, inventories, items, player handling

- More complex systems like a game loop need to be implemented (and this is an interesting task in Java already)

- There is some part external to Bukkit (in this case the game logic)

## Where to Start

As with all rather unusual experiments, "Where should I start?" is indeed a good question to ask yourself. In my case, I decided to look around if I could find someone who already integrated Clojure and Spigot once, and I did.

There were a couple of forum posts asking Clojure-related questions on SpigotMC (though usually without any response, see [here](https://www.spigotmc.org/threads/java-interop-with-clojure.234145/) and [here](https://www.spigotmc.org/threads/registering-a-custom-pluginloader.449746/)). 

There was also a small, now rather outdated resource called [Bukkit4Clojure](https://github.com/Proximyst/Bukkit4Clojure) that enables you to integrate the two with some very primitive glue code. This wasn't what I was looking for as I wanted to try something myself, but it did already raise my awareness considering certain complications that I'll get to later. It also made me read some of the [forum posts about this resource](https://www.spigotmc.org/threads/bukkit-for-clojure.235929) on SpigotMC (which was not productive, but a bit funny):

> >  insert hate towards clojure here
> 
> thx for permission.  
> 
> clojure looks like shit.

And finally, I even found a [blog post](http://rycole.com/2013/01/27/clojure-minecraft-bukkit-plugins.html) covering this very topic. The post is now almost 8 years old though and it doesn't go into many details or show a "real-world" plugin.

This was all very helpful nonetheless, because it also gave me an impression of the use of Clojure in Minecraft servers: it's practically non-existent.

Thus, I decided to dive right in and try to create something that would run, first of all. This turned out to be non-trivial, however.

## Writing Plugins in Clojure

I used [Leiningen]([Leiningen](https://leiningen.org)) to set up my project, since it's a very easy build tool that doesn't require much configuration. Following my experiments, I published a [template](https://github.com/JohnnyJayJay/spigot-clj-template) that does most of the setup for you (you can use it by running `lein new spigot-clj your-project-name`). 

I continued by adding the repositories

```clojure
:repositories [["spigotmc" "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"]
               ["snapshots" "https://oss.sonatype.org/content/repositories/snapshots"]]
```

and the dependency

```clojure
:profiles {:provided {:dependencies [[org.spigotmc/spigot-api "1.16.1-R0.1-SNAPSHOT" :scope "runtime"]]}}
```

to the Leiningen `project.clj` file. Regular dependencies (like Clojure itself) are added to the regular `:dependencies` key, but to exclude Spigot from the jar shading process later, I added it to the `provided` profile.

And finally, since I wanted to export it as a jar, I needed to compile everything ahead of time, which can be anabled by  setting `:aot :all` in the same file.

The next step was to create a namespace that would act as the "plugin main", as Spigot calls it. You usually don't create classes in Clojure, but if you need to (e.g. for interop) you can easily instruct a namespace to be compiled to a specified class.

In case you didn't figure it out already, namespaces are the way programs are structured and definitions are grouped in Clojure. 

```clojure
; This is the name of the namespace as used from Clojure
(ns com.github.johnnyjayjay.connect4.plugin 
  ; Tells the compiler to generate a complete class file during compilation
  (:gen-class
    ; This is the fully-qualified class name of the class this namespace will compile to
    :name com.github.johnnyjayjay.connect4.ConnectFourPlugin
    ; The name of the class the namespace class should extend, ... 
    :extends org.bukkit.plugin.java.JavaPlugin))
  ; ... imported here again for use in code
  (:import (org.bukkit.plugin.java JavaPlugin)))

; Defines a function called -onEnable taking one argument "this" and 
; calling this.getLogger().info("Clojure Plugin connect-four enabled") when called
(defn -onEnable [^JavaPlugin this]
  (.. this (getLogger) (info "Clojure Plugin connect-four enabled!")))
```

Whew, that may seem like a lot of new things all of a sudden. Let's quickly go through them:

- `(defn <name> [<params>] <body>)` defines a function. It's actually just a macro that expands to `(def <name> (fn [<params>] <body>))` (`fn` being the special form to create functions)

- You probably already know `onEnable`, it's the method that is called when the plugin is enabled. But what's the deal with the hyphen in front? This tells the compiler to see this as an instance method of the class that will be generated from the namespace.

- And what the hell is `^JavaPlugin`?? You didn't tell me about ths syntax?! This is so-called "meta" (metadata) for the symbol `this`, in this specific case it's a type hint that will tell the compiler the type the parameter should have. Note that unlike in Java, we need to specify the current object (`this`) as a parameter.

With all of this combined, this namespace (which resides in the `com/github/johnnyjayjay/connect4/plugin.clj` file by the way) will compile to something like this (this is just the equivalent in Java, it won't actually compile to Java source code of course):

```java
package com.github.johnnyjayjay.connect4;

public class Connect4Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("Clojure Plugin connect-four enabled!");
    }
}
```

Ok, seems like it's ready to test, right? Just calling `lein uberjar` to create a jar with dependencies, deploying the plugin on the server, restarting, and... 

Uh oh.

### Class Loading Complications

If you tried this yourself, you just ran into a `ClassNotFoundException` of some sort, telling you that some Clojure class couldn't be found. But how can this happen when we put the entirety of Clojure in our plugin jar file?

Well, the answer is simple (kinda): Clojure's and Spigot's class loading systems clash. 

You see, class loading in Java *usually* works like this: when a class `A` references another class `B`, `A` asks its own `ClassLoader` to look up `B`. The class loader will then first delegate the request to its parent class loaders, and finally, if none of them know the class already, `A`'s class loader will load and initialise `B`.

Following up on that, Spigot loads your plugin main using a custom class loader called [`PluginClassLoader`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/PluginClassLoader.java) that **deviates** from this pattern - it doesn't have a a proper parent class loader. Instead, when asked to find a class, it will first perform a lookup on an internal `Class` instance cache, then ask its corresponding [`JavaPluginLoader`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/JavaPluginLoader.java) (which, confusingly, isn't a class loader!) to find the class (which will typically result in the object looking through all plugins loaded so far) and then, if it still isn't found, it will attempt to load the class from the plugin jar. 

One consequence of this is that Spigot doesn't really use normal class loaders, and therefore those normal class loaders do not know about the plugin classes. Which brings us to Clojure. Clojure uses the **context class loader of the current thread** for a lot of its operations. This is a problem though, because this context class loader is not a `PluginClassLoader` by default and thus, boom. Classes located in our plugin jar not found.

Fortunately, there is a simple fix for this: Just set the context class loader to the plugin class loader right before loading all the Clojure stuff. This may not be the best way of going about it, considering we're setting the context class loader for the entire Spigot thread and perhaps affect other plugins by doing so, but let's not care until it becomes an issue...

So, what does the simple fix look like? Something like this:

```java
public abstract class ClojurePlugin extends JavaPlugin {

  static {
    Thread.currentThread().setContextClassLoader(ClojurePlugin.class.getClassLoader());
  }

}
```

In fact, this class will be generated automatically if you use `lein new spigot-clj ...`.  As you can see, it really doesn't do a lot - its entire purpose is to be extended by the plugin main. The trick is: before the plugin main is loaded, this class will be loaded and the static initialiser will be run, so that when the plugin main is *actually* loaded, Clojure will find its classes through the `PluginClassLoader`. That's why we have to do it in Java - there's no way we're writing code in Clojure that doesn't depend on Clojure.

So we'll just modify our namespace declaration from earlier to look like this:

```clojure
(ns com.github.johnnyjayjay.connect4.plugin
  (:gen-class
    :name com.github.johnnyjayjay.connect4.ConnectFourPlugin
    :extends com.github.johnnyjayjay.connect4.ClojurePlugin)
  (:import (org.bukkit.plugin.java JavaPlugin)))
```

And we're done! We now see `Clojure Plugin connect-four enabled` popping up in the console on startup.

### Interop Issues

Now that we've gotten that out of the way, let's take a moment to talk about interop, in particular typing and write some code for inventory handling along the way.

#### Type Hints and Reflective Access

Earlier, I showed you this piece of code: `^JavaPlugin this` - and I also mentioned that the `^JavaPlugin` essentially associates the type `JavaPlugin` with the symbol `this` as a "hint". So why exactly did I do this? Was it necessary?

Short answer, no.

For the long answer, we'll have to take a closer look at how Clojure handles direct Java interop. 

Let's create a namespace `inventory` that will contain code to create and manipulate Minecraft `Inventory`s and `ItemStack`s. 

```clojure
;; File inventory.clj
(ns com.github.johnnyjayjay.connect4.inventory
  ; Here are Java imports again - lists of symbols, where the first in each list describes the package and the rest the classes
  (:import (org.bukkit.inventory ItemStack ItemFlag Inventory)
           (org.bukkit Material Bukkit)
           (org.bukkit.enchantments Enchantment)))
```

Let's think about how we'd ideally work with `ItemStack`s from Clojure.

`ItemStack` is primarily a class that carries data about an item, such as its type, amount and durability as well as some metadata that Spigot encapsulates in `ItemMeta`. As established earlier, we use maps to represent structured data like this. This is what an `ItemStack` map might look like:

```clojure
{:type       Material/STAINED_GLASS_PANE ; "Class/member" is interop notation for static member access (such as Material.STAINED_GLASS_PANE)
 :durability 8
 :meta       {:display-name "Drop a disc"
              :lore         ["Click to drop a disc here"]}}
```

Note that **this is still a map**, it doesn't magically transform into an actual `ItemStack` object. This is just how it would be modelled if we translated it directly to Clojure.

Now, this is a nice representation. Why? Because we can "manipulate" `ItemStack`s using only map operations. We're completely independent from Spigot's API (except the enum constant, but that doesn't really bother us because it doesn't compromise immutability). 

Let's write a function that "adds glow" to any item (add an enchantment and hide it):

```clojure
(defn add-glow [item]
  (-> item
      ; Silk touch is just a random pick that won't have an effect in our case
      (assoc-in [:meta :enchants Enchantment/SILK_TOUCH] (int 1))
      (update-in [:meta :flags] conj ItemFlag/HIDE_ENCHANTS)))
```

Again, this does not manipulate an `ItemStack` object, it takes a map representation like the one above and returns a new map with the additions. Let's go over this quickly:

- `->` is the "thread first" macro - you can see it as an operator for chaining operations. It's easy to understand, because it looks more like something you'd see in other code. Take this item, **then** add an enchantment, **then** add an item flag. `(-> x (foo 4) (bar "hello" "world") (baz))` expands to `(baz (bar (foo x 4) "hello" "world"))` for example.
- `assoc-in` associates a key with a value in a nested sub-structure of the given map - the vector given as an argument describes the "path" to it.

```klipse-clj
(let [my-map {:nested {:map {:key 3}}, :other [4 3 2]}]
  ; Regular assoc is used to override or add a key at the top level
  (println (assoc my-map :other true))
  ; assoc-in "traverses" the given keys and creates maps on the way if needed
  (println (assoc-in my-map [:nested :map :key] 7)))
```

- `update-in` updates a value by applying a function to it, the `in` meaning "nested" again.

```klipse-clj
(let [my-map {:nested {:map {:key 3}}, :other [4 3 2]}] 
  ; updates the value at :other with (conj v 1) (v being the value). conj returns a new vector with the given element appended
  (println (update my-map :other conj 1))
  ; updates the nested value by calling inc on it (increment by one)
  (println (update-in my-map [:nested :map :key] inc)))
```

Note that we do not mutate the maps here. `assoc`, `update` etc. return new maps.

Finally, you might ask yourself why we associate the enchantment with `(int 1)`. The idea is to end up with a `Map<Enchantment, Integer> ` that we can pass to [`addUnsafeEnchantments`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html#addUnsafeEnchantments(java.util.Map)). We need to coerce the `1` to `int` because as stated earlier, Clojure uses `long` for integer numbers by default and that doesn't work with Spigot.

Spigot doesn't know how to handle the data model we just made up, of course. It expects us to pass around `ItemStack`s and similar. What we need is an `item-stack` function that takes a specification map like those described above and turns it into an actual `ItemStack` object. This is what it looks like:

```clojure
(defn item-stack
  ; Docstring
  "Creates an `ItemStack` from the given specification map."
  ; Parameters using map destructuring 
  [{{:keys [display-name lore flags enchants] :as meta} :meta
    :keys [type durability amount]}]
  (cond-doto (ItemStack. ^Material type)
             meta (.setItemMeta (cond-doto (.getItemMeta (ItemStack. ^Material type))
                                           display-name (.setDisplayName display-name)
                                           lore (.setLore lore)
                                           flags (.addItemFlags (into-array ItemFlag flags))))
             amount (.setAmount (int amount))
             durability (.setDurability (short durability))
             enchants (.addUnsafeEnchantments enchants)))
```

This function has a many interesting things again. Most notably:

- Map destructuring in the parameters - binding symbols to individual values from the map

- `cond-doto` - a macro I wrote myself for this purpose - I will say more about this in a second

- More Java interop - `(.bar foo baz)` <=> `foo.bar(baz)` and `(Foo. bar)` <=> `new Foo(bar)`

`cond-doto` takes a `value` and then a bunch of `test` `action` pairs. It will expand to code that goes through each `test`, one by one. If a test evaluates to true, the code calls the corresponding action with the value as its first argument. To make it more clear, here's the equivalent in Java:

```java
ItemStack item = new ItemStack(type);
if (meta != null) {
    ItemMeta itemMeta = item.getItemMeta();
    if (displayName != null) {
        itemMeta.setDisplayName(displayName);
    }
    if (lore != null) {
        itemMeta.setLore(lore);
    }
    // and so on...
    item.setItemMeta(itemMeta);
}
if (amount != null) {
    item.setAmount(amount);
} 
// and so on...
```

You may notice that in the test expressions in Clojure, we do not do `(not= value nil)` or `(some? value)` (which would be the same as `value != null`), but rather use `value` directly. This is because in Clojure, `nil` and `false` can be treated as false (said to be "falsey") and everything else can be treated as true (said to be "truthy").

The main thing I'd like to highlight is something we've already seen elsewhere: type hints like in `(ItemStack. ^Material type)`. Perhaps you're wondering why we do this, considering we're in a dynamically typed language. And that's a good question - if you removed the hints, it would still work in the cases so far.

Here are some reasons why you might see a type hint in Clojure:

- To differentiate between different Java method overloads - Clojure might not be able to figure out the method to call if there are two method overloads with the same amount of parameters.

- To avoid reflection - this is the main reason. When Clojure can't infer the types (which is often the case, since its type inference is very basic), it will resort to reflective access. That's something you usually want to avoid, because it's slow and possibly volatile.

Type hints aren't always as straight-forward as shown in these examples, however. Take this code, also from the `inventory` namespace:

```clojure
(defn highlight-win-line!
  [{:keys [win-discs]} ^"[Ljava.lang.Object;" contents winner win-line]
  (let [glowing-disc (win-discs winner)]
    (run! #(aset contents (position->slot (:position %)) glowing-disc) win-line)
    contents))
```

The purpose of this function is to replace the items in the winning Connect 4 line with glowing items of the same kind, so the 4-disc-line that prompted the win is highlighted in the inventory. 

It takes 4 arguments: 

1. The game data map containing the glowing win disc items associated with each player (`ItemStack`s are "compiled" once before the game and then reused) - so `win-discs` will be a map of player name -> `ItemStack`, where the `ItemStack` represents their respective disc item (red or yellow dye) with a glow.

2. The inventory contents array. 

3. The name of the player who won the game

4. A sequence of `[x y]` positions forming a win line

You can see some weird metadata here: `^"[Ljava.lang.Object;"`. What is this? Well, `Ljava.lang.Object;` is the formal type descriptor for the class `java.lang.Object` and `[` means it's an array. So this string is the type descriptor of `Object[]`. We need to do this here because `aset` modifies an array and will use reflection if it can't determine the type. Unfortunately, Clojure doesn't have a pretty way to type hint arrays - with the exception of primitive arrays, which are denoted by `ints`, `longs` and so on. 

I recommend adding this to your `project.clj` when you want to type hint your interop:

```clojure
:global-vars {*warn-on-reflection* true}
```

This will emit warnings where your code compiles to reflection.

#### Methods and Functions

Another issue I often found myself facing is that Java methods can't be treated as Clojure functions. For example, this does not work:

```clojure
(map Integer/parseInt ["123" "4" "937" "-5"])
```

Instead, you have to do this:

```clojure
(map #(Integer/parseInt %) ["123" "4" "937" "-5"])
```

Remember, the `#` sign indicates a reader macro - this one is just a shorthand for creating functions, i.e. it is equivalent to `(fn [arg] (Integer/parseInt arg))` (`%` is the first parameter). Since in order to use Spigot, you need to call a lot of Java methods, this was really annoying to me - especially because you also need to add type hints everywhere. Also, Java method naming conventions don't match the Clojure ones (`lower-kebap-case`).

Fortunately, Clojure comes with a macro that allows you to "wrap" Java methods in Clojure functions, and I used that quite a lot. Here are some examples:

```clojure
(def send-message (memfn ^CommandSender sendMessage ^String message))
(def open-inventory (memfn ^HumanEntity openInventory ^Inventory inventory))
(def set-contents (memfn ^Inventory setContents ^"[Lorg.bukkit.inventory.ItemStack;" items))
```

The first `memfn` parameter is the method name, optionally type hinted with the type where it's declared in. The other parameters are the parameters of a method, which can also be type hinted like here.

For a bigger plugin written in Clojure or a proper library that seeks to wrap entire Spigot APIs, it may be a better approach to translate Spigot objects to Clojure data structures and back like I did in the `inventory` namespace. But in this case, I figured this wasn't worth the effort.

### Asynchronous Adventures

One big motivator of Clojure's design is concurrency. Functional programming is not just a gimmick - it simplifies concurrent and parallel programming by orders of magnitude. 

For the most part, Spigot is single-threaded - most APIs within Spigot require you to use the server thread. That doesn't mean we cannot leverage Clojure's excellent concurrency support, though.

#### State

Clojure has a couple of built-in concurrency primitives. You've seen one of those many times now already: `Var`. You declare `Var`s with `def`. They are essentially thread-local bindings, meaning that each thread has its own view of a `Var`. Here's an overview of the 4 most important concurrency primitives in Clojure:

| Primitive | Synchronised | Coordinated | Thread-Local |
| --------- | ------------ | ----------- | ------------ |
| `Var`     | ❌            | ❌           | ✅            |
| `Atom`    | ✅            | ❌           | ❌            |
| `Ref`     | ✅            | ✅           | ❌            |
| `Agent`   | ❌            | ❌           | ❌            |

All of these are used to encapsulate state and sometimes called "reference types" because they are essentially different kinds of references to immutable values. 

Synchronised means that changing the value of the container will have immediate effect, coordinated means that changes will happen in a defined order (e.g. when two different threads try to change the value of a container at the same time). All of those types have their uses, but what you will encounter most commonly besides `Var`s are `Atom`s. If you're familiar with Java's `AtomicReference` - great. Because `Atom`s are very similar to those.

In the connect four plugin, I use an atom to store a map of players who have been invited to play a game by another player. The system is very simple: one player types `/connect4 play <name>`, and the player associated with that name will be put in the requests map. When they in turn type `/connect4 accept`, their opponent will be looked up from the map.

```clojure
(ns com.github.johnnyjayjay.connect4.command
  (:gen-class
    :name com.github.johnnyjayjay.connect4.Connect4Command
    :implements [org.bukkit.command.CommandExecutor])
  (:require [clojure.core.match :refer [match]])
  (:import (org.bukkit Bukkit)
           (java.util UUID))

; Defining an atom - defonce is just like regular def, 
; but it only binds a value if there isn't one already 
; (useful when programming at the REPL, reloading namespaces and so on but not a requirement here)
(defonce requests (atom {}))

; some other stuff...

(defn -onCommand [this ^CommandSender sender command label args]
  (if (instance? Player sender)
    ; match is a macro that comes from core.match, a pattern matching library
    (match (vec args)
      ["play" ^String name]
      ; if-let is like an if that checks whether the binding (in this case opponent) is nil
      (if-let [opponent (Bukkit/getPlayer name)]
        (do
          (swap! requests assoc (player->uuid opponent) (player->uuid sender))
          (send-message opponent (str "§6" (player->name sender) "§a invited you to play connect four. Type §6/connect4 accept§a to accept."))
          (send-message sender (str "§aA request to play has been sent to §6" name "§a.")))
        (send-message sender (str "§cThe player §6" name "§c is not online.")))

      ["accept"]
      (if-let [^UUID opponent-id (@requests (player->uuid sender))]
        (if-let [opponent (Bukkit/getPlayer opponent-id)]
          (start-game [opponent sender])
          (send-message sender "§cThe player who sent you the request is not online anymore."))
        (send-message sender "§cThere is no pending request to accept."))

      :else
      (send-message sender "§cPlease use §a/connect4 play [player]§c to send a play request to another player or §a/connect4 accept§c to accept a pending request."))
    (send-message sender "§cThis command is only executable by players."))
  true)
```

You can see two very common atom operations in this code:

- `swap!` applies a function to the old value and replaces it with whatever that function returns. Since the `assoc` function (used to put new key-value pairs in a map) returns a new map, we can use it with `swap!`.

- `@requests` (shorthand for `(deref requests)`)  returns the current value contained in the atom.

This is how you often handle state in Clojure - rather than using mutable values, you use containers of immutable values that you can replace with new values. This ensures that "nothing changes from under your feet" and therefore preserves the benefits of immutability while allowing you to have state (something that indeed every program needs at some point, even in functional programming).

#### Event Handling

Instead of using Spigot's listener system (which is possible in Clojure), I decided to try something different using a library called `core.async`. This library adds a new concurrency primitive called `Channel` that allows you to do lots of fancy stuff. For those familiar with Go, the library is heavily inspired by its concurrency model and implements the same kind of channel. I won't go too much into detail, but this idea of channels comes from a paper called "Communicating Sequential Processes" (or CSP for short) by the famous computer scientist Tony Hoare. You can watch [Rich Hickey talk about core.async](https://www.youtube.com/watch?v=drmNlZVkUeE) or [learn more about CSP](https://en.wikipedia.org/wiki/Communicating_sequential_processes), if you like.

I didn't want a traditional listener system because it gives you very little freedom. For example, I often want to "wait" for events. Or "filter" out certain events. Or perhaps wait for "one or the other", whichever comes in first. And all of that just in a specific time frame, sometimes. Usually, to achieve something like this, you need to write some sort of state machine that moves between states depending on the events that come in. This can produce headaches if your design is rather complex - and it's hard to integrate with your sequential command code. 

So what did I do? I leveraged Spigot's `EventExecutor` API to put incoming events on `Channel`s, which I can then take from, wait for etc. from my sequential code. This is particularly nice for a game loop where we want to wait for the next move.

```clojure
(def listener-stub (reify Listener))

(defn event-executor
  "Returns an `EventExecutor` that puts events of the given type on the given channel.
  Unregisters the listener in question when the channel is closed."
  [event-type channel]
  (reify EventExecutor
    (execute [this listener event]
      (when (= (type event) event-type)
        (when-not (put! channel event)
          (HandlerList/unregisterAll listener))))))

(defn pipe-events!
  "Registers a Bukkit listener that puts all events of the given type on the given core.async channel."
  [plugin type channel
   & {:keys [priority ignore-cancelled?]
      :or {priority (EventPriority/NORMAL) ignore-cancelled? false}}]
  (.registerEvent
    (Bukkit/getPluginManager)
    type
    listener-stub
    priority
    (event-executor type channel)
    plugin
    ignore-cancelled?))
```

And this is all the code needed to enable it. When you now create a channel and call `(pipe-events! plugin AsyncPlayerChatEvent channel)`, all `AsyncPlayerChatEvent`s will be sent to that channel and you can filter, map, take and do all kinds of things to this "stream of events".  Again, explaining all of this would totally blow the scope of this post (it's too long already anyway), so I again recommend watching Rich Hickey's talk about this, if you're interested. 

A lot of the nice channel operations can only be done from a so-called `go` block, which puts its body on a specific thread pool. If you know any languages that have coroutine support (e.g. Python or Kotlin), this is very similar. Instead of blocking, many channel operations in `go` blocks "park" threads so they can be used for other purposes while you're "waiting" for something. However, this also means that code within a `go` block does not run on the server thread, and thus you can't invoke many Spigot APIs. To circumvent this, I just wrote a macro that runs an action on the server thread using the `BukkitScheduler`. It also uses `core.async`, so you can "wait" for synchronous operations from asynchronous contexts.

```clojure
(def run-task (memfn ^BukkitScheduler runTask ^Plugin plugin ^Runnable runnable))

(defmacro runsync
  "Executes the given body on the server thread and returns a channel that receives the result when it's done."
  [plugin & body]
  `(let [result-channel# (chan)]
     (run-task
       (Bukkit/getScheduler)
       ~plugin
       (fn []
         (if-let [result# (do ~@body)]
           (put! result-channel# result#)
           (close! result-channel#))))
     result-channel#))
```

I just wanted to show this so you can see what macro definitions look like. They're a rather advanced topic in the language, so I won't explain this here either - if you want to read more, see [Writing Macros | Clojure for the Brave and True](https://www.braveclojure.com/writing-macros/) (this book is a general recommendation if you want to learn Clojure).

## Conclusion

There is a lot more I could say about this plugin and how it was written - but I need to wrap up *somewhere* if I expect anyone to read this entire post.

Fiddling with Clojure and Spigot was a lot of fun and a great learning experience for making compromises. I think the use of Clojure made this implementation of connect four simpler, and most certainly shorter (just around 700 Lines of Code - I would be very willing to bet that a well-designed Java implementation would turn out to be much bigger). 

I do not think that, as it stands, Clojure is the best choice for all future plugins. Languages that specifically try to improve Java while maintaining its core ideas and feature set - like Kotlin - are much more straight-forward to work with in conjunction with Spigot. However, I would definitely be interested in a more elaborate wrapper for Spigot written in Clojure and this will not be my last time writing a plugin in this language.

As promised, here are the resources from earlier again:

- [Clojure for Java Programmers Part 1 - Rich Hickey - YouTube](https://www.youtube.com/watch?v=P76Vbsk_3J0)

- [Clojure - Learn Clojure](https://clojure.org/guides/learn/syntax)

- [Clojure - About Clojure](https://clojure.org/about/rationale)

- [Clojure beginner resources](https://gist.github.com/yogthos/be323be0361c589570a6da4ccc85f58f)

Want to try the connect four plugin? Get the standalone version from [Releases · WeLoveOpenSourcePlugins/connect-four · GitHub](https://github.com/WeLoveOpenSourcePlugins/connect-four/releases).
