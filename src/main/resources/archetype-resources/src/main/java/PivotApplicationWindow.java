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
package ${package};

import org.apache.pivot.wtk.*;
import org.apache.pivot.util.Resources;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.collections.Map;
import java.net.URL;

public class PivotApplicationWindow extends Window implements Bindable  {
    @BXML private PushButton sayHelloButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        sayHelloButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                sayHello();
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        sayHelloButton.requestFocus();
    }

    private void sayHello() {
        Prompt.prompt("Hello from Pivot!", this);
    }

}
