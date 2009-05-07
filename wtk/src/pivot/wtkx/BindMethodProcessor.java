/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtkx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.source.util.Trees;

/**
 * Annotation processor that suppresses the base {@link Bindable#bind()}
 * implementation such that it is not <tt>final</tt> and is a no-op, thus
 * paving the way for {@link BindProcessor} to process subclasses of
 * <tt>Bindable</tt>.
 *
 * @author tvolkert
 */
@SupportedAnnotationTypes("pivot.wtkx.BindMethodProcessor.BindMethod")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindMethodProcessor extends AbstractProcessor {
    /**
     * Flags the base implementation of the bind method. This cues the
     * annotation processor to remove the <tt>final</tt> flag from the method
     * so that it may be extended by the compiler and to remove the body of the
     * method, making it a no-op at runtime (since the annotation processor
     * inlines all WTKX binding).
     *
     * @author tvolkert
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    static @interface BindMethod {
    }

    /**
     * This actually does the work of bind method suppression.
     *
     * @author tvolkert
     */
    private class BindMethodSuppressor extends TreeTranslator {
        /**
         * Checks for the <tt>@BindMethod</tt> annotation on a method
         * (signalling the base class' implementation). When found, this strips
         * the method of its body and the <tt>final</tt> keyword, thus clearing
         * the way for us to override <tt>bind()</tt> in bindable subclasses
         * with inline implementations that can safely call
         * <tt>super.bind()</tt> without running the runtime bind
         * implementation.
         *
         * @param tree
         * The AST method declaration node
         */
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl tree) {
            super.visitMethodDef(tree);

            Element methodElement = tree.sym;
            if (methodElement != null) {
                BindMethod bindMethod = methodElement.getAnnotation(BindMethod.class);

                if (bindMethod != null) {
                    // Remove the 'final' flag so that we may extend the method
                    tree.sym.flags_field &= ~Flags.FINAL;
                    tree.mods.flags &= ~Flags.FINAL;

                    // Clear the method body so that it becomes a no-op
                    tree.body.stats = List.<JCTree.JCStatement>nil();

                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Suppressed Bindable#bind() base method");
                }
            }
        }
    }

    private Trees trees;
    private BindMethodSuppressor bindMethodSuppressor = new BindMethodSuppressor();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        trees = Trees.instance(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(BindMethod.class)) {
                if (annotatedElement.getKind() == ElementKind.METHOD) {
                    // Visit the AST method node with our BindMethodSuppressor visitor
                    JCTree tree = (JCTree)trees.getTree(annotatedElement);
                    tree.accept(bindMethodSuppressor);
                }
            }
        }

        return true;
    }
}
