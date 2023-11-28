{:title "Software Transactional Memory: Clojure vs. Haskell",
 :description
 "\"To have your cake and eat it too\" is really just a literary race condition",
 :date "2023-11-28",
 :layout :post,
 :tags ["clojure" "haskell" "concurrency"],
 :toc true,
 :comments
 {:instance "chaos.social", :author "johnny", :id "111489016722517856"}}

A few days ago, I saw a [post](https://toot.cat/@plexus/111447816873237415) from Arne Brasseur sharing a concurrency problem and asking people how to solve it in Clojure (given an erroneous solution as the basis). 

<iframe src="https://toot.cat/@plexus/111447816873237415/embed" class="mastodon-embed" style="max-width: 100%; border: 0" width="550" allowfullscreen="allowfullscreen"></iframe><script src="https://toot.cat/embed.js" async="async"></script>

I [fixed](https://gist.github.com/JohnnyJayJay/69ef9c9f0092ad3a1d5082f0b71e66c7) the code using Clojure's software transactional memory (STM) model, which I think is a perfect fit for this sort of task. Since I'm currently picking up a bit of Haskell, I was curious to see what concurrency features Haskell people might use to solve this. To my delight, I found that Haskell also has an STM implementation, so I figured it might be interesting to compare it to Clojure. If you're unfamiliar with STM, don't worry, I'll go over the idea before talking about the concrete solutions. 

## Pie à la mode

An excerpt from *The Pragmatic Programmer*, where this is from, reads:

> You’re in your favorite diner. You finish your main course, and ask your server if there’s any apple pie left. He looks over his shoulder, sees one piece in the display case, and says yes. You order it and sigh contentedly. Meanwhile, on the other side of the restaurant, another customer asks their server the same question. She also looks, confirms there’s a piece, and that customer orders.
> 
> One of the customers is going to be disappointed.
> 
> Swap the display case for a joint bank account, and turn the waitstaff into point-of-sale devices. You and your partner both decide to buy a new phone at the same time, but there’s only enough in the account for one. Someone—the bank, the store, or you—is going to be very unhappy.

The essence of the problem is the following:

- There is an inventory of ingredients (state, a map from ingredient name -> available amount)
- There are some recipes that translate an item from the menu into quantities of ingredients (e.g. for menu item *ice cream* you need 2 *scoops of ice cream*)
- You have arbitrary orders coming in; an order is any number of menu items and how many of each a customer wants.
- You have to tell the customer whether the order can be fulfilled or not (just returning `true` or `false` is sufficient)

The goal is to implement a general-purpose solution that works for any amount of ingredients and recipes. There is an example scenario that is used for illustration, however, and in this scenario you have three recipes and two ingredients:
- *pie* needs 1 *slice of pie*
- *ice cream* needs 2 *scoops of ice cream*
- *pie à la mode* needs 1 *slice of pie* and 1 *scoop of ice cream*

What I like about this is how straight forward it is. You have some state (your inventory) and all you need to do to handle an order is update that state according to the ingredients that are needed. At the same time, this is not a "made up" situation – many concurrency problems in the real world look something like this.

There are multiple (non-STM) approaches for how to solve this. Arne [has collected a bunch of them](https://github.com/plexus/pie-a-la-mode):

- Use a global lock that must be acquired before reading from or changing the inventory 
- Use an atomic reference for the inventory (requires immutable inventory values!)
- Use some sort of queue for orders and send them to a central "head chef" who is the only one that can manage the inventory

For someone used to imperative programming, the lock solution is probably the first thing that comes to mind. The issue with using locks (in general) is that they impose a mental load on the programmer – they have to remember to actually do the locking, because nothing technically prevents you from accessing the inventory without first acquiring the lock. Fortunately, this isn't a really noticeable problem in this case. Instead of using a manual lock, you'd probably reach for a map implementation that synchronises internally, such as Java's `ConcurrentHashMap`[^1].

[^1]: It *does* become a problem though if you decide you want to increase concurrency by using a separate lock for every piece of state.

The slightly more fancy imperative solution is using a queue and handing orders to some poor thread who has to do all the updating. This is more fancy because it separates the concurrent part (all the waiters taking orders) and the synchronised part (taking ingredients out of the inventory). Essentially, waiters don't have to announce to the whole café anymore that they're now taking an order.

The solution using an atomic reference is more up the functional programmer's alley. Values of an atom are updated *atomically* (who'd've thought) using a pure function that takes the old value and returns the new value. It's crucial that the values themselves are immutable – because atomic updates may have to be retried if multiple threads try to update at the same time. The special thing about atomic references is that they don't use locks, which, among other things, means that there is no overhead to reading the value from an atom (unfortunately this doesn't give us a benefit in the case of the pie à la mode problem). 

Now, there is a flaw in all of these solutions: they treat the inventory as one big piece of state, rather than treating the *items* in the inventory as state. Imagine the following: you have two waiters (threads) taking orders. But one only ever gets pie orders, the other only ever gets ice cream orders. In theory, there is no conflict between the two! One could get pie slices, the other could get scoops of ice cream and they would never run into each other. It could run 100% concurrently.
However, if the entire inventory becomes locked away anytime someone needs something, the ice cream waiter will have to wait for the pie waiter and vice versa, for no sensible reason. In the worst case, the waiters will be slowed down by a factor of 2!<br>
Conversely, we cannot treat every ingredient separately, because sometimes they *do* have to be connected (namely when someone orders pie à la mode). So, how do we define state that coordinates with other state, but *only if it needs to*? 🤔

## Coordinated, lock-free concurrency with STM

STM provides exactly what we're looking for. Not only that, it is also a very high-level construct, meaning that it doesn't take a lot of brain power to apply it to our problem. The basic idea of STM is this:

- any state is a reference to an immutable value (the number of ingredients left in our case)
- to update state, we have to start a *transaction*, which makes two guarantees[^2]:
  - *atomicity*: the effects of a transaction become visible to other threads all at once ("all or nothing")
  - *isolation*: the code inside a transaction is unaffected by changes made in other transactions

[^2]: See the explanation of STM in [Beautiful Concurrency](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.365.1337&rep=rep1&type=pdf).

These two guarantees are really powerful. They allow us to coordinate different pieces of state using code that effectively doesn't care about concurrency. In our 2-waiter issue earlier, a good STM implementation could hand out scoops of ice cream and slices of pie fully concurrently.

You can think of STM references as atomic references that can be intertwined with other atomic references. Similar to atomic references, the secret sauce for transactions is *retrying* if there's a conflict between two transactions that ran at the same time. I'm not part of the group of smart people who know how to implement these things efficiently, so I can't tell you much about how this works – I just know *that* it works and that it's a pretty nifty tool.

## Solution

Okay, let's finally look at the solutions to the "pie à la mode" problem in Clojure and Haskell. I'll first specify the overall structure I used for both, then I'll show you the implementations. 

### General structure

The *domain* of the problem has largely been defined already:
- we have 2 enums: `ingredient` (pie slice or ice cream scoop) and `menu-item` (pie, ice cream or pie à la mode)
- `inventory` = Map (`ingredient` -> `integer` state)
- `order` = Map (`menu-item` -> `integer`)
- `ingredients` = Map (`ingredient` -> `integer`)

We define the recipes (Map `menu-item` -> `ingredients`) at the top level, i.e. statically, but the code accessing them should work no matter how we define it. Furthermore, we have three functions:

- function to get all cumulative `ingredients` required for an `order` e.g. for the order `{:pie 3, :ice-cream 1}` it should return `{:pie-slice 3, :ice-cream-scoop 2}`
  - get the recipes for all the items in the order
  - multiply the ingredients of each recipe by the corresponding quantity in the order
  - merge all the ingredients into a single map
- function to check whether there are enough ingredients left in an inventory (returns a boolean)
- function to handle an order
  - take an inventory and an order
  - if enough ingredients are left, update the inventory
  - return whether it was successful

I'll also provide an example for an inventory definition at the bottom of the solutions.

### In Clojure

```clojure
(ns pie-a-la-mode)

(def recipes
  {:pie {:pie-slice 1}
   :ice-cream {:ice-cream-scoop 2}
   :pie-a-la-mode {:pie-slice 1 :ice-cream-scoop 1}})

(defn order->ingredients [order]
  (->> order
       (map (fn [[item quantity]]
              (update-vals (get recipes item) (partial * quantity))))
       (apply merge-with +)))

(defn enough-inventory? [inv ingredients]
  (every? (fn [[ingredient quantity]]
            (<= quantity @(get inv ingredient)))
          ingredients))

(defn handle-order
  [inv order]
  (let [ingredients (order->ingredients order)]
    (dosync
     (let [enough? (enough-inventory? inv ingredients)]
       (when enough?
         (doseq [[ingredient quantity] ingredients]
           (alter (get inv ingredient) - quantity)))
       enough?))))

(def sample-inventory
  {:pie-slice (ref 12)
   :ice-cream-scoop (ref 50)})
```


The name used for "STM state" in Clojure is [`ref`](https://clojure.org/reference/refs). The STM operations we can see here are as follows:

- `@(get inv ingredient)` gets the ref containing the current available amount of the ingredient and then **reads** the current value. `@` is shorthand for `deref`, the read function.
- Everything inside `dosync` is part of a transaction.
- `(alter (get inv ingredient) - quantity)` **changes** the value of the ref for the ingredient by applying a function to it. This is the same as `(ref-set (get inv ingredient) (- @(get inv ingredient) quantity))`, i.e. get the old value -> apply the function -> set the result as the new value 

Notably, because Clojure is a dynamic language, the compiler wouldn't catch it if we were to `alter` our state outside of the `dosync`. These constraints are enforced at runtime using exceptions. 

Another thing that can only be enforced at runtime is the prevention of non-STM side effects inside of transactions. Since transactions may be retried any number of times, you don't want to do anything in there that is costly or not idempotent. Clojure offers a way to "mark" functions that should not be used inside of transactions using a macro called `io!` that throws an exception if run in a transaction. Unfortunately, this macro is not super effective. A lot of Clojurists (including myself) don't really use it in their functions, and as soon as you have to call out to a Java library, it can't help you anymore anyway.

One more observation about the code is that we use the same function (`deref`) for reading from refs both inside and outside transactions. This means that we could call `enough-inventory?` in its current form outside of a transaction. In general, reading without a transaction is valid, of course – but in this case it means that you may get an inconsistent view of the inventory because the individual parts of the inventory could change while you're going over them.

### In Haskell

```haskell
module PieALaMode where

import Control.Monad
import Control.Concurrent.STM
import qualified Data.Map as Map
import Data.Maybe
import Data.Functor
import System.IO.Unsafe

data Ingredient = PieSlice | IceCreamScoop
  deriving (Eq, Ord)
data MenuItem = Pie | IceCream | PieALaMode
  deriving (Eq, Ord)

type Inventory = Map.Map Ingredient (TVar Int)
type Order = Map.Map MenuItem Int
type Ingredients = Map.Map Ingredient Int

recipes :: Map.Map MenuItem Ingredients
recipes = Map.fromList [(Pie, Map.singleton PieSlice 1),
                        (IceCream, Map.singleton IceCreamScoop 2),
                        (PieALaMode, Map.fromList [(PieSlice, 1), (IceCreamScoop, 1)])]

ingredientsFromOrder :: Order -> Ingredients
ingredientsFromOrder order = foldl (Map.unionWith (+)) Map.empty $ map toIngredients $ Map.toList order
  where toIngredients (item, quantity) =
          Map.map (* quantity) $ fromMaybe Map.empty $ Map.lookup item recipes

enoughInventory :: Inventory -> Ingredients -> STM Bool
enoughInventory inv ingredients = fmap (all id) $ mapM enoughIngredient $ Map.toList ingredients
  where enoughIngredient (ingredient, quantity) = fmap (> quantity) $ readTVar $ inv Map.! ingredient

handleOrder :: Inventory -> Order -> IO Bool
handleOrder inv order = let ingredients = ingredientsFromOrder order in
  atomically $ do
        enough <- enoughInventory inv ingredients
        when enough $ mapM_ takeIngredient $ Map.toList ingredients
        pure enough
  where takeIngredient (ingredient, quantity) = let var = inv Map.! ingredient in
          (readTVar var) <&> (-quantity +) >>= (writeTVar var)

{-# NOINLINE sampleInventory #-}
sampleInventory :: Inventory
sampleInventory = unsafePerformIO $ mapM newTVarIO $ Map.fromList [(PieSlice, 12), (IceCreamScoop, 50)]
```

The Haskell solution follows pretty much everything from the Clojure solution. Apart from minor things like imports, type definitions and different syntax this is really similar! The main and interesting difference is Haskell's way to encode the STM in its type system.

Haskell is a purely functional language, meaning that code cannot have side effects. Clojure's `ref`, `deref` and `alter` have side effects: creating, reading or writing mutable state. So these functions can not directly exist in Haskell. Instead, side effects from the STM are captured in a monad (fittingly called `STM`). What's really interesting is that it's not the same monad used to capture "regular" side effects (`IO`) but a separate construct that *only* allows you to do things related to STM. The only way to "convert" the `STM` monad to the `IO` monad and therefore *actually* execute the side effects is by using the transaction function `atomically :: STM a -> IO a`. This means that it's impossible to change STM state outside of a transaction. Conversely, it's impossible to do non-STM side effects inside a transaction. And in contrast to Clojure, this is consistent and verified at compile time. 

Since I don't have the eloquence or the experience to get more into the whole "side effects in Haskell" thing, I highly recommend reading [SPJ's](https://en.wikipedia.org/wiki/Simon_Peyton_Jones) article [Beautiful Concurrency](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.365.1337&rep=rep1&type=pdf) if you want to learn more about how `IO` and `STM` work. It serves as a very good introduction to STM in general and how it's done in Haskell (without requiring prior Haskell knowledge).

Now, while `atomically` and thus, a transaction, is indeed the only way to get from `STM` to `IO`, that doesn't mean that there are no shortcuts to perform an action without going through `STM`. An example of this can be found in the above code, where I create the sample inventory `TVar`s (equivalent to `ref`s in Clojure) using `newTVarIO` (which returns `IO (TVar Int)`) instead of `newTVar` (which returns `STM (TVar Int)`). Conceptually, they're equivalent – you can just "wrap" `newTVar` in an `atomically` to make a transaction. However, you cannot use transactions at the top level of your code with `unsafePerformIO` (which is required there to "unwrap" the resulting value), so this additional function serves as a way to close that gap.

Just for convenience, a similar function exists for reading: `readTVarIO`, which returns the value of a `TVar` in an `IO` rather than an `STM`. I didn't make use of this function here – but it illustrates a subtle difference to Clojure, where `deref` is both the function used to read in transactions *and* the function to read outside of transactions. So, `deref` corresponds to both `readTVar` and `readTVarIO`.

### Deeper comparison

It's easy to see that, for the purpose of the "pie à la mode" problem, Haskell and Clojure are practically equivalent. Disregarding the type system, there is almost a 1:1 mapping between the two. However, researching the Haskell STM, I found some more consequential differences, which I find reflect the philosophies of the two languages pretty well. Put simply: Haskell's STM is more *complete*, while Clojure's STM is more *optimal* in the functionality it provides.<br>
Here's what I mean by this: The basic feature set of the STM is equivalent in both – creating refs/TVars, starting transactions, reading and writing – but on top of this, there are essentially two (non-trivial) features in each language that are not present in the other.

#### Clojure: more ways to optimise

In Clojure, these are the two functions `commute` and `ensure`. They don't extend the STM in terms of ability to model things, but they provide two optimisations, one directly and one indirectly.

`commute` is like `alter` in that it lets you update a ref using a function, but unlike `alter` it is a mechanism to allow for more concurrency by saying "this particular change is allowed to happen from other transactions *while* I'm in my transaction". In effect, it violates the *isolation* guarantee of transactions for more concurrency in cases of state that is only changed in ways that are "naturally consistent". This is often the case for counters, for example - we don't care whether we increment first or some other transaction happening at the same time increments first. "commuters will never block commuters", as the [Clojure reference](https://clojure.org/reference/refs) puts it.

`ensure` is interesting because it is essentially a way to opt in to a feature that is mandatory in Haskell. This is probably the biggest difference between the two in the set of features they share: Clojure's `deref` doesn't "lock" the ref. What this means is that your transaction could read a ref via `deref` (and it would always see a consistent value based on the initial snapshot!) but there is no guarantee that the actual value of the ref wasn't changed by another transaction in the meantime. This has one obvious disadvantage: if you have some sort of validation logic inside of your transaction that depends on a value you read but never write to, it can break. On the other hand, if you don't *need* to fix the value, you can skip it and get more concurrency.<br>
Though to be honest, I think it would have been better to have a function `ref-read` that combines `ensure` and `deref` in one to make it clearer. 

#### Haskell: more expressive power

In Haskell, there are two additional functions that allow for more sophisticated transactions that Clojure lacks: `retry`/`check` and `orElse`. They let you express things in the STM that you cannot easily express in Clojure.

Starting with `retry` and `check` (which is essentially an `if` and `retry` combined). These functions are a way to "put a transaction on hold" as long as the state it depends on doesn't change. When the state changes, the transaction will be retried, i.e. re-executed. This is really cool for producer-consumer-type situations where you want to work with something but you need to wait until there is enough of it. In fact, our "pie à la mode" problem is one of those situations! Let's imagine that the problem is extended: there now is a supplier who may deliver more ice cream and pie at any point in time to replenish the inventory. With the way we implemented it above, customers would need to re-order once there are new ingredients in case their previous order could not be fulfilled. Wouldn't it be nice to "remember" their order for the next time the inventory is refilled? Well, maybe, maybe not, both approaches can be valid depending on what we want to achieve. But still, what would our café look like using `retry`?

```haskell
handleOrderRetry :: Inventory -> Order -> IO ()
handleOrderRetry inv order = let ingredients = ingredientsFromOrder order in
  atomically $ do
        enough <- enoughInventory inv ingredients
        if enough then mapM_ takeIngredient $ Map.toList ingredients else retry
  where takeIngredient (ingredient, quantity) = let var = inv Map.! ingredient in
          (readTVar var) <&> (-quantity +) >>= (writeTVar var)
```

`if enough then ... takeIngredient ... else retry` makes a lot of sense, I think. Customers now don't have to be rejected anymore in case the next pie will be out of the oven in a minute. Unfortunately, it doesn't seem like we can attach a timeout to this to say "drop the transaction if we've had no luck for x seconds" or something, but this is still pretty neat.

The second special Haskell feature is `orElse`, which lets you express choice in transactions. `orElse a b` first runs `a` – if that retries, it runs `b` instead, and if *that* retries, it retries the whole thing. To apply it to our `handleOrderRetry` (in a sort of dumb way), this would basically generalise the function over an arbitrary number of inventories, where each is preferred to its successor. Try to take from the first inventory, if that doesn't have enough try to take from the second and so on and so forth.

```haskell
-- let's imagine we have the retrying function from above but without the `atomically`
handleOrderRetry :: Inventory -> Order -> STM ()

handleOrderNInventories :: [Inventory] -> Order -> IO ()
handleOrderNInventories invs order = atomically $ foldl' orElse retry $ map (\inv -> handleOrderRetry inv order) invs
```

I have to say, I have no idea how I would do something like this in Clojure. Pretty cool.

#### Comparison table

Below is a table that shows the different STM operations, what they're called in the two implementations and how they differ. I figured that might be good for a quick overview.

| Name                  | Clojure            | Haskell         | Notes                                                                                         |
|-----------------------|--------------------|-----------------|-----------------------------------------------------------------------------------------------|
| Create reference      | `ref`              | `newTVar`       | `newTVarIO` for creation outside of a transaction<br>`ref` is not safe inside of transactions |
| Run transaction       | `dosync`           | `atomically`    |                                                                                               |
| Read (without fixing) | `deref`            | /               |                                                                                               |
| Read (with fixing)    | `deref` + `ensure` | `readTVar`      | `readTVarIO` for reading outside of a transaction                                             |
| Write                 | `ref-set`          | `writeTVar`     |                                                                                               |
| Update                | `alter`            | /               | Haskell equivalent for unary function `f`: `readTVar x <&> f >>= writeTVar x`                 |
| Update commutatively  | `commute`          | /               |                                                                                               |
| Retry                 | /                  | `retry`/`check` |                                                                                               |
| Choose                | /                  | `orElse`        |                                                                                               |


## Conclusion

When I began writing this post, I thought it would be a quick one: briefly describe the problem, dump two blocks of code, point out basic differences, done. Well, that... didn't work out, it seems. I'm realising (again) that I have way too much fun talking about programming problems and comparing their solutions in different languages. 

Throughout the research for this post, I found that the Haskell STM is really well-designed and elegant. It also showed me that there are possibilities beyond what is provided by Clojure (which is already like magic to me). I tend to agree with [this post](http://computationalthoughts.blogspot.com/2008/03/some-examples-of-software-transactional.html):

> The most impressive thing with the transactional memory implementation in GHC is in my opinion the retry combinator. It handles all conditional synchronization by complete magic. [...] [T]he simplicity of the API is just marvellous.

The comparison also highlighted a lot of Clojure's shortcomings in terms of safety for me. Sure, you can still write code with race conditions in Haskell but the `STM` monad serves as a beautiful way to compose and verify transactions, making it way less likely (and communicating transaction effects much better). Don't get me wrong – Clojure is already *awesome* compared to what we have to deal with in imperative languages. Immutable data structures *by themselves* remove so much of the pain of concurrency. STM on top is... *chef's kiss*. Such a simple way to model so many complicated concurrency problems. 

However, I have to admit that I'd much rather have `retry` and `orElse` in Clojure than `commute`. Granted, I don't write high-performance code and I haven't used the STM for anything critical, so I can't speak to efficiency. But there's no way around it for me personally: I'll take completeness and expressivity over opportunities for optimisation. I haven't looked into whether there has been a discussion about it before or whether there is a reason `retry` wasn't implemented in Clojure. If you know more, please share!

To conclude with my honest opinion: through its STM's simple, well-defined and expressive nature, Haskell takes the pie and eats it too (or something like that).
