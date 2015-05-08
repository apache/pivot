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
package org.apache.pivot.tests.issues.pivot964;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.media.SVGDiagramSerializer;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Test application with Pivot components
 */
public class Pivot964Pivot extends Application.Adapter {

    // Display stuff
    protected SVGDiagram diagram;
    protected SVGElement root;
    protected Window window;

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot964Pivot.class, args);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) {
        display.getHostWindow().setSize(1000, 600);  // force dimensions for host frame

        window = new Window();

        prepareSVG();

        final ImageView image = new ImageView(new Drawing(diagram));

        BoxPane bp = new BoxPane();

        PushButton pb1 = new PushButton("Visible");
        PushButton pb2 = new PushButton("Invisible (bug)");

        bp.add(image);
        bp.add(pb1);
        bp.add(pb2);

        pb1.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button arg0) {
                try {
                    root.setAttribute("viewBox", AnimationElement.AT_XML, "0 0 2368 1652");
                    root.updateTime(0f);
                    image.repaint();
                } catch (SVGElementException e) {
                    e.printStackTrace();
                } catch (SVGException e) {
                    e.printStackTrace();
                }
            }

        });

        pb2.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button arg0) {
                try {
                    root.setAttribute("viewBox", AnimationElement.AT_XML, "800 0 2368 1652");
                    root.updateTime(0f);
                    image.repaint();
                } catch (SVGElementException e) {
                    e.printStackTrace();
                } catch (SVGException e) {
                    e.printStackTrace();
                }
            }

        });

        window.setContent(bp);
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    protected void prepareSVG() {
        SVGDiagramSerializer s = new SVGDiagramSerializer();

        try {
            diagram = s.readObject(new ByteArrayInputStream(makeDynamicSVG().getBytes()));
            root = diagram.getRoot();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeDynamicSVG() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(
            "<svg width=\"800\" height=\"600\" style=\"fill:none;stroke-width:2\" viewBox=\"0 0 2368 1652\">");
        pw.println(
            "<rect x=\"0\" y=\"0\" width=\"2000\" height=\"1000\" style=\"stroke:blue;fill:blue\"/>");
        pw.println("</svg>");

        pw.close();
        return sw.toString();
    }

}
