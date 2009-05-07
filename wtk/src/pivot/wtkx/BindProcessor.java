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

import pivot.collections.ArrayList;
import pivot.collections.ArrayStack;
import pivot.collections.HashMap;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.source.util.Trees;

/**
 * Annotation processor that injects <tt>bind()</tt> overrides into classes
 * that use the <tt>@Load</tt> and <tt>@Bind</tt> annotations to perform WTKX
 * loading and binding.
 *
 * @author tvolkert
 */
@SupportedAnnotationTypes("pivot.wtkx.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindProcessor extends AbstractProcessor {
    /**
     * Holds pertinent information about a class' member variables that use
     * the <tt>@Load</tt> and <tt>@Bind</tt> annotations. A bind scope object
     * is pushed onto a stack before visiting a class and popped off the
     * stack after visiting it, allowing us to know if any members variables
     * contained in the class need bind processing as we're exiting the class.
     *
     * @author tvolkert
     */
    private static class BindScope {
        public static class LoadGroup {
            public JCTree.JCVariableDecl loadVariable = null;
            public ArrayList<JCTree.JCVariableDecl> bindFields = null;

            public LoadGroup(JCTree.JCVariableDecl loadVariable) {
                this.loadVariable = loadVariable;
            }
        }

        // Maps load field names to their corresponding load group
        public HashMap<String, LoadGroup> loadGroups = null;
    }

    /**
     * This actually does the work of bind method override injection.
     *
     * @author tvolkert
     */
    private class BindInjector extends TreeTranslator {
        private ArrayStack<BindScope> bindScopeStack = new ArrayStack<BindScope>();

        /**
         * Injects an override implementation of the <tt>bind()</tt> method
         * into the specified class if any member variables are found to be
         * annotated with the <tt>@Load</tt> or <tt>@Bind</tt> annotations.
         *
         * @param tree
         * The AST class declaration node
         */
        @Override
        public void visitClassDef(JCTree.JCClassDecl tree) {
            BindScope bindScope = new BindScope();

            bindScopeStack.push(bindScope);
            super.visitClassDef(tree);
            bindScopeStack.pop();

            if (bindScope.loadGroups != null) {
                StringBuilder sourceCode = new StringBuilder("class _A {");

                sourceCode.append("protected void bind(pivot.collections.Map<String,Object> namedObjects) {");
                sourceCode.append("super.bind(namedObjects);");
                sourceCode.append("pivot.wtkx.WTKXSerializer wtkxSerializer;");
                sourceCode.append("Object value;");
                sourceCode.append("java.net.URL location;");

                for (String loadVariableName : bindScope.loadGroups) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(loadVariableName);
                    JCTree.JCVariableDecl loadVariable = loadGroup.loadVariable;
                    JCTree.JCAnnotation loadAnnotation = getLoadAnnotation(loadVariable);

                    String loadName = getAnnotationProperty(loadAnnotation, "name");

                    if (DEBUG) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                            String.format("Processing load(%s, %s#%s)", loadName,
                            tree.name.toString(), loadVariableName));
                    }

                    // Load the WTKX resource
                    sourceCode.append("wtkxSerializer = new pivot.wtkx.WTKXSerializer();");
                    sourceCode.append(String.format("location = getClass().getResource(\"%s\");", loadName));
                    sourceCode.append("try {");
                    sourceCode.append("value = wtkxSerializer.readObject(location);");
                    sourceCode.append("} catch (Exception ex) {");
                    sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                    sourceCode.append("}");

                    // Bind the resource to the field
                    sourceCode.append(String.format("%s = (%s)value;", loadVariableName, loadVariable.vartype.toString()));

                    // Bind the resource lookups to their corresponding fields
                    if (loadGroup.bindFields != null) {
                        for (JCTree.JCVariableDecl bindField : loadGroup.bindFields) {
                            String bindFieldName = bindField.name.toString();
                            JCTree.JCAnnotation bindAnnotation = getBindAnnotation(bindField);

                            String bindName = getAnnotationProperty(bindAnnotation, "name");
                            if (bindName == null) {
                                // The bind name defaults to the field name
                                bindName = bindFieldName;
                            }

                            if (DEBUG) {
                                String property = getAnnotationProperty(bindAnnotation, "property");
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                    String.format("Processing bind(%s.%s, %s#%s)", property,
                                    bindName, tree.name.toString(), bindFieldName));
                            }

                            sourceCode.append(String.format("value = wtkxSerializer.getObjectByName(\"%s\");", bindName));
                            sourceCode.append("if (value == null) {");
                            sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindName));
                            sourceCode.append("}");
                            sourceCode.append(String.format("%s = (%s)value;", bindFieldName, bindField.vartype.toString()));
                        }
                    }
                }

                sourceCode.append("}");
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
         * Looks for the <tt>@Load</tt> and <tt>@Bind</tt> annotations on
         * member variable declarations. When found, it records pertinent
         * information in the current bind scope, to be used before we exit
         * the containing class.
         *
         * @param tree
         * The AST variable declaration node
         */
        @Override
        public void visitVarDef(JCTree.JCVariableDecl tree) {
            super.visitVarDef(tree);

            JCTree.JCAnnotation loadAnnotation = getLoadAnnotation(tree);
            JCTree.JCAnnotation bindAnnotation = getBindAnnotation(tree);

            if (loadAnnotation != null
                && bindAnnotation != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot combine " + Bindable.Load.class.getName()
                    + " and " + Bindable.Bind.class.getName() + " annotations.");
            } else if (loadAnnotation != null) {
                BindScope bindScope = bindScopeStack.peek();
                String fieldName = tree.name.toString();

                if (bindScope.loadGroups == null) {
                    bindScope.loadGroups = new HashMap<String, BindScope.LoadGroup>();
                }

                bindScope.loadGroups.put(fieldName, new BindScope.LoadGroup(tree));
                loadTally++;
            } else if (bindAnnotation != null) {
                BindScope bindScope = bindScopeStack.peek();
                String property = getAnnotationProperty(bindAnnotation, "property");

                if (bindScope.loadGroups != null
                    && bindScope.loadGroups.containsKey(property)) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(property);

                    if (loadGroup.bindFields == null) {
                        loadGroup.bindFields = new ArrayList<JCTree.JCVariableDecl>();
                    }

                    loadGroup.bindFields.add(tree);
                    bindTally++;
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Resource not found: " + property);
                }
            }
        }

        private JCTree.JCAnnotation getLoadAnnotation(JCTree.JCVariableDecl variableDeclaration) {
            return getAnnotation(variableDeclaration, Bindable.Load.class.getSimpleName());
        }

        private JCTree.JCAnnotation getBindAnnotation(JCTree.JCVariableDecl variableDeclaration) {
            return getAnnotation(variableDeclaration, Bindable.Bind.class.getSimpleName());
        }

        private JCTree.JCAnnotation getAnnotation(JCTree.JCVariableDecl variableDeclaration, String name) {
            JCTree.JCAnnotation result = null;

            if (variableDeclaration.mods != null
                && variableDeclaration.mods.annotations != null) {
                for (JCTree.JCAnnotation annotation : variableDeclaration.mods.annotations) {
                    JCTree.JCIdent identifier = (JCTree.JCIdent)annotation.annotationType;

                    if (identifier.name.contentEquals(name)) {
                        result = annotation;
                        break;
                    }
                }
            }

            return result;
        }

        private String getAnnotationProperty(JCTree.JCAnnotation annotation, String propertyName) {
            String result = null;

            for (JCTree.JCExpression arg : annotation.args) {
                JCTree.JCAssign assign = (JCTree.JCAssign)arg;
                JCTree.JCIdent key = (JCTree.JCIdent)assign.lhs;

                if (key.name.contentEquals(propertyName)) {
                    JCTree.JCLiteral value = (JCTree.JCLiteral)assign.rhs;
                    result = (String)value.value;
                    break;
                }
            }

            return result;
        }
    }

    private int loadTally = 0;
    private int bindTally = 0;

    private Trees trees;
    private Context context;
    private Scanner.Factory scannerFactory;
    private Parser.Factory parserFactory;
    private BindInjector bindInjector = new BindInjector();

    private static final boolean DEBUG = false;

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
            for (Element rootElement : roundEnvironment.getRootElements()) {
                if (rootElement.getKind() == ElementKind.CLASS) {
                    // Visit each AST class node with our BindInjector visitor
                    JCTree tree = (JCTree)trees.getTree(rootElement);
                    tree.accept(bindInjector);
                }
            }
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                String.format("%d WTKX %s processed (bound to %d %s).",
                loadTally, loadTally == 1 ? "load" : "loads",
                bindTally, bindTally == 1 ? "variable" : "variables"));
        }

        return true;
    }
}
