package javaoo.eclipse;

import java.io.File;
import java.io.PrintWriter;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class AspectTest {
	static String sampledir = "../javac-oo/sample/";
	public static void compile(String file) {
		System.out.print("Compiling " + file + ": ");
		boolean res = BatchCompiler.compile(sampledir + file + " -source 1.7", new PrintWriter(System.out), new PrintWriter(System.err), null);
		System.out.println(res ? "ok" : "fail");
	}
	public static void main(String[] args) {
		System.out.println(new File(".").getAbsolutePath());
		compile("MathTest.java");
		compile("CmpTest.java");
		/*compile("ListIndexGetTest.java");
		compile("ListIndexSetTest.java");
		compile("MapIndexTest.java");*/
	}
}
