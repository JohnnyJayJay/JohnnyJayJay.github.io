{:title "A Tale of Java Backwards (In)compatibility"
 :description "javac links in mysterious ways"
 :date "2023-09-24"
 :layout :post
 :tags ["java" "jvm"]
 :comments {:instance "chaos.social" :author "johnny" :id "111119455462737605"}}

Last year, I wrote a [library for WebAuthn authenticator emulation](https://github.com/adessoSE/softauthn) in Java. I did my usual Gradle setup for Java libraries: adding the `java-library` plugin and explicitly setting `sourceCompatibility` and `targetCompatibility` to make sure that I don't accidentally make my releases incompatible with the version that I want to target. In this case, Java 8 (Java enthusiasts, I'll give you a second to let out the sigh). Easy, right?

Well, so I thought too. On all Java code bases I had worked on so far, I would just set `compatibility = X` and assume my code would actually run on version X of the Java runtime. Unfortunately, this is generally not the case. If you've done the same (that is, just setting compatibility options and calling it a day) and you've never had a problem, congrats. You were lucky, I suppose.

## The headscratcher

But let's start from the beginning and explain the issue. Here's an example of a broken method from the library:

```java
private byte[] extractAaguid(CBORObject attestationObject) {
    ByteBuffer authenticatorData = ByteBuffer.wrap(attestationObject.get("authData").GetByteString());
    authenticatorData.position(37);
    byte[] aaguid = new byte[16];
    authenticatorData.get(aaguid);
    return aaguid;
}
```

What made me really scratch my head is that it's broken in a very peculiar way. First of all, it is fully compatible with all Java versions starting from Java 8. By "compatible", I mean there are no syntactic or semantic issues, both in terms of the language and the standard library. As a corollary, you can *compile* it using JDK 17 while targeting JRE 8. And so... it should run?

Except, oops:

```plaintext
Exception in thread "main" java.lang.NoSuchMethodError: java.nio.ByteBuffer.position(I)Ljava/nio/ByteBuffer;
```

This is what you'll get if you compile the method using JDK 17 and the aforementioned compatibility flags and then run it on JRE 8. What happened?

## We do a little linkage breaking

If you want to try this yourself, here's the minimal reproduction case:

```java
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[1]);
        buffer.position(0);
    }
}
```

As it turns out, the method that gets called here – `buffer.position(0)` – changes from JDK 12 to JDK 13.\ 
Before the change, there was a method `position(int)` on the class `Buffer` that returned `Buffer`. After the change, that method still existed, however a *highly complex override* was added to `ByteBuffer`:

```java
@Override
public ByteBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
} 
```

The person who added this probably didn't think twice about it. It's just a convenience override removing the need to cast back to `ByteBuffer` if you chain a bunch of operations. In fact, the change was thought to be so insignificant that the [javadoc](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html#position(int)) does not mention when or even *that* it was added. After all, supposedly nothing changed for existing code – a stronger return type does not break call sites. 

While this is technically true on the language level (as `javac` continues to work), it has the unfortunate side effect that every compiler prior to Java 13 links `buffer.position(0)` to the method in `Buffer` (`java/nio/Buffer.position:(I)Ljava/nio/Buffer;`), while every other compiler links it to the method in `ByteBuffer` (`java/nio/ByteBuffer.position:(I)Ljava/nio/ByteBuffer;`). From old to new, these linkings are compatible thanks to dynamic dispatch, but from new to old they are not.

## RTFM

As usual, when you discover something that looks like a bug, it turns out that it was just you not reading documentation. Because of course, this is not a fringe issue. In fact, if you compile the example using `javac` directly, you'll get the following warning:

```plaintext
$ java -version
openjdk version "17.0.8" 2023-07-18
OpenJDK Runtime Environment Temurin-17.0.8+7 (build 17.0.8+7)
OpenJDK 64-Bit Server VM Temurin-17.0.8+7 (build 17.0.8+7, mixed mode, sharing)
$ javac -source 8 -target 8 Main.java 
warning: [options] bootstrap class path not set in conjunction with -source 8
1 warning
```

Interestingly, gradle does not show/propagate this warning (here in the library project):

```plaintext
$ grep Compatibility build.gradle.kts
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
$ ./gradlew compilejava

BUILD SUCCESSFUL in 586ms
1 actionable task: 1 executed
```

It doesn't even show up when enabling the `--info` flag for verbose output, even though it uses the incompatible compiler:

```plaintext
Compiling with toolchain '/home/johnny/.sdkman/candidates/java/17.0.8-tem'.
Compiling with JDK Java compiler API.
```

For some reason, Gradle [seems to have been removed](https://stackoverflow.com/questions/16679593/gradle-compilejava-task-warning-options-bootstrap-class-path-not-set-in-conju) this log message at some point. 

But OK, clearly, I should have known about the bootstrap classpath. So I wondered: why have I never seen it used in the wild? Maybe it's because it's kind of a [pain in the ass](https://stackoverflow.com/questions/22681544/how-to-set-gradle-options-bootclasspath-in-an-os-independent-manner) to set up in a platform-agnostic way since you have to make assumptions about the user's system – they need to have the JDK in question installed, you need to know its location and you have to figure out *what parts of it* need to be added to the classpath.

Fortunately, Gradle 6.7 [added](https://docs.gradle.org/6.7/release-notes.html#new-jvm-ecosystem-features) support for ["toolchains"](https://docs.gradle.org/current/userguide/toolchains.html), a way to remove the pain from `javac`'s compatibilty options (and what I should have used from from the get-go). Here's what that looks like:

```gradle
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
```

### Wait, maybe there is a bug?

OK, sure, toolchains, bootstrap classpath, got it. There is something that I haven't figured out yet, though, and it's again very strange behaviour from Gradle.

Gradle will actually fail to compile (with the `compatibility` options) if you try to *use* the new override, but `javac` will not:

```plaintext
$ ./gradlew compilejava

> Task :app:compileJava FAILED
/tmp/compat-test/app/src/main/java/App.java:16: error: incompatible types: java.nio.Buffer cannot be converted to java.nio.ByteBuffer
        ByteBuffer wtf = buf.position(0);
                                     ^
1 error

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileJava'.
> Compilation failed; see the compiler error output for details.

* Try:
> Run with --info option to get more log output.
> Run with --scan to get full insights.

BUILD FAILED in 439ms
1 actionable task: 1 executed

$ javac -source 8 -target 8 app/src/main/java/App.java
warning: [options] bootstrap class path not set in conjunction with -source 8
1 warning
```

So this means that Gradle "knows" the correct method to call in Java 8 and makes sure the code is compatible with it, but... still links to the wrong method in the end. Splendid.

## Stop using the compatibility options

I'm not sure if this has changed by now but last year, almost 2 years after toolchains were released, IntelliJ *still* didn't generate the toolchains block for new projects. It's about time that changes if it can prevent bugs like these.
At the end of the day, this was a bug caused mostly by developer error (myself) but I still found it very interesting how such an innocuous addition to the standard library could cause such a severe incompatibility. You just don't expect something like that. Lesson learned though: do *not* use `sourceCompatibility` and `targetCompatibility` to target a specific runtime; use toolchains. This makes me wonder if there even is a Maven equivalent to this... 

I'm curious to hear from you: did you know that issues like this exist? Are you using toolchains with Gradle?
