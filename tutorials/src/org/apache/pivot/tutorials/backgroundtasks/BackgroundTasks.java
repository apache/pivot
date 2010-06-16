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
package org.apache.pivot.tutorials.backgroundtasks;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

public class BackgroundTasks implements Application {
    private Window window = null;

    private ActivityIndicator activityIndicator = null;
    private PushButton executeSynchronousButton = null;
    private PushButton executeAsynchronousButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BeanSerializer beanSerializer = new BeanSerializer();
        window = (Window)beanSerializer.readObject(this, "background_tasks.bxml");

        activityIndicator = (ActivityIndicator)beanSerializer.get("activityIndicator");

        executeSynchronousButton = (PushButton)beanSerializer.get("executeSynchronousButton");
        executeSynchronousButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                activityIndicator.setActive(true);

                System.out.println("Starting synchronous task execution.");

                SleepTask sleepTask = new SleepTask();

                String result = null;
                try {
                    result = sleepTask.execute();
                } catch (TaskExecutionException exception) {
                    System.err.println(exception);
                }

                System.out.println("Synchronous task execution complete: \"" + result + "\"");

                activityIndicator.setActive(false);
            }
        });

        executeAsynchronousButton = (PushButton)beanSerializer.get("executeAsynchronousButton");
        executeAsynchronousButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                activityIndicator.setActive(true);
                window.setEnabled(false);

                System.out.println("Starting asynchronous task execution.");

                SleepTask sleepTask = new SleepTask();
                TaskListener<String> taskListener = new TaskListener<String>() {
                    @Override
                    public void taskExecuted(Task<String> task) {
                        activityIndicator.setActive(false);
                        window.setEnabled(true);

                        System.out.println("Synchronous task execution complete: \""
                            + task.getResult() + "\"");
                    }

                    @Override
                    public void executeFailed(Task<String> task) {
                        activityIndicator.setActive(false);
                        window.setEnabled(true);

                        System.err.println(task.getFault());
                    }
                };

                sleepTask.execute(new TaskAdapter<String>(taskListener));
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(BackgroundTasks.class, args);
    }
}
