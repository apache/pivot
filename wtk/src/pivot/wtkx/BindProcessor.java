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
import com.sun.tools.javac.util.List;
import com.sun.source.util.Trees;

/**
 * Annotation processor that injects instance initializers into classes that
 * use the <tt>@Load</tt> and <tt>@Bind</tt> annotations to perform the loading
 * and binding.
 *
 * @author tvolkert
 */
@SupportedAnnotationTypes("pivot.wtkx.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindProcessor extends AbstractProcessor {
    /**
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
     * This actually does the work of instance initializer injection.
     *
     * @author tvolkert
     */
    private class BindInjector extends TreeTranslator {
        private ArrayStack<BindScope> bindScopeStack = new ArrayStack<BindScope>();

        @Override
        public void visitClassDef(JCTree.JCClassDecl tree) {
            BindScope bindScope = new BindScope();

            bindScopeStack.push(bindScope);
            super.visitClassDef(tree);
            bindScopeStack.pop();

            if (bindScope.loadGroups != null) {
                StringBuilder sourceCode = new StringBuilder("{");

                sourceCode.append("pivot.wtkx.WTKXSerializer wtkxSerializer;");
                sourceCode.append("Object resource;");
                sourceCode.append("Object value;");

                for (String loadFieldName : bindScope.loadGroups) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(loadFieldName);
                    JCTree.JCVariableDecl loadField = loadGroup.loadField;
                    Element loadElement = loadField.sym;
                    Load loadAnnotation = loadElement.getAnnotation(Load.class);

                    if (DEBUG) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                            String.format("Processing load(%s, %s#%s)", loadAnnotation.value(),
                            tree.name.toString(), loadFieldName));
                    }

                    // Load the WTKX resource
                    sourceCode.append("wtkxSerializer = new pivot.wtkx.WTKXSerializer();");
                    sourceCode.append(String.format("java.net.URL location = getClass().getResource(\"%s\");", loadAnnotation.value()));
                    sourceCode.append("try {");
                    sourceCode.append("resource = wtkxSerializer.readObject(location);");
                    sourceCode.append("} catch (Exception ex) {");
                    sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                    sourceCode.append("}");

                    // Bind the resource to the field
                    sourceCode.append(String.format("%s = (%s)resource;", loadFieldName, loadField.vartype.toString()));

                    // Bind the resource lookups to their corresponding fields
                    if (loadGroup.bindFields != null) {
                        for (JCTree.JCVariableDecl bindField : loadGroup.bindFields) {
                            String bindFieldName = bindField.name.toString();
                            Element bindElement = bindField.sym;
                            Bind bindAnnotation = bindElement.getAnnotation(Bind.class);

                            String bindID = bindAnnotation.id();
                            if ("\0".equals(bindID)) {
                                // The bind ID defaults to the field name
                                bindID = bindFieldName;
                            }

                            if (DEBUG) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                    String.format("Processing bind(%s.%s, %s#%s)", bindAnnotation.resource(),
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

                // Parse our source code into a AST block
                Scanner.Factory scannerFactory = Scanner.Factory.instance(context);
                Parser.Factory parserFactory = Parser.Factory.instance(context);

                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCTree.JCBlock block = parser.block();

                // Add the AST block (instance initializer) to the class
                tree.defs = tree.defs.prepend(block);
            }
        }

        @Override
        public void visitVarDef(JCTree.JCVariableDecl tree) {
            super.visitVarDef(tree);

            Load loadAnnotation = null;
            Bind bindAnnotation = null;

            Element element = tree.sym;
            if (element != null) {
                loadAnnotation = element.getAnnotation(Load.class);
                bindAnnotation = element.getAnnotation(Bind.class);
            } else if (tree.mods != null
                && tree.mods.annotations != null) {
                List<JCTree.JCAnnotation> annotations = tree.mods.annotations;
                // TODO
            }

            if (loadAnnotation != null
                && bindAnnotation != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot combine " + Load.class.getName()
                    + " and " + Bind.class.getName() + " annotations.", element);
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
                    && bindScope.loadGroups.containsKey(bindAnnotation.resource())) {
                    BindScope.LoadGroup loadGroup = bindScope.loadGroups.get(bindAnnotation.resource());

                    if (loadGroup.bindFields == null) {
                        loadGroup.bindFields = new ArrayList<JCTree.JCVariableDecl>();
                    }

                    loadGroup.bindFields.add(tree);
                    bindTally++;
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Resource not found: " + bindAnnotation.resource(), element);
                }
            }
        }
    }

    private int loadTally = 0;
    private int bindTally = 0;

    private Trees trees;
    private Context context;

    private static final boolean DEBUG = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        trees = Trees.instance(processingEnvironment);
        context = ((JavacProcessingEnvironment)processingEnvironment).getContext();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        boolean claimAnnotations = false;

        if (!roundEnvironment.processingOver()) {
            claimAnnotations = true;

            BindInjector bindInjector = new BindInjector();

            for (Element rootElement : roundEnvironment.getRootElements()) {
                if (rootElement.getKind() == ElementKind.CLASS) {
                    // Visit each Class tree with our bindInjector visitor
                    JCTree tree = (JCTree)trees.getTree(rootElement);
                    tree.accept(bindInjector);
                }
            }
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                String.format("%d WTKX %s processed (bound to %d %s).",
                loadTally, loadTally == 1 ? "load" : "loads", bindTally,
                bindTally == 1 ? "variable" : "variables"));
        }

        return claimAnnotations;
    }
}
