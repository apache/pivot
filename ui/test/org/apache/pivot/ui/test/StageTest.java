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
package org.apache.pivot.ui.test;

import java.util.List;
import java.util.Map;

import org.apache.pivot.scene.Stage;
import org.apache.pivot.scene.media.Image;
import org.apache.pivot.scene.shape.Rectangle;
import org.apache.pivot.ui.Application;

public class StageTest implements Application {
    public String getTitle() {
        return null;
    }

    public List<Image> getIcons() {
        return null;
    }

    public void startup(Stage stage, Map<String, String> properties) throws Exception {
        // TODO Rectangles don't have a preferred size; add a group to the stage and
        // put the rectangles in it
        stage.getNodes().add(new Rectangle(10, 10, 320, 240));
        stage.getNodes().add(new Rectangle(20, 20, 320, 240));
        stage.getNodes().add(new Rectangle(30, 30, 320, 240));
    }

    public boolean shutdown(boolean optional) {
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
