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
importPackage(java.lang);  // required to use System.out and System.err
importPackage(org.apache.pivot.util);  // required to use Pivot Utility class Console
importPackage(org.apache.pivot.wtk);   // required to use Pivot WTK classes

function alert1() {
    Alert.alert("Alert message", window);
}

function alert2() {
    Alert("Alert message").open(window);
}

function prompt1() {
    Prompt.prompt("Prompt message", window);
}

function prompt2() {
    Prompt("Prompt message").open(window);
}

function logDirectMessage() {
    System.out.println("Log message via direct call to System.out");
}

function logDirectError() {
    System.err.println("Log error via direct call to System.err");
}

function logConsoleMessage() {
    Console.log("Log message via Console");
}

function logConsoleError() {
    Console.logError("Log error via Console");
}

