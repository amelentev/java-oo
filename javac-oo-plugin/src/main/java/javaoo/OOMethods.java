/* Copyright 2013 Artem Melentyev <amelentev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        put("-", "negate");     // jdk7
        put("---", "negate");   // jdk8
        put("~", "not");
    }};
    String   compareTo = "compareTo";
    String   indexGet = "get";
    String[] indexSet = new String[]{"set", "put"};
    String   valueOf = "valueOf";
}
