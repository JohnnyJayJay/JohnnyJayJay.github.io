{:title "An Introduction to JVM Bytecode"

 :description "Java, Kotlin, Scala, Clojure - They all compile to a language we rarely touch. Let's demystify it."
 :date "2019-11-01"
 :layout :post
 :tags ["jvm" "java"]
 :toc true}

It is very likely that you were taught the same lesson as me when you first started learning Java: the benefits of using the JVM.  
Usually, that lesson is just a very brief and general description of Java’s well-known cycle: Write source code, compile to bytecode, run anywhere. Thanks, JVM!

Nowadays, that’s hardly a cutting-edge selling point anymore. Nonetheless, I’d like to talk about Java’s bytecode part today, more specifically about bytecode instructions. Even though almost all Java developers know what bytecode is and why it is used, many of them don’t take the time to actually look at how it works or what to make of the instructions.

## Why should I care?

If you’ve never come across bytecode directly, you may be wondering wy you should read about it in the first place. After all, it doesn’t seem to be part of a Java developer’s everyday life, and I get it: this knowledge is not really required to write good Java/Kotlin/Scala applications. But in my opinion, there are still a few reasons to learn it:

- Knowing the system is always a good thing. The JVM should not be a black box to you.
- It may answer your questions about how certain things work and look like after compilation.
- You can compare the details of different JVM-based languages and see what they have in common or do differently.
- You can actually do a lot of fancy stuff with it. Chances are that some of the frameworks you already use even perform bytecode manipulation, runtime class generation or something similar.
- If you want to look at how optimisation works or how you can make your own optimiser, it’s the place to start.
- It’s fun!

Personally, I got more into bytecode because I needed it for the realisation of a small idea that required me to manipulate bytecode at runtime. And it turned out to be much easier than expected.

## A simple algorithm

In order to learn how to read bytecode instructions, we are going to have to look at some. Luckily, we can disassemble any bytecode to make it humanly readable using the `javap` binary available in the JDK (I’m using [Oracle JDK 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)). We’ll come to that shortly.

Let’s begin by writing a simple algorithm to break down on a lower level later:

```java
public final class DivisorPrinter {
	
  private final int number;
	
  public DivisorPrinter(int number) {
    this.number = number;
  }
	
  public void print() {
    for (int i = 1; i <= number; ++i) {
      /* If i is a divisor of number */
      if (number % i == 0) { 
        System.out.print(i);
        if (i != number) {
          System.out.print(", "); // Append a comma if there are more divisors to come
        }
      }
    }
  }
  
}
```

```java
public final class SimpleAlgorithm {
	
  public static void main(String[] args) {
    String input;
    /* If arguments were provided, assign them, else exit the program */
    if (args.length > 0) {
      input = args[0];
    } else {
      System.exit(1);
      return;
    }
    int number = Integer.parseInt(input);
    DivisorPrinter printer = new DivisorPrinter(number);
    printer.print();
  }
	
}
```

This algorithm takes a number as a start argument and prints all divisors of it to the console. If no argument is provided, it just exits. The possible `NumberFormatException` is not handled here, as it would make the result more complex than it needs to be for this purpose.  
I intentionally implemented it like that (some might call it quirky), because this implementation covers a lot of Java’s functionalities: Variable assignment, Object creation, arrays, static & instance method calls as well as static & instance fields. And of course some basic control flow.

## Getting there

Let’s take it a step further and compile SampleAlgorithm.java using `javac SimpleAlgorithm.java`. This will produce two files and put them in the same directory: the corresponding .class files for our two classes. These files contain the bytecode.

When opening them with an ordinary text editor like Notepad++, the results we get aren’t really helpful, although you may recognise some of the descriptors that are readable in this form.

![](/img/jvmbytecode/binary_in_text.png)

Raw bytecode. Not of any educational use.

As I already mentioned, we can use `javap` to look at `.class` files.  
To achieve this, we simply need to run `javap` in a terminal of our choice and provide a class name as the first argument. We get the following result for `javap SimpleAlgorithm`:

```plaintext
Compiled from "SimpleAlgorithm.java"  
public final class SimpleAlgorithm {  
  public SimpleAlgorithm();  
  public static void main(java.lang.String[]);  
}
```

