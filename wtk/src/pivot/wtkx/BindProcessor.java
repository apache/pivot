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

import pivot.collections.ArrayStack;
import pivot.collections.HashMap;

//import com.sun.tools.javac.processing.JavacProcessingEnvironment;
//import com.sun.tools.javac.code.Flags;
//import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
//import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
//import com.sun.tools.javac.util.Context;
//import com.sun.tools.javac.util.List;
//import com.sun.tools.javac.util.Name;
import com.sun.source.util.Trees;

@SupportedAnnotationTypes("pivot.wtkx.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindProcessor extends AbstractProcessor {
    private static class BindScope {
        public HashMap<String, Load> loadAnnotations = null;
        public HashMap<String, Bind> bindAnnotations = null;
    }

    private class BindTranslator extends TreeTranslator {
        private ArrayStack<BindScope> bindScopeStack = new ArrayStack<BindScope>();

        @Override
        public void visitClassDef(JCTree.JCClassDecl tree) {
            BindScope bindScope = new BindScope();

            bindScopeStack.push(bindScope);
            super.visitClassDef(tree);
            bindScopeStack.pop();

            if (bindScope.loadAnnotations != null) {
                for (String fieldName : bindScope.loadAnnotations) {
                    Load loadAnnotation = bindScope.loadAnnotations.get(fieldName);

                    if (DEBUG) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                            String.format("Processing load(%s, %s#%s)", loadAnnotation.name(),
                            tree.name.toString(), fieldName));
                    }

                    // TODO
                }
            }

            if (bindScope.bindAnnotations != null) {
                for (String fieldName : bindScope.bindAnnotations) {
                    Bind bindAnnotation = bindScope.bindAnnotations.get(fieldName);

                    if (DEBUG) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                            String.format("Processing bind(%s.%s, %s#%s)", bindAnnotation.resource(),
                                bindAnnotation.id(), tree.name.toString(), fieldName));
                    }

                    // TODO
                }
            }
        }

        @Override
        public void visitVarDef(JCTree.JCVariableDecl tree) {
            super.visitVarDef(tree);

            Element element = tree.sym;

            if (element != null) {
                String fieldName = tree.name.toString();

                Load loadAnnotation = element.getAnnotation(Load.class);
                Bind bindAnnotation = element.getAnnotation(Bind.class);

                if (loadAnnotation != null
                    && bindAnnotation != null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Cannot combine " + Load.class.getName()
                        + " and " + Bind.class.getName() + " annotations.", element);
                } else if (loadAnnotation != null) {
                    BindScope bindScope = bindScopeStack.peek();

                    if (bindScope.loadAnnotations == null) {
                        bindScope.loadAnnotations = new HashMap<String, Load>();
                    }

                    bindScope.loadAnnotations.put(fieldName, loadAnnotation);
                    loadTally++;
                } else if (bindAnnotation != null) {
                    BindScope bindScope = bindScopeStack.peek();

                    if (bindScope.bindAnnotations == null) {
                        bindScope.bindAnnotations = new HashMap<String, Bind>();
                    }

                    bindScope.bindAnnotations.put(fieldName, bindAnnotation);
                    bindTally++;
                }
            }
        }

        // TODO remove.  This is reference code
        /*
        @Override
        public void visitAssert(JCTree.JCAssert tree) {
            super.visitAssert(tree);
            JCTree.JCStatement newNode = makeIfThrowException(tree);
            result = newNode;
            //tally++;
        }

        private JCTree.JCStatement makeIfThrowException(JCTree.JCAssert node) {
            // make: if (!(condition) throw new AssertionError(detail);
            List<JCTree.JCExpression> args = (node.getDetail() == null
                ? List.<JCTree.JCExpression> nil()
                : List.of(node.detail));
            JCTree.JCExpression expr = treeMaker.NewClass(null,
                                              null,
                                              treeMaker.Ident(names.fromString("AssertionError")),
                                              args,
                                              null);

            return treeMaker.If(treeMaker.Unary(JCTree.NOT, node.cond),
                           treeMaker.Throw(expr),
                           null);

        }
        */
    }

    private int loadTally;
    private int bindTally;

    private Trees trees;
    //private TreeMaker treeMaker;
    //private Name.Table names;

    private static final boolean DEBUG = true;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        trees = Trees.instance(env);
        //Context context = ((JavacProcessingEnvironment)env).getContext();
        //treeMaker = TreeMaker.instance(context);
        //names = Name.Table.instance(context);
        loadTally = 0;
        bindTally = 0;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        boolean claimAnnotations = false;

        if (!roundEnvironment.processingOver()) {
            claimAnnotations = true;

            BindTranslator bindTranslator = new BindTranslator();

            for (Element rootElement : roundEnvironment.getRootElements()) {
                if (rootElement.getKind() == ElementKind.CLASS) {
                    // Visit each Class tree with our bindTranslator visitor
                    JCTree tree = (JCTree)trees.getTree(rootElement);
                    tree.accept(bindTranslator);
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
