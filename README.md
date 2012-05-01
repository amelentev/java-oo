# Java Operator Overloading #

Implementation of ([Scala-like]) Operator Overloading for Java language.
Works with JavaC and Netbeans IDE.

Example (see also: javac-oo/sample/*.java):

	:::java
	import java.math.BigInteger;
	public class Test {
		public static void main(String[] args) {
			BigInteger  a = BigInteger.valueOf(1),
				b = BigInteger.valueOf(2),
				c = a + b*a; // here is `magic`
			System.out.println(c);
		}
	}

Here `a+b*a` will be transformed to `a.add(b.multiply(a))`.

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


## subprojects

- javac-oo-plugin
	- plugin to JavaC 1.7 and **Netbeans IDE** for operator overloading. Based on [javac-oo].
	- just add it to classpath while compiling (ant, maven, etc):
		- `javac -cp javac-oo-plugin.jar <sources>`
	- Add it as compile or processor library to Netbeans and enable "Annotation Processing in Editor" (Project Properties-Build-Compiling)
		- tested on 7.1.1, 7.2-dev

- [javac-oo]
	- patched version of JavaC 1.7 for Operator Overloading support. If you need standalone javac compiler with OO.

- [eclipse-oo]
	- Eclipse [Java Developer Tools] fork for Operator Overloading.
	- use it if you need native **[Eclipse] IDE support**, or Eclipse Java Compiler support.

comming soon:

- eclipse-oo-plugin
	- [Eclipse] IDE and ECJ support via "plugin" similar to javac-oo-plugin.
- idea-oo-plugin
	- [Intellij Idea] support
- update [lombok-oo]
	- all-in-one solution bundled with great [lombok] plugin.

[Scala-like]: http://www.slideshare.net/joeygibson/operator-overloading-in-scala-2923973
[javac-oo]: https://bitbucket.org/amelentev/javac-oo
[lombok]: http://projectlombok.org/
[lombok-oo]: https://github.com/amelentev/lombok-oo
[eclipse]: http://eclipse.org/
[Java Developer Tools]: http://eclipse.org/jdt/
[Intellij Idea]: http://www.jetbrains.com/idea/
[eclipse-oo]: https://bitbucket.org/amelentev/eclipse.jdt.core-oo
