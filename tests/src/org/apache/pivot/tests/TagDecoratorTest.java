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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.effects.TagDecorator;
import org.apache.pivot.wtk.media.Image;

public final class TagDecoratorTest implements Application {
    private Frame frame = null;

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Tag Decorator Test");
        frame.setPreferredSize(480, 360);

        Image tag = Image.load(getClass().getResource("go-home.png"));

        frame.getDecorators().add(
            new TagDecorator(tag, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, 10, -10));

        frame.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(TagDecoratorTest.class, args);
    }
}
