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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree.*;
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
            public JCVariableDecl loadVariableDeclaration = null;
            public ArrayList<JCVariableDecl> bindVariableDeclarations = null;

            private LoadGroup(JCVariableDecl loadVariableDeclaration) {
                this.loadVariableDeclaration = loadVariableDeclaration;
            }
        }

        // Maps load field names to their corresponding load group
        public HashMap<String, LoadGroup> loadGroups = null;

        // Bind fields whose load field hasn't been seen yet
        public ArrayList<JCVariableDecl> strandedBindVariableDeclarations = null;

        /**
         * Creates a load group for the specified load field.
         *
         * @param loadVariableDeclaration
         * The AST load variable declaration node
         */
        public void createLoadGroup(JCVariableDecl loadVariableDeclaration) {
            if (loadGroups == null) {
                // Lazily create the load groups map
                loadGroups = new HashMap<String, LoadGroup>();
            }

            // Create a new load group for this load field
            String loadFieldName = loadVariableDeclaration.name.toString();
            loadGroups.put(loadFieldName, new LoadGroup(loadVariableDeclaration));
        }

        /**
         * Adds the specified bind field to its appropriate load group, if one
         * exists. If it is bound to a property that has not yet been
         * encountered by the bind injector visitor, it is added to the
         * stranded list.
         *
         * @param loadVariableDeclaration
         * The AST load variable declaration node
         */
        public void addToLoadGroup(JCVariableDecl bindVariableDeclaration) {
            addToLoadGroup(bindVariableDeclaration, true);
        }

        /**
         * Adds the specified bind field to its appropriate load group, if one
         * exists. If it is bound to a property that has not yet been
         * encountered by the bind injector visitor, and <tt>recordStranded</tt>
         * is <tt>true</tt>, then it is added to the stranded list.
         *
         * @param loadVariableDeclaration
         * The AST load variable declaration node
         *
         * @param recordStranded
         * <tt>true</tt> to add group-less fields to the stranded list
         *
         * @return
         * <tt>true</tt> if the field was added to a load group; <tt>false</tt>
         * if it was not
         */
        private boolean addToLoadGroup(JCVariableDecl bindVariableDeclaration, boolean recordStranded) {
            boolean added = false;

            JCAnnotation bindAnnotation = getBindAnnotation(bindVariableDeclaration);
            String loadFieldName = getAnnotationProperty(bindAnnotation, "property");

            if (loadGroups != null
                && loadGroups.containsKey(loadFieldName)) {
                added = true;
                LoadGroup loadGroup = loadGroups.get(loadFieldName);

                if (loadGroup.bindVariableDeclarations == null) {
                    // Lazily create the bind fields list
                    loadGroup.bindVariableDeclarations = new ArrayList<JCVariableDecl>();
                }

                // Add this bind field to its load group
                loadGroup.bindVariableDeclarations.add(bindVariableDeclaration);
            }

            if (!added && recordStranded) {
                if (strandedBindVariableDeclarations == null) {
                    // Lazily create the stranded list
                    strandedBindVariableDeclarations = new ArrayList<JCVariableDecl>();
                }

                strandedBindVariableDeclarations.add(bindVariableDeclaration);
            }

            return added;
        }

        /**
         * Attempts to find a load group for all stranded bind variable
         * declarations. Those that are found to have a load group will be
         * placed in the load group and removed from the stranded list. Those
         * left in the stranded list are assumed to be binding to a superclass'
         * load field.
         */
        public void resolve() {
            if (strandedBindVariableDeclarations != null) {
                for (int i = strandedBindVariableDeclarations.getLength() - 1; i >= 0; i--) {
                    if (addToLoadGroup(strandedBindVariableDeclarations.get(i), false)) {
                        // Remove it from the stranded list
                        strandedBindVariableDeclarations.remove(i, 1);
                    }
                }
            }
        }
    }

    /**
     * This actually does the work of bind method override injection.
     *
     * @author tvolkert
     */
    private class BindInjector extends TreeTranslator {
        private ArrayStack<BindScope> bindScopeStack = new ArrayStack<BindScope>();

        /**
         * Injects an override implementation of the <tt>bind(Map)</tt> method
         * into the specified class if any member variables are found to be
         * annotated with the <tt>@Load</tt> or <tt>@Bind</tt> annotations.
         *
         * @param tree
         * The AST class declaration node
         */
        @Override
        public void visitClassDef(JCClassDecl tree) {
            BindScope bindScope = new BindScope();

            bindScopeStack.push(bindScope);
            super.visitClassDef(tree);
            bindScopeStack.pop();

            bindScope.resolve();
            if (bindScope.loadGroups != null
                || bindScope.strandedBindVariableDeclarations != null) {
                // There is some bind work to be done in this class; start by
                // creating the source code buffer
                StringBuilder sourceCode = new StringBuilder("class _A {");
                sourceCode.append("@Override ");
                sourceCode.append("protected void __bind__(pivot.collections.Map<String,pivot.wtkx.WTKXSerializer> namedSerializers) {");
                sourceCode.append("super.__bind__(namedSerializers);");

                // Local variable declarations
                sourceCode.append("pivot.wtkx.WTKXSerializer wtkxSerializer;");
                sourceCode.append("Object object;");
                sourceCode.append("java.net.URL location;");

                if (bindScope.loadGroups != null) {
                    for (String loadFieldName : bindScope.loadGroups) {
                        BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(loadFieldName);
                        JCVariableDecl loadVariableDeclaration = loadGroup.loadVariableDeclaration;
                        JCAnnotation loadAnnotation = getLoadAnnotation(loadVariableDeclaration);
                        String resourceName = getAnnotationProperty(loadAnnotation, "name");

                        if (DEBUG) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                String.format("Processing load(%s, %s#%s)", resourceName,
                                tree.name.toString(), loadFieldName));
                        }

                        // Load the WTKX resource
                        sourceCode.append("wtkxSerializer = new pivot.wtkx.WTKXSerializer();");
                        sourceCode.append(String.format("location = getClass().getResource(\"%s\");", resourceName));
                        sourceCode.append("try {");
                        sourceCode.append("object = wtkxSerializer.readObject(location);");
                        sourceCode.append("} catch (Exception ex) {");
                        sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                        sourceCode.append("}");

                        // Bind the resource to the field
                        sourceCode.append(String.format("%s = (%s)object;", loadFieldName,
                            loadVariableDeclaration.vartype.toString()));

                        // Public and protected fields get kept for subclasses
                        if ((loadVariableDeclaration.mods.flags & (Flags.PUBLIC | Flags.PROTECTED)) != 0) {
                            sourceCode.append(String.format("namedSerializers.put(\"%s\", wtkxSerializer);",
                                loadFieldName));
                        }

                        // Bind the resource lookups to their corresponding fields
                        if (loadGroup.bindVariableDeclarations != null) {
                            for (JCVariableDecl bindVariableDeclaration : loadGroup.bindVariableDeclarations) {
                                String bindFieldName = bindVariableDeclaration.name.toString();
                                JCAnnotation bindAnnotation = getBindAnnotation(bindVariableDeclaration);

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

                                sourceCode.append(String.format("object = wtkxSerializer.getObjectByName(\"%s\");",
                                    bindName));
                                sourceCode.append("if (object == null) {");
                                sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindName));
                                sourceCode.append("}");
                                sourceCode.append(String.format("%s = (%s)object;", bindFieldName,
                                    bindVariableDeclaration.vartype.toString()));
                            }
                        }
                    }
                }

                if (bindScope.strandedBindVariableDeclarations != null) {
                    for (JCVariableDecl bindVariableDeclaration : bindScope.strandedBindVariableDeclarations) {
                        String bindFieldName = bindVariableDeclaration.name.toString();
                        JCAnnotation bindAnnotation = getBindAnnotation(bindVariableDeclaration);
                        String loadFieldName = getAnnotationProperty(bindAnnotation, "property");

                        String bindName = getAnnotationProperty(bindAnnotation, "name");
                        if (bindName == null) {
                            // The bind name defaults to the field name
                            bindName = bindFieldName;
                        }

                        sourceCode.append(String.format("wtkxSerializer = namedSerializers.get(\"%s\");",
                            loadFieldName));

                        sourceCode.append("if (wtkxSerializer == null) {");
                        sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Property not found: %s.\");", loadFieldName));
                        sourceCode.append("}");

                        sourceCode.append(String.format("object = wtkxSerializer.getObjectByName(\"%s\");",
                            bindName));
                        sourceCode.append("if (object == null) {");
                        sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindName));
                        sourceCode.append("}");
                        sourceCode.append(String.format("%s = (%s)object;", bindFieldName,
                            bindVariableDeclaration.vartype.toString()));
                    }
                }

                sourceCode.append("}");
                sourceCode.append("}");

                // Parse the source code and extract the method declaration
                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCCompilationUnit compilationUnit = parser.compilationUnit();
                JCClassDecl classDeclaration = (JCClassDecl)compilationUnit.defs.head;
                JCMethodDecl methodDeclaration = (JCMethodDecl)classDeclaration.defs.head;

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
        public void visitVarDef(JCVariableDecl variableDeclaration) {
            super.visitVarDef(variableDeclaration);

            JCAnnotation loadAnnotation = getLoadAnnotation(variableDeclaration);
            JCAnnotation bindAnnotation = getBindAnnotation(variableDeclaration);

            if (loadAnnotation != null
                && bindAnnotation != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot combine " + Bindable.Load.class.getName()
                    + " and " + Bindable.Bind.class.getName() + " annotations.");
            } else if (loadAnnotation != null) {
                BindScope bindScope = bindScopeStack.peek();
                bindScope.createLoadGroup(variableDeclaration);

                // Increment the tally for reporting purposes
                loadTally++;
            } else if (bindAnnotation != null) {
                BindScope bindScope = bindScopeStack.peek();
                bindScope.addToLoadGroup(variableDeclaration);

                // Increment the tally for reporting purposes
                bindTally++;
            }
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
                    JCClassDecl classDeclaration = (JCClassDecl)trees.getTree(rootElement);
                    classDeclaration.accept(bindInjector);
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

    /**
     * Gets the <tt>Load</tt> AST annotation node that's associated with
     * the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getLoadAnnotation(JCVariableDecl variableDeclaration) {
        return getAnnotation(variableDeclaration, Bindable.Load.class.getSimpleName());
    }

    /**
     * Gets the <tt>Bind</tt> AST annotation node that's associated with
     * the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getBindAnnotation(JCVariableDecl variableDeclaration) {
        return getAnnotation(variableDeclaration, Bindable.Bind.class.getSimpleName());
    }

    /**
     * Gets the AST annotation node with the given name that's associated
     * with the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @param name
     * The simple (unqualified) name of the annotation
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getAnnotation(JCVariableDecl variableDeclaration, String name) {
        JCAnnotation result = null;

        if (variableDeclaration.mods != null
            && variableDeclaration.mods.annotations != null) {
            for (JCAnnotation annotation : variableDeclaration.mods.annotations) {
                JCIdent identifier = (JCIdent)annotation.annotationType;

                if (identifier.name.contentEquals(name)) {
                    result = annotation;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the given property's value within the specified AST annotation
     * node.
     *
     * @param annotation
     * The AST annotation node
     *
     * @param propertyName
     * The name of the property to retrieve
     *
     * @return
     * The value of the property, or <tt>null</tt> if it is not explicitly
     * set in the annotation
     */
    private static String getAnnotationProperty(JCAnnotation annotation, String propertyName) {
        String result = null;

        for (JCExpression arg : annotation.args) {
            JCAssign assign = (JCAssign)arg;
            JCIdent key = (JCIdent)assign.lhs;

            if (key.name.contentEquals(propertyName)) {
                JCLiteral value = (JCLiteral)assign.rhs;
                result = (String)value.value;
                break;
            }
        }

        return result;
    }
}