Now, this isn’t really helpful yet; we only get to see what’s exposed to the outside (note the default constructor that was added during compilation).

We want to see the contents of these methods. To do this, we can simply add the `-c` flag to our command, which will tell `javap` to disassemble the instructions. To view [the whole class file](https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html), use `-v` instead.  
We are going to focus on the output of `javap -c SimpleAlgorithm` and `javap -c DivisorPrinter` respectively for now.

```plaintext
Compiled from "SimpleAlgorithm.java"  
public final class SimpleAlgorithm {  
  public SimpleAlgorithm();  
    Code:  
       0: aload_0  
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V  
       4: return  

  public static void main(java.lang.String[]);  
    Code:  
       0: aload_0  
       1: arraylength  
       2: ifle          12  
       5: aload_0  
       6: iconst_0  
       7: aaload  
       8: astore_1  
       9: goto          17  
      12: iconst_1  
      13: invokestatic  #2                  // Method java/lang/System.exit:(I)V  
      16: return  
      17: aload_1  
      18: invokestatic  #3                  // Method java/lang/Integer.parseInt:(Ljava/lang/String;)I  
      21: istore_2  
      22: new           #4                  // class DivisorPrinter  
      25: dup  
      26: iload_2  
      27: invokespecial #5                  // Method DivisorPrinter."<init>":(I)V  
      30: astore_3  
      31: aload_3  
      32: invokevirtual #6                  // Method DivisorPrinter.print:()V  
      35: return
```

This is the output of disassembling SimpleAlgorithm.class; it might look confusing to some people at first glance. But it will be easy to understand once you get the hang of the very simple concept behind it.

## Goto?

The first thing to notice about this code might be the fact that there is no nesting. Each and every instruction is on the same layer and in the same scope. Instead, we now have `goto` operations (e.g. instruction 9). While goto is actually a keyword in Java and thus cannot be used as an identifier, it has no meaning or purpose in the language. The closest we can get are (labeled) `break` and `continue` statements.

In bytecode however, goto and similar instructions are the only way to manipulate control flow, because the concept of nested control flow structures (as seen in almost every higher level imperative language) is just too abstract.

Try nesting a lot of if statements and loops and look what the bytecode of that is like (spoiler: probably not very pleasant to read).

## The Stack

When taking a closer look, you may notice a second thing: there don’t seem to be any variables. Sure, sometimes you can see some sort of argument for the instruction, such as `#2` or `17`. But there aren’t any **direct** assignments, declarations or usages of variables anywhere.

