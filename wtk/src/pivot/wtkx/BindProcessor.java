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
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
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
            public JCTree.JCVariableDecl loadField = null;
            public ArrayList<JCTree.JCVariableDecl> bindFields = null;

            public LoadGroup(JCTree.JCVariableDecl loadField) {
                this.loadField = loadField;
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
                StringBuilder sourceCode = new StringBuilder("{");

                sourceCode.append("try {");
                sourceCode.append("super.bind();");
                sourceCode.append("} catch (Exception ex) {");
                sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                sourceCode.append("}");
                sourceCode.append("pivot.wtkx.WTKXSerializer wtkxSerializer;");
                sourceCode.append("Object value;");

                for (String loadFieldName : bindScope.loadGroups) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(loadFieldName);
                    JCTree.JCVariableDecl loadField = loadGroup.loadField;
                    Element loadElement = loadField.sym;
                    Bindable.Load loadAnnotation = loadElement.getAnnotation(Bindable.Load.class);

                    if (DEBUG) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                            String.format("Processing load(%s, %s#%s)", loadAnnotation.name(),
                            tree.name.toString(), loadFieldName));
                    }

                    // Load the WTKX resource
                    sourceCode.append("wtkxSerializer = new pivot.wtkx.WTKXSerializer();");
                    sourceCode.append(String.format("java.net.URL location = getClass().getResource(\"%s\");", loadAnnotation.name()));
                    sourceCode.append("try {");
                    sourceCode.append("value = wtkxSerializer.readObject(location);");
                    sourceCode.append("} catch (Exception ex) {");
                    sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                    sourceCode.append("}");

                    // Bind the resource to the field
                    sourceCode.append(String.format("%s = (%s)value;", loadFieldName, loadField.vartype.toString()));

                    // Bind the resource lookups to their corresponding fields
                    if (loadGroup.bindFields != null) {
                        for (JCTree.JCVariableDecl bindField : loadGroup.bindFields) {
                            String bindFieldName = bindField.name.toString();
                            Element bindElement = bindField.sym;
                            Bindable.Bind bindAnnotation = bindElement.getAnnotation(Bindable.Bind.class);

                            String bindID = bindAnnotation.id();
                            if ("\0".equals(bindID)) {
                                // The bind ID defaults to the field name
                                bindID = bindFieldName;
                            }

                            if (DEBUG) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                    String.format("Processing bind(%s.%s, %s#%s)", bindAnnotation.property(),
                                    bindID, tree.name.toString(), bindFieldName));
                            }

                            sourceCode.append(String.format("value = wtkxSerializer.getObjectByName(\"%s\");", bindID));
                            sourceCode.append("if (value == null) {");
                            sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindID));
                            sourceCode.append("}");
                            sourceCode.append(String.format("%s = (%s)value;", bindFieldName, bindField.vartype.toString()));
                        }
                    }
                }

                sourceCode.append("}");

                // Parse our source code into a AST block (bind() method body)
                Scanner.Factory scannerFactory = Scanner.Factory.instance(context);
                Parser.Factory parserFactory = Parser.Factory.instance(context);

                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCTree.JCBlock methodBody = parser.block();

                // Create the bind() override AST method declaration
                // TODO Declare it to throw IOException, then remove the
                // try/catch from super.bind()
                Type.MethodType methodType = new Type.MethodType(
                    List.<Type>nil(),             // Argument types
                    symbolTable.voidType,         // Return type
                    List.<Type>nil(),             // Throws types
                    symbolTable.methodClass       // Type symbol
                );
                Symbol.MethodSymbol methodSymbol = new Symbol.MethodSymbol(
                    Flags.PROTECTED,              // Flags
                    nameTable.fromString("bind"), // Name
                    methodType,                   // Type
                    tree.sym                      // Owner
                );
                JCTree.JCMethodDecl methodDeclaration = make.MethodDef(
                    methodSymbol,                 // Symbol
                    methodBody                    // Body
                );

                // Add the AST method declaration to the class
                tree.defs = tree.defs.prepend(methodDeclaration);
            }
        }

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
            Bindable.BindMethod bindMethod = methodElement.getAnnotation(Bindable.BindMethod.class);

            if (bindMethod != null) {
                // Remove the 'final' flag so that we may extend the method
                tree.sym.flags_field &= ~Flags.FINAL;
                tree.mods.flags &= ~Flags.FINAL;

                // Clear the method body so that it becomes a no-op
                tree.body.stats = List.<JCTree.JCStatement>nil();
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

            Bindable.Load loadAnnotation = null;
            Bindable.Bind bindAnnotation = null;

            Element element = tree.sym;
            if (element != null) {
                loadAnnotation = element.getAnnotation(Bindable.Load.class);
                bindAnnotation = element.getAnnotation(Bindable.Bind.class);
            } else if (tree.mods != null
                && tree.mods.annotations != null) {
                // TODO
                //List<JCTree.JCAnnotation> annotations = tree.mods.annotations;
            }

            if (loadAnnotation != null
                && bindAnnotation != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot combine " + Bindable.Load.class.getName()
                    + " and " + Bindable.Bind.class.getName() + " annotations.", element);
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

                if (bindScope.loadGroups != null
                    && bindScope.loadGroups.containsKey(bindAnnotation.property())) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(bindAnnotation.property());

                    if (loadGroup.bindFields == null) {
                        loadGroup.bindFields = new ArrayList<JCTree.JCVariableDecl>();
                    }

                    loadGroup.bindFields.add(tree);
                    bindTally++;
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Resource not found: " + bindAnnotation.property(), element);
                }
            }
        }
    }

    private int loadTally = 0;
    private int bindTally = 0;

    private Trees trees;
    private Context context;
    private TreeMaker make;
    private Name.Table nameTable;
    private Symtab symbolTable;
    private BindInjector bindInjector = new BindInjector();

    private static final boolean DEBUG = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        trees = Trees.instance(processingEnvironment);
        context = ((JavacProcessingEnvironment)processingEnvironment).getContext();
        make = TreeMaker.instance(context);
        symbolTable = Symtab.instance(context);
        nameTable = Name.Table.instance(context);
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
