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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Panorama;

public class PanoramaTest implements Application {
    private Frame frame1 = null;
    private Frame frame2 = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame1 = new Frame();
        frame1.setTitle("Panorama Test 1");

        Panorama panorama = new Panorama();
        frame1.setContent(panorama);
        frame1.setPreferredSize(240, 320);

        ImageView imageView = new ImageView();
        imageView.setImage(getClass().getResource("IMG_0767_2.jpg"));
        panorama.setView(imageView);
        frame1.open(display);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        frame2 = new Frame((Component) bxmlSerializer.readObject(getClass().getResource(
            "panorama_test.bxml")));
        frame2.setTitle("Panorama Test 2");
        frame2.setPreferredSize(480, 360);
        frame2.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame1 != null) {
            frame1.close();
        }

        if (frame2 != null) {
            frame2.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(PanoramaTest.class, args);
    }
}
