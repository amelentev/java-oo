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
package javaoo.idea;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Util {
    private Util() {}

    private static @NotNull Field findField(Class clas, String... fields) {
        for (String field : fields) {
            try {
                Field f = clas.getDeclaredField(field);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                // continue
            }
        }
        throw new RuntimeException(String.format("Can't find %s fields in %s class", Arrays.toString(fields), clas.getName()));
    }

    public static <T> Object get(Class<T> clas, T obj, String... fields) {
        try {
            return findField(clas, fields).get(obj);
        } catch (Exception e) {
            throw sneakyThrow(e);
        }
    }

    public static void set(Class<?> clas, Object obj, Object val, String... fields) {
        try {
            findField(clas, fields).set(obj, val);
        } catch (Exception e) {
            throw sneakyThrow(e);
        }
    }
    public static void setJavaElementConstructor(IElementType et, Class<? extends ASTNode> clas) {
        try {
            set(JavaElementType.JavaCompositeElementType.class, et, clas.getConstructor(), "myConstructor", "a");
        } catch (NoSuchMethodException e) {
            throw sneakyThrow(e);
        }
    }
    public static RuntimeException sneakyThrow(Throwable ex) {
        return Util.sneakyThrowInner(ex);
    }
    private static <T extends Throwable> T sneakyThrowInner(Throwable ex) throws T {
        throw (T) ex;
    }
}