That’s because bytecode is [stack-based](https://www.geeksforgeeks.org/stack-data-structure-introduction-program/). Almost every JVM instruction operates on the stack; the two possible operations are push and pop. So, instead of taking input and returning something, bytecode instructions pop previously pushed elements from the stack and push new ones. Variables do exist, but they aren’t used like in higher-level imperative languages, i.e. like `int foo = 0`. Instead, their values can be assigned from the stack or pushed onto the stack.

Although this is very efficient, it’s not really convenient. You always have to keep track of what’s on the stack; if you miss anything, you will get runtime errors.

Take this line of code for example: `Math.max(1, 2);`.  
After compilation (within a [static initialiser](https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html) of a class), it looks like this:

```plaintext
4: iconst_1  
5: iconst_2  
6: invokestatic  #2                  // Method java/lang/Math.max:(II)I  
9: pop
```

Instructions 4 and 5 push values onto the stack. `iconst` is used to push `int` literals, in this case `1` and `2`.  
The stack now looks like this:

| Index | Value |
| ----- | ----- |
| 1     | 2     |
| 0     | 1     |

So far we’ve only pushed constant values onto the stack. When we invoke a method however, it pops a value for each parameter from the stack (starting from the last parameter, since stacks are [LIFO](https://www.geeksforgeeks.org/lifo-last-in-first-out-approach-in-programming/)) and pushes its return value (if there is one) to the stack.

`invokestatic` in our example is the instruction used to invoke static methods (duh). The `#2` behind it references a method descriptor in the so-called constant pool; since I won’t focus on this particular part of the class file format in this article, it’s sufficient for you to know that it tells the instruction which method to call and where it’s located. Conveniently, `javap` adds a comment for each of those references, telling us exactly what they point to. In this case, `Method java/lang/Math.max:(II)I`.

This is what the instruction does:

1. Pop
2. Pop
3. Invoke `Math.max()` with the popped values as arguments
4. Push return value

After the invocation, this is the state of the stack:

| Index | Value |
| ----- | ----- |
| 0     | 2     |

Since this value isn’t used any further (e.g. stored in a variable), it is simply removed from the stack with the last instruction, `pop`.

This particular type of stack is referred to as the “operand stack”. It stores variables, interim results and returns from methods. For a more sophisticated definition, see [The Structure of the JVM, Chapter 2](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.5.2).

## Inspecting the algorithm

For the sake of simplicity, I won’t go through every single instruction but rather focus on the most important ones.

### aload_n/iload_n

We see these instructions a lot, e.g. in `0: aload_0` or `26: iload_2`. They mean nothing more than “push this local variable’s value onto the stack”. The prefix `a` means that it’s a reference variable, such as an `Object`; `i` refers to the primitive type `int`. Notice that it always pushes the **value** of that variable. Primitive values are “direct” values like `1`, `45.6` or `‘a’`; reference values are **references** to the actual Object in the JVM heap. This tends to confuse people sometimes, but it also explains [why Java always uses “pass by value”](https://www.journaldev.com/3884/java-is-pass-by-value-and-not-pass-by-reference) and not “pass by reference”.

Since local variable names are erased in the process of compiling, they are instead assigned an index based on their first occurrence. This index is referenced by the suffix of the instructions.

Parameters also count as local variables. If you didn’t know already: the keyword `this` is a local variable as well. In every non-static method, it’s actually the first parameter (implicitely). That means that `aload_0` in a non-static context always loads a reference to `this` onto the stack.

Instance methods are therefore not attached to the class they are declared in; other values can be passed as `this`. That is why inheritance works.

### astore_n/istore_n

Of course you can also assign new values to existing variables. To do that, there are the `store` instructions. They work pretty much like the `load` instructions, but additionally expect an argument that is popped from the stack. This is then assigned to the variable.

### Array instructions

Arrays in Java are special, because they are, along with the primitive types, remainders of the direct influences of C and other lower level languages. They are considered reference types, but have special instructions instead of methods. Most of them are pretty straight-forward:

- `aaload` - takes an array reference and an index from the stack and pushes whatever is at that index in the array
- `aastore` - takes an array reference, an index and a value and assigns the value to the index in the array
- `arraylength` - pushes the length of a given array

### Creating objects

At first glance, the instructions used to create objects (like `DivisorPrinter` in our example) might look a bit weird:

```plaintext
22: new           #4                  // class DivisorPrinter  
25: dup  
26: iload_2  
27: invokespecial #5                  // Method DivisorPrinter."<init>":(I)V  
30: astore_3
```

In Java (source), object references are created by invoking a constructor, like `new Object()`. Looking at the bytecode, we realise that the creation of an object and the constructor call are actually separated.

`22: new #4 // class DivisorPrinter` creates the object and pushes its reference to the stack; `27: invokespecial #5 // Method DivisorPrinter."<init>":(I)V` invokes the constructor. We see that constructors are considered “special" invokables. They don’t use `invokevirtual` (like instance methods) or `invokestatic` (like static methods), but `invokespecial`. Also, they have a special name (`”<init>”`) and return `void`.

So, `new` creates the object and `invokespecial` invokes the constructor. But what does `dup` do?

It duplicates the last element on the stack and pushes it onto the stack again. So if there is an object reference on the stack and `dup` is called, there are now two references to that object on the stack.

Now, why is that necessary? After `dup`, there is an `iload_2`, which pushes the value of the int variable we parsed earlier. Invoking the constructor after that consumes two elements from the stack: the explicit int argument and the object to call the constructor on. If we had omitted the `dup`, nothing would be left on the stack now, so we wouldn’t be able to store the object reference in a variable via `astore_3` and it would be gone for good.

### Constructors

Let’s focus on `DivisorPrinter` now. When we disassemble it using `javap -c DivisorPrinter`, we get the following output:

```plaintext
Compiled from "DivisorPrinter.java"  
public final class DivisorPrinter {  
  public DivisorPrinter(int);  
    Code:  
       0: aload_0  
       1: invokespecial #1                  // Method java/lang/Object."<init>":                                                ()V  
       4: aload_0  
       5: iload_1  
       6: putfield      #2                  // Field number:I  
       9: return  

  public void print();  
    Code:  
       0: iconst_1  
       1: istore_1  
       2: iload_1  
       3: aload_0  
       4: getfield      #2                  // Field number:I  
       7: if_icmpgt     48  
      10: aload_0  
      11: getfield      #2                  // Field number:I  
      14: iload_1  
      15: irem  
      16: ifne          42  
      19: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                io/PrintStream;  
      22: iload_1  
      23: invokevirtual #4                  // Method java/io/PrintStream.print:                                                (I)V  
      26: iload_1  
      27: aload_0  
      28: getfield      #2                  // Field number:I  
      31: if_icmpeq     42  
      34: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                io/PrintStream;  
      37: ldc           #5                  // String ,  
      39: invokevirtual #6                  // Method java/io/PrintStream.print:                                                (Ljava/lang/String;)V  
      42: iinc          1, 1  
      45: goto          2  
      48: return  
}
```

At the top, we can see the constructor we invoked from the other code. Looking at `1: invokespecial #1 // Method java/lang/Object.”<init>”:`, we can see that the super constructor of a class is always invoked implicitely. It also gets clear now why object creation and constructors are separated. In this case, we want to invoke a constructor without creating an object (It also works the other way around, see [sun.misc.Unsafe](https://dzone.com/articles/understanding-sunmiscunsafe)).

Another, rather obvious instruction is `putfield`. We see it in `6: putfield #2 // Field number:I`. Again, this references a descriptor in the constant pool. The instruction itself pops the last value from the stack and assigns it to the given field.

`getfield` in turn is used to  -  you get it -  push the value of a field onto the stack.

### Control flow

Now we get to the ugliest part of this short article: control flow without nesting. I, myself, find it sometimes hard to wrap my head around in certain situations.

Let’s look at `7: if_icmpgt 48`. What does it mean? `icmpgt` stands for “int compare greater than”. This means it takes 2 integer inputs from the stack, compares them, and, should the first one be greater than the second one, skips to instruction `48`, similar to `goto`. In the source code, this is the condition of our for loop, where the current interation `i` is compared to the `number` field.

In the loop, there is a second control flow element - an if statement checking whether `i` divides `number`. In the bytecode, it looks like this:

```plaintext
11: getfield      #2                  // Field number:I  
14: iload_1  
15: irem  
16: ifne          42
```

First of all, `number` and `i` are fetched in instruction 11 and 14. Then, `irem` is called on these values, which calculates the remainder of a division (the %-operator in source code).

`ifne` then jumps to the designated branchoffset if the result is not equal to 0. We see that conditions are often flipped like that -  originally, we branch somewhere if the *opposite* is true, but in bytecode it’s just easier to organise the other way around.

After this bit of code comes the whole printing code, but I think you are able to figure that out yourself by now.

### Some more instructions

Let me give you a quick explanation of the instructions in this code I haven’t elaborated on yet.

- `getstatic` - pushes the value of a static field onto the stack
- `if_icmpeq` - similar to `if_icmpgt`, but instead of greater than, check for equality
- `ldc` - pushes a constant from the constant pool onto the stack, such as `String` literals (`“foo”`)
- `iinc` - increments a local variable by the given amount
- `return` - returns the last element on the stack or `void`, if empty and “leaves” the method call

You can find a table of all JVM instructions on [Wikipedia](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings) or by looking at the [official JVM specification](https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.11).

## Conclusion

I hope that this article helped you get some insight of what is happening under the hood of a JVM application. Of course, we’re still pretty much on the surface here, so there are still deeper layers for you to explore.

Nevertheless, maybe you now have some new ideas or want to look at a particular compiler output to understand a certain language feature. And even if you don’t, you might have learned something that will prove to be useful some day.

---

This article was originally published in "The Startup" on [medium.com](https://medium-com).
