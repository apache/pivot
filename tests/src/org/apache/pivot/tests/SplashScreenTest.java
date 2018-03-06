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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.File;
import java.util.Date;
import java.util.Random;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

/**
 * Test Application for demonstrating the <code>--preserveSplashScreen</code>
 * Pivot startup property.<br/> This desktop application simulates some task
 * processing while a SplashScreen is displayed, before forcing the Pivot host
 * window to become visible which in turn hides the SplashScreen.<br/>The
 * progress of the simulated tasks is shown using a Pivot Meter that paints onto
 * the SplashScreen as the tasks 'complete'. <br/><br/> If
 * <code>--preserveSplashScreen</code> is set to <code>true</code> and there is
 * a SplashScreen, DesktopApplicationContext will not make the Pivot host window
 * visible until {@link DesktopApplicationContext#replaceSplashScreen(Display)
 * replaceSplashScreen(Display)} is called.<br/><br/> If
 * <code>--preserveSplashScreen</code> is set to <code>false</code>, is not
 * supplied, or there is no SplashScreen, DesktopApplicationContext make the
 * Pivot host window visible as normal. Any calls to
 * {@link DesktopApplicationContext#replaceSplashScreen(Display)
 * replaceSplashScreen(Display)} will have no effect.<br/><br/> <b>Example
 * usage</b> (all one line) <pre> java -classpath bin;
 * -splash:bin/org/apache/pivot/tests/splash.png
 * org.apache.pivot.tests.SplashScreenTest --preserveSplashScreen=true
 * --fullScreen=false </pre>
 *
 * @see SplashScreen
 * @see DesktopApplicationContext#replaceSplashScreen(Display)
 * @see DesktopApplicationContext#PRESERVE_SPLASH_SCREEN_ARGUMENT
 */
public final class SplashScreenTest implements Application {

    private static final class SplashScreenProgressOverlay {
        private final SplashScreen splashScreen;
        private final Meter meter = new Meter(Orientation.HORIZONTAL);
        private Graphics2D graphics;

        private SplashScreenProgressOverlay() {
            this.splashScreen = SplashScreen.getSplashScreen();
            if (splashScreen != null) {
                configureMeter(256, 16);
                configureGraphics();
            } else {
                System.err.println("Splash Screen not found");
            }
        }

        // Increment the Meter by a percentage supplied as a double between
        // 0.0 and and 1.0 (representing 0% and 100% respectively)
        private void increment(final double increment) {
            if (splashScreen == null) {
                return;
            }

            double percentage = meter.getPercentage() + increment;
            meter.setPercentage(Math.min(percentage, 1.0f));
            if (splashScreen != null) {
                meter.paint(graphics);
                splashScreen.update();
            }
            System.out.println(String.format("Completed : %3.0f%%", getPercentage() * 100));
        }

        private double getPercentage() {
            return meter.getPercentage();
        }

        private void configureMeter(final int width, final int height) {
            meter.setSize(width, height);
            meter.setPercentage(0);
            meter.getStyles().put(Style.gridFrequency, 1);
        }

        // Align the Meter on the SplashScreen, centered horizontally,
        // 10 pixels from the bottom edge
        private void configureGraphics() {
            Rectangle splash = splashScreen.getBounds();
            int x = ((splash.width - meter.getBounds().width) / 2);
            int y = (splash.height - meter.getBounds().height - 10);
            graphics = splashScreen.createGraphics();
            graphics.translate(x, y);
        }
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {

        File splashFile = new File("org/apache/pivot/tests/splash.png");
        System.out.println("Startup the application at " + new Date());
        System.out.println(
            "To show the Splash Screen, remember to run as a Standard Java Application this way:\n"
            + "java -splash:" + splashFile.getPath() + " <mainclassname> --preserveSplashScreen=true\n"
            + "or no splash screen will be shown.");

        // Create a Task that will load a BXML file and simulate some other
        // processing while updating a progress meter on the SplashScreen
        final Task<Void> prepareApplicationTask = new Task<Void>() {
            final SplashScreenProgressOverlay progressOverlay = new SplashScreenProgressOverlay();

            @Override
            public Void execute() throws TaskExecutionException {
                // Load the main BXML
                progressOverlay.increment(0);
                loadBXML(display, 0.1);

                // Simulate other tasks until the progress meter has been filled
                final Random random = new Random();
                while (progressOverlay.getPercentage() < 1.0) {
                    // Short random sleep to simulate some processing
                    try {
                        Thread.sleep(random.nextInt(50) + 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Update the progress meter by a random amount
                    progressOverlay.increment((1 + random.nextInt(10)) / 100.0);
                }
                return null;
            }

            // Load the Pivot UI
            private void loadBXML(final Display displayArgument, final double weight) {
                try {
                    ApplicationContext.queueCallback(() -> {
                        Window window = null;
                        try {
                            window = (Window) new BXMLSerializer().readObject(this.getClass().getResource(
                                "splash.bxml"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        if (window != null) {
                            window.open(displayArgument);
                            progressOverlay.increment(weight);
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Hide the SplashScreen when the Task finishes by making the Pivot host
        // window visible.
        final TaskListener<Void> taskListener = new TaskListener<Void>() {
            @Override
            public void taskExecuted(final Task<Void> task) {
                finished();
            }

            @Override
            public void executeFailed(final Task<Void> task) {
                System.err.println(String.format("Failed\n%s", task.getFault()));
                task.getFault().printStackTrace();
                finished();
            }

            private void finished() {
                DesktopApplicationContext.replaceSplashScreen(display);
            }
        };

        // Run the Task asynchronously
        prepareApplicationTask.execute(new TaskAdapter<>(taskListener));
    }

    @Override
    public boolean shutdown(final boolean optional) throws Exception {
        System.out.println("Shutdown the application at " + new Date());
        return false;
    }

    public static void main(final String[] args) {
        // Allow the BXML to be loaded on a background thread
        Container.setEventDispatchThreadChecker(null);

        // Start the application
        DesktopApplicationContext.main(SplashScreenTest.class, args);
    }

}
