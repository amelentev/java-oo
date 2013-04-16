# Java Operator Overloading #

Implementation of ([Scala-like]) [Operator Overloading] for Java language.
Works with standard JavaC compiler, [Netbeans IDE], [Eclipse IDE], [IntelliJ IDEA] IDE and any build tools.

Example (see other examples at [examples/](https://github.com/amelentev/java-oo/tree/master/examples) dir):

```java
import java.math.*;
import java.util.*;
public class Test {
	public static void main(String[] args) {
		BigInteger  a = BigInteger.valueOf(1), // without OO
				b = 2, // with OO

				c1 = a.negate().add(b.multiply(b)).add(b.divide(a)), // without OO
				c2 = -a + b*b + b/a; // with OO

		if (c1.compareTo(c2)<0 || c1.compareTo(c2)>0) System.out.println("impossible"); // without OO
		if (c1<c2 || c1>c2) System.out.println("impossible"); // with OO

		HashMap<String, String> map = new HashMap<>();
		if (!map.containsKey("qwe")) map.put("qwe", "asd"); // without OO
		if (map["qwe"]==null) map["qwe"] = "asd"; // with OO
	}
}
```

# News #
17 Apr 2013. [IntelliJ IDEA](#intellij-idea-ide) IDE plugin.

26 Nov 2012. [Version 0.2] released. New feature: [Implicit type conversion](https://github.com/amelentev/java-oo/issues/4) via static _#valueOf_ method.
[Version 0.2]: https://github.com/amelentev/java-oo/issues?milestone=1&state=closed

# Installation #

## [Eclipse IDE] update site ##
Click in menu: Help - Install New Software. Enter in "Work with" field:

	http://amelentev.github.io/eclipse.jdt-oo-site/

Tested on 4.2.1

## [Netbeans IDE] ##
1. Add [javac-oo-plugin.jar] as compile or processor library to Netbeans.
2. Enable "Annotation Processing in Editor" (Project Properties -> Build -> Compiling).

Tested on 7.2.1

## [IntelliJ IDEA] IDE ##
1. Install [idea-oo-plugin](http://plugins.jetbrains.com/plugin?pr=&pluginId=7224)
(mirror: [idea-oo-plugin.jar]) <br/>
For [Maven projects](#maven) installation is done. IDEA should setup everything according to pom.xml. <br/>
For other project types: <br/>
2. Add [javac-oo-plugin.jar] as compile or processor library.
3. Enable Annotation Processing:
`Menu File -> Settings -> Compiler -> Annotation Processing -> Enable annotation processing`
4. Make sure you use `javac` compiler in `Settings -> Compiler -> Use compiler`. <br/>
Tested on IDEA Commutity Edition 12.1.1

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
	`==` and `!=` is not overloadable because it will break things

index:

	| []  | get       | `v = lst[i]` <=> `v = lst.get(i)`
	| []= | set, put  | `map[s] = v` <=> `map.put(s,v)`,  `lst[i] = v` <=> `lst.set(i,v)`

Implicit type conversion:

if _expression_ has type _ExpressionType_ and there are static method _RequredType RequredType#valueOf(ExpressionType)_<br/> 
then _expression_ can be assigned to _RequredType_.
example: <br/>
`BigInteger a = 1` translates to `BigInteger a = BigInteger.valueOf(1)`

These methods exists in many java classes (example: BigInteger, BigDecimal) so you can
use operators on them "out of the box". Or you can add these methods to your classes to use OO (see [examples/Vector.java](https://github.com/amelentev/java-oo/blob/master/examples/Vector.java)).


## Subprojects / Implementation details

- javac-oo-plugin
	- plugin to JavaC 1.7 and [Netbeans IDE] for operator overloading. Based on [javac-oo].

- eclipse-oo-plugin
	- [Eclipse IDE] (JDT) plugin for OO support.
	- Patch Eclipse Java Compiler to allow OO.

- idea-oo-plugin
	- [IntelliJ IDEA] IDE plugin for OO support. 
	- Modify Java frontend in IDEA to allow OO. Need javac-oo-plugin to actually compile.

- [javac-oo]
	- patched version of JavaC 1.7 for Operator Overloading support. If you need standalone javac compiler with OO.

- [eclipse-oo]
	- Eclipse [Java Developer Tools] fork for Operator Overloading.
	- use it if you need native [Eclipse IDE] support, or Eclipse Java Compiler support.


[Scala-like]: http://www.slideshare.net/joeygibson/operator-overloading-in-scala-2923973
[javac-oo]: https://bitbucket.org/amelentev/javac-oo
[lombok]: http://projectlombok.org/
[lombok-oo]: https://github.com/amelentev/lombok-oo
[Eclipse IDE]: http://eclipse.org/
[Netbeans IDE]: http://www.netbeans.org/
[IntelliJ IDEA]: http://www.jetbrains.com/idea/
[Java Developer Tools]: http://eclipse.org/jdt/
[eclipse-oo]: https://github.com/amelentev/eclipse.jdt-oo
[Operator Overloading]: http://en.wikipedia.org/wiki/Operator_overloading

[javac-oo-plugin.jar]: http://amelentev.github.io/mvnrepo/java-oo/javac-oo-plugin/0.2/javac-oo-plugin-0.2.jar
[idea-oo-plugin.jar]: http://amelentev.github.io/mvnrepo/java-oo/idea-oo-plugin/idea-oo-plugin-0.2.jar
