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
package org.apache.pivot.wtkx.test;

import java.io.InputStream;
import java.util.ArrayList;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.pivot.wtkx.Translator;


/**
 */
public class JavaTranslatorTest {
   public static void main(String[] args) throws Exception {
       Translator translator = new Translator();

       InputStream inputStream = JavaTranslatorTest.class.getResourceAsStream
           ("java_translator_test.wtkx");

       JavaFileObject javaFileObject = translator.translate(inputStream,
           "org.apache.pivot.wtkx.test.java_translator_test_WTKX");

       System.out.println(javaFileObject.getCharContent(true));

       ArrayList<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>(1);
       javaFileObjects.add(javaFileObject);

       JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
       JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, javaFileObjects);

       task.call();
       System.out.println("Class compiled successfully");

       javaFileObject.delete();
   }
}
