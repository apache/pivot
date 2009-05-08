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
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.source.util.Trees;

/**
 * Annotation processor that re-writes the base {@link Bindable#bind()}
 * implementation such that it calls into a newly defined <tt>protected
 * void __bind(Map)</tt> method, thus paving the way for {@link BindProcessor}
 * to process subclasses of <tt>Bindable</tt>.
 * <p>
 * Note that this class works in close tandem with <tt>BindProcessor</tt> and
 * <tt>Bindable</tt> in that they share a mutual contract.
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

    static final String BIND_OVERLOAD_NAME = "__bind";

    /**
     * This actually does the work of overloaded bind method injection.
     *
     * @author tvolkert
     */
    private class BindOverloadInjector extends TreeTranslator {
        private ArrayStack<Boolean> stack = new ArrayStack<Boolean>();

        /**
         * Adds a bind overload signature (<tt>protected void __bind(Map)</tt>)
         * to the class containing the base bind implementation.
         *
         * @param classDeclaration
         * The AST class declaration node
         */
        @Override
        public void visitClassDef(JCClassDecl classDeclaration) {
            stack.push(false);
            super.visitClassDef(classDeclaration);
            boolean addOverload = stack.pop();

            if (addOverload) {
                // Create source code containing out bind overload
                StringBuilder sourceCode = new StringBuilder();
                sourceCode.append("class _A {");
                sourceCode.append("protected void ");
                sourceCode.append(BIND_OVERLOAD_NAME);
                sourceCode.append("(pivot.collections.Map<String,pivot.wtkx.WTKXSerializer> m) {}");
                sourceCode.append("}");

                // Parse the source code and extract the method declaration
                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCCompilationUnit parsedCompilationUnit = parser.compilationUnit();
                JCClassDecl parsedClassDeclaration = (JCClassDecl)parsedCompilationUnit.defs.head;
                JCMethodDecl parsedMethodDeclaration = (JCMethodDecl)parsedClassDeclaration.defs.head;

                // Add the AST method declaration to our class
                classDeclaration.defs = classDeclaration.defs.prepend(parsedMethodDeclaration);
            }
        }

        /**
         * Checks for the <tt>@BindMethod</tt> annotation on a method
         * (signalling the base class' implementation), and marks the current
         * stack frame when it finds the annotation.
         *
         * @param methodDeclaration
         * The AST method declaration node
         */
        @Override
        public void visitMethodDef(JCMethodDecl methodDeclaration) {
            super.visitMethodDef(methodDeclaration);

            Element methodElement = methodDeclaration.sym;
            if (methodElement != null) {
                if (methodElement.getAnnotation(BindMethod.class) != null) {
                    stack.poke(true);
                }
            }
        }
    }

    private Trees trees;
    private Context context;
    private Scanner.Factory scannerFactory;
    private Parser.Factory parserFactory;
    private BindOverloadInjector bindOverloadInjector = new BindOverloadInjector();

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
                // Visit the AST class node with our BindOverloadInjector visitor
                JCClassDecl classDeclaration = (JCClassDecl)trees.getTree(classElement);
                classDeclaration.accept(bindOverloadInjector);
            }
        }

        return true;
    }
}
