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
package org.apache.pivot.scene;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class SWTStageHost extends Canvas {
    public SWTStageHost(Composite parent, int style) {
        super(parent, style);

        // TODO Use appropriate styles to control redraws

        // TODO Add a paint listener
    }

    // TODO We'll need to maintain a reference-counted cache of resources
    // such as fonts and paints. We'll need to dispose that cache when the
    // stage host is disposed. Maybe we can use weak references to manage
    // this. We could use a weak map of org.apache.pivot.scene.Font to
    // org.eclipse.swt.graphics.Font, etc. When the gargbage collector
    // detects that no more references to a given Font are outstanding,
    // the association will be removed from the map and we will dispose
    // the resource.
}
