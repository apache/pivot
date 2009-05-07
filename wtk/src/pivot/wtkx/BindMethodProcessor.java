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
import java.util.HashSet;
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

import pivot.collections.ArrayStack;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.source.util.Trees;

/**
 * Annotation processor that re-writes the base {@link Bindable#bind()}
 * implementation such that it calls into a newly defined <tt>protected
 * void bind(Map)</tt> method, thus paving the way for {@link BindProcessor} to
 * process subclasses of <tt>Bindable</tt>.
 *
 * @author tvolkert
 */
@SupportedAnnotationTypes("pivot.wtkx.BindMethodProcessor.BindMethod")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindMethodProcessor extends AbstractProcessor {
    /**
     * Flags the base implementation of the bind method. This cues the
     * annotation processor to re-write the body of the method.
     *
     * @author tvolkert
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    static @interface BindMethod {
    }

    /**
     * This actually does the work of bind method re-writing.
     *
     * @author tvolkert
     */
    private class BindMethodRewriter extends TreeTranslator {
        private ArrayStack<Boolean> stack = new ArrayStack<Boolean>();

        /**
         * Adds a bind overload signature (<tt>protected void bind(Map)</tt>)
         * to the class containing the base bind implementation.
         */
        @Override
        public void visitClassDef(JCTree.JCClassDecl tree) {
            stack.push(false);
            super.visitClassDef(tree);
            boolean addOverload = stack.pop();

            if (addOverload) {
                // Create source code containing out bind overload
                StringBuilder sourceCode = new StringBuilder();
                sourceCode.append("class _A {");
                sourceCode.append("protected void bind(pivot.collections.Map<String,Object> m) {}");
                sourceCode.append("}");

                // Parse the source code and extract the method declaration
                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCTree.JCCompilationUnit compilationUnit = parser.compilationUnit();
                JCTree.JCClassDecl classDeclaration = (JCTree.JCClassDecl)compilationUnit.defs.head;
                JCTree.JCMethodDecl methodDeclaration = (JCTree.JCMethodDecl)classDeclaration.defs.head;

                // Add the AST method declaration to our class
                tree.defs = tree.defs.prepend(methodDeclaration);
            }
        }

        /**
         * Checks for the <tt>@BindMethod</tt> annotation on a method
         * (signalling the base class' implementation). When found, this
         * re-writes the method's body such that it calls into the bind(Map)
         * overload (which will be defined in <tt>visitClassDef</tt>), thus
         * clearing the way for us to override <tt>bind(Map)</tt> in bindable
         * subclasses with inline implementations.
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
                    // Generate the re-written source code for bind()
                    StringBuilder sourceCode = new StringBuilder("{");
                    sourceCode.append("pivot.collections.HashMap<String, Object> m = ");
                    sourceCode.append("new pivot.collections.HashMap<String, Object>();");
                    sourceCode.append("bind(m);");
                    sourceCode.append("}");

                    // Parse the source code into a AST block
                    Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                    Parser parser = parserFactory.newParser(scanner, false, false);
                    JCTree.JCBlock methodBody = parser.block();

                    // Set the AST block as the body of the bind() method
                    tree.body = methodBody;

                    // Notify the stack that this is the correct level to add
                    // our bind overload
                    stack.poke(true);
                }
            }
        }
    }

    private Trees trees;
    private Context context;
    private Scanner.Factory scannerFactory;
    private Parser.Factory parserFactory;
    private BindMethodRewriter bindMethodRewriter = new BindMethodRewriter();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        trees = Trees.instance(processingEnvironment);
        context = ((JavacProcessingEnvironment)processingEnvironment).getContext();
        scannerFactory = Scanner.Factory.instance(context);
        parserFactory = Parser.Factory.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            Set<Element> classElements = new HashSet<Element>();

            for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(BindMethod.class)) {
                if (annotatedElement.getKind() == ElementKind.METHOD) {
                    Element classElement = annotatedElement;
                    while (classElement != null
                        && classElement.getKind() != ElementKind.CLASS) {
                        classElement = classElement.getEnclosingElement();
                    }

                    if (classElement != null) {
                        classElements.add(classElement);
                    }
                }
            }

            for (Element classElement : classElements) {
                // Visit the AST class node with our BindMethodRewriter visitor
                JCTree tree = (JCTree)trees.getTree(classElement);
                tree.accept(bindMethodRewriter);
            }
        }

        return true;
    }
}
