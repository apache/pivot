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
package pivot.wtk;

/**
 * Container class representing a decorated frame window.
 *
 * @author gbrown
 */
public class Frame extends Window {
    public Frame() {
        this(null, null);
    }

    public Frame(String title) {
        this(title, null);
    }

    public Frame(Component content) {
        this(null, content);
    }

    public Frame(String title, Component content) {
        super(content, false);

        setTitle(title);
        installSkin(Frame.class);
    }
}
