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
package org.apache.pivot.util.concurrent;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An executor service that simply spawns a new thread on every call to {@link #execute}.
 * <p> Note: this has been moved out of {@link Task} where it used to be used as the default
 * executor service as a workaround for problems seen some time ago with
 * {@link java.util.concurrent.Executors#newCachedThreadPool} when running as an applet.
 * <p> The default for {@link Task}, {@link TaskSequence} and {@link TaskGroup} is now to
 * use the system service, but this class may be used still as a workaround if problems
 * are still seen (unlikely).
 */
public class SimpleExecutorService extends AbstractExecutorService {
    private boolean shutdown = false;

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void shutdown() {
        shutdownNow();
    }

    @Override
    public java.util.List<Runnable> shutdownNow() {
        shutdown = true;
        return new java.util.ArrayList<>();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown();
    }

    @Override
    public void execute(Runnable command) {
        Thread thread = new Thread(command);
        thread.start();
    }
}

