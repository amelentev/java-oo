# Java Operator Overloading #

Implementation of ([Scala-like]) [Operator Overloading] for Java language.
Works with JavaC and Netbeans IDE.

Example (see also: javac-oo/sample/*.java):

```java
import java.math.BigInteger;
public class Test {
	public static void main(String[] args) {
		BigInteger  a = BigInteger.valueOf(1),
			b = BigInteger.valueOf(2),
			c = a + b*a; // here is `magic`
		System.out.println(c);
	}
}
```

Here `a+b*a` will be transformed to `a.add(b.multiply(a))`.

# Installation #

## Eclipse IDE update site ##
Click in menu: Help - Install New Software. Enter in "Work with" field:

	http://amelentev.github.com/eclipse.jdt-oo-site/

## Netbeans IDE ##
Add [javac-oo-plugin.jar] as compile or processor library to Netbeans and enable "Annotation Processing in Editor" (Project Properties-Build-Compiling). Tested on 7.1.1, 7.2-dev

## javac, ant, etc ##
Just add [javac-oo-plugin.jar] to classpath:
```
javac -cp javac-oo-plugin.jar <sources>
```
Demo at [examples/compile.sh](https://github.com/amelentev/java-oo/blob/master/examples/compile.sh)

## Maven ##
Look at [javac-oo-mvndemo/pom.xml](https://github.com/amelentev/java-oo/blob/master/javac-oo-mvndemo/pom.xml)

# Details #

Supported operators (operator to method map):

binary:

	| OPERATOR | METHOD    |
	------------------------
	| +        | add       |
	| -        | subtract  |
	| *        | multiply  |
	| /        | divide    |
	| %        | remainder |
	| &        | and       |
	| |        | or        |
	| ^        | xor       |
	| <<       | shiftLeft |
	| >>       | shiftRight|

unary:

	| - | negate |
	| ~ | not    |

comparison:

	| <, <=, >, >= | compareTo	| example: `a < b` <=> `a.compareTo(b)<0`
	`==` is not overloadable because it will break things

index:

	| []  | get       | `v = lst[i]` <=> `v = lst.get(i)`
	| []= | set, put  | `map[s] = v` <=> `map.put(s,v)`,  `lst[i] = v` <=> `lst.set(i,v)`

These methods exists in many java classes (ex:BigInteger,BigDecimal) so you can
use operators on them "out of the box".


## Subprojects

- javac-oo-plugin
	- plugin to JavaC 1.7 and **Netbeans IDE** for operator overloading. Based on [javac-oo].

- eclipse-oo-plugin
	- [Eclipse] IDE (JDT) plugin for OO support.

- [javac-oo]
	- patched version of JavaC 1.7 for Operator Overloading support. If you need standalone javac compiler with OO.

- [eclipse-oo]
	- Eclipse [Java Developer Tools] fork for Operator Overloading.
	- use it if you need native **[Eclipse] IDE support**, or Eclipse Java Compiler support.

comming soon:

- idea-oo-plugin
	- [Intellij Idea] support

[Scala-like]: http://www.slideshare.net/joeygibson/operator-overloading-in-scala-2923973
[javac-oo]: https://bitbucket.org/amelentev/javac-oo
[lombok]: http://projectlombok.org/
[lombok-oo]: https://github.com/amelentev/lombok-oo
[eclipse]: http://eclipse.org/
[Java Developer Tools]: http://eclipse.org/jdt/
[Intellij Idea]: http://www.jetbrains.com/idea/
[eclipse-oo]: https://github.com/amelentev/eclipse.jdt-oo
[Operator Overloading]: http://en.wikipedia.org/wiki/Operator_overloading

[javac-oo-plugin.jar]: http://amelentev.github.com/mvnrepo/java-oo/javac-oo-plugin/0.1/javac-oo-plugin-0.1.jar
