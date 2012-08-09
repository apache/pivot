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


function log(msg) {
	if (msg == undefined || msg == null || typeof msg != "string")
		return ;

    System.out.println(msg);
}

function updateStatus(msg) {
	if (msg == undefined || msg == null || typeof msg != "string")
		return ;

	if (msg.length < 50)
		textStatus.text = msg;
}

function clearStatus() {
	updateStatus("");
}

function clearConsole() {
	templateButton.selectedIndex = 0;
	textArea.text = "";
	// runButton.enabled = false;  // ok
	runButton.setEnabled(false);   // explicit usage of the setter
	log("Console cleared");
	clearStatus();
}


function runConsole() {
	var text = textArea.text;
	log("Console Text length = " + text.length());
	if (text.length() < 1)
		return ;
	// else
	var msg = "Run JS Code in Console";
	log(msg);
	updateStatus(msg + " ...");

// TODO: continue here ...
	;
}

//TODO: temp ...
function openFrame(button) {
    testFrame.open(window);
    java.lang.System.out.println(label1.getText());
    java.lang.System.out.println(label2.getText());
}
