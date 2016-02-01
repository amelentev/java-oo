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

import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.impl.ExtensionComponentAdapter;
import com.intellij.openapi.extensions.impl.ExtensionPointImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.impl.source.tree.java.PsiArrayAccessExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPrefixExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OOComponent implements ProjectComponent {
    private static final Logger LOG = Logger.getInstance("#"+OOComponent.class.getName());

    private final Project project;
    private final ClassLoaderInjector classLoaderInjector = new ClassLoaderInjector();

    public OOComponent(Project project) {
        this.project = project;
    }

    @NotNull @Override
    public String getComponentName() {
        return "Java Operator Overloading plugin";
    }

    @Override
    public void initComponent() {
        LOG.info("OO init");
        Util.setJavaElementConstructor(JavaElementType.BINARY_EXPRESSION, PsiOOBinaryExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.PREFIX_EXPRESSION, PsiOOPrefixExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.POLYADIC_EXPRESSION, PsiOOPolyadicExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.ARRAY_ACCESS_EXPRESSION, PsiOOArrayAccessExpressionImpl.class);

        ExtensionPoint<HighlightVisitor> ep = Extensions.getArea(project).getExtensionPoint(HighlightVisitor.EP_HIGHLIGHT_VISITOR);
        Set<ExtensionComponentAdapter> hadapters = (Set<ExtensionComponentAdapter>) Util.get(ExtensionPointImpl.class, (ExtensionPointImpl<HighlightVisitor>) ep, Set.class, "myExtensionAdapters");
        for (ExtensionComponentAdapter ca : hadapters) {
            if (HighlightVisitorImpl.class.getName().equals(ca.getAssignableToClassName())) {
                try {
                    Class ooHighlightVisitorClass = classLoaderInjector.injectOOHighlightVisitorImplClass(HighlightVisitorImpl.class.getClassLoader());
                    Util.set(ExtensionComponentAdapter.class, ca, Class.class, ooHighlightVisitorClass, "myImplementationClass");
                } catch (Exception e) {
                    LOG.error("Can't load transformed OOHighlightVisitorImpl class", e);
                }
                break;
            }
        }
    }

    @Override
    public void disposeComponent() {
        LOG.info("OO dispose");
        Util.setJavaElementConstructor(JavaElementType.BINARY_EXPRESSION, PsiBinaryExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.PREFIX_EXPRESSION, PsiPrefixExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.POLYADIC_EXPRESSION, PsiPolyadicExpressionImpl.class);
        Util.setJavaElementConstructor(JavaElementType.ARRAY_ACCESS_EXPRESSION, PsiArrayAccessExpressionImpl.class);
        classLoaderInjector.injectedClasses.clear();
    }

    public void projectOpened() {}
    public void projectClosed() {}
}
