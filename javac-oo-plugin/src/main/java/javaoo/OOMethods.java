/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU Public License v2.0 + OpenJDK assembly exception.
 *
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com>
 ******************************************************************************/
package javaoo;

import java.util.HashMap;
import java.util.Map;

/** Caution this file is symlinked in all plugins */
public interface OOMethods {
    Map<String, String> binary = new HashMap<String, String>() {{
        put("+",    "add");
        put("-",    "subtract");
        put("*",    "multiply");
        put("/",    "divide");
        put("%",    "remainder");
        put("&",    "and");
        put("|",    "or");
        put("^",    "xor");
        put("<<",   "shiftLeft");
        put(">>",   "shiftRight");
        put("<",    compareTo);
        put(">",    compareTo);
        put("<=",   compareTo);
        put(">=",   compareTo);
    }};
    Map<String, String> unary = new java.util.HashMap<String, String>() {{
        put("-", "negate");
        put("~", "not");
    }};
    String   compareTo = "compareTo";
    String   indexGet = "get";
    String[] indexSet = new String[]{"set", "put"};
    String   valueOf = "valueOf";
}
