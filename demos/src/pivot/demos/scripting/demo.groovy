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
import pivot.collections.adapter.*
import pivot.wtk.*

foo = "ABCDE"

def doSomething(button) {
    Alert.alert("You clicked me!", button.getWindow())
}

public class MyButtonPressListener1 implements ButtonPressListener {
    private Script script;
    
    public MyButtonPressListener1(Script script) {
        this.script = script
    }
    
    public void buttonPressed(Button button) {
        script.doSomething(button)
    }
}

buttonPressListener1 = new MyButtonPressListener1(this)

public class MyButtonPressListener2 implements ButtonPressListener {
    public void buttonPressed(Button button) {
        System.out.println("[Groovy] A button was clicked.");
    }
}

buttonPressListener2 = new MyButtonPressListener2()

listData = []
listData << "One"
listData << "Two"
listData << "Three"

listData = new ListAdapter(listData)
