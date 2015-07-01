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
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TablePane;
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
    String[] spinnerData = {
        "0", "100", "200", "210", "220", "230", "240", "250",
        "260", "261", "262", "263", "264", "265", "266", "267", "268", "269",
        "270", "280", "290", "300", "400",
        "500", "501", "502", "503", "504", "505", "506", "507", "508", "509", "510",
        "600", "700", "800", "900", "1000", "1500", "2000" 
    };

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot964Pivot.class, args);
    }

    private void setStyles(Component comp, String styles) {
        try {
            comp.setStyles(styles);
        } catch (SerializationException se) {
            throw new RuntimeException(se);
        }
    }

    @Override
    public void startup(Display display, Map<String, String> properties) {
        display.getHostWindow().setSize(1028, 600);  // force dimensions for host frame

        window = new Window();

        prepareSVG();

        final ImageView image = new ImageView(new Drawing(diagram));

        BoxPane bp = new BoxPane();

        TablePane tp = new TablePane();
        setStyles(tp, "{padding: 4}");
        TablePane.Column c1 = new TablePane.Column(-1);
        TablePane.Column c2 = new TablePane.Column(-1);
        tp.getColumns().add(c1);
        tp.getColumns().add(c2);
        TablePane.Row r1 = new TablePane.Row(-1);
        TablePane.Row r2 = new TablePane.Row(-1);
        TablePane.Row r3 = new TablePane.Row(-1);

        PushButton pb1 = new PushButton("Visible");
        PushButton pb2 = new PushButton("Invisible (bug)");
        r1.add(pb1);
        r1.add(pb2);
        final Spinner sp1 = new Spinner(new ListAdapter<String>(spinnerData));
        sp1.setPreferredWidth(80);
        sp1.setSelectedIndex(0);
        final Spinner sp2 = new Spinner(new ListAdapter<String>(spinnerData));
        sp2.setPreferredWidth(80);
        sp2.setSelectedIndex(0);
        BoxPane bp1 = new BoxPane();
        setStyles(bp1, "{verticalAlignment:'center', padding: 4, spacing: 2}");
        bp1.add(new Label("X:"));
        bp1.add(sp1);
        r2.add(bp1);
        BoxPane bp2 = new BoxPane();
        setStyles(bp2, "{verticalAlignment:'center', padding: 4, spacing: 2}");
        bp2.add(new Label("Y:"));
        bp2.add(sp2);
        r2.add(bp2);
        tp.getRows().add(r1);
        tp.getRows().add(r2);
        r3.add(new Label("   Max X=507"));
        r3.add(new Label("   Max Y=269"));
        tp.getRows().add(r3);

        bp.add(image);
        bp.add(tp);

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
                    String xOffset = (String)sp1.getSelectedItem();
                    String yOffset = (String)sp2.getSelectedItem();
                    String viewBox = String.format("%1$s %2$s 2368 1652", xOffset, yOffset);
                    root.setAttribute("viewBox", AnimationElement.AT_XML, viewBox);
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
            "<svg width=\"800\" height=\"600\" style=\"fill:none;stroke-width:20\" viewBox=\"0 0 2368 1652\">");
        pw.println(
            "<rect x=\"0\" y=\"0\" width=\"2000\" height=\"1000\" style=\"stroke:blue;fill:cyan\"/>");
        pw.println("</svg>");

        pw.close();
        return sw.toString();
    }

}
