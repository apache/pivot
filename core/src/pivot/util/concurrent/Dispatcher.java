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
package pivot.util.concurrent;

import pivot.collections.ArrayQueue;
import pivot.collections.Queue;
import pivot.collections.concurrent.SynchronizedQueue;

/**
 * Operates a thread pool for dispatching runnable tasks. Runnables are
 * added to a pending queue and dispatched as threads become available to
 * execute them.
 * <p>
 * TODO This class is currently functional but not complete. Runnables are
 * currently dispatched as soon as they are added to the queue. Need to complete
 * the pooling implementation.
 * <p>
 * TODO Add a flag that allows the monitor thread to run as a non-daemon, and
 * define a shutdown() or cancel() method that will stop the thread. This will
 * allow ApplicationContext to control the dispatcher lifecycle and prevent
 * the thread from being randomly killed by applets.
 * <p>
 * TODO Is there a way to throw an AbortException when an item is removed
 * from the queue, without having to rely on methods like abort()?
 *
 * @author gbrown
 * @author tvolkert
 */
public class Dispatcher {
    private class MonitorThread extends Thread {
        public MonitorThread() {
            super(Dispatcher.this.getClass().getName() + "-MonitorThread");

            // Mark this thread as a daemon
            setDaemon(true);
        }

        public void run() {
            while (true) {
                // Block until an entry is available
                Runnable runnable = pendingQueue.dequeue();

                // When our thread group is disposed, this thread will get
                // interrupted, and the #dequeue() call will return null
                if (runnable == null) {
                    break;
                }

                // TODO Use the thread pool
                Thread workerThread = new Thread(runnable,
                    Dispatcher.this.getClass().getName() + "-WorkerThread");
                workerThread.setPriority(Thread.MIN_PRIORITY);
                workerThread.start();
            }
        }
    }

    private Queue<Runnable> pendingQueue = null;

    /*
    private int minimumThreadCount = 0;
    private int maximumThreadCount = 0;
    private List<Thread> threadPool = null;
    */

    private Thread queueMonitorThread = null;

    public Dispatcher() {
        this(0, 10);
    }

    public Dispatcher(int minimumThreadCount, int maximumThreadCount) {
        // TODO Use a linked queue for performance
        pendingQueue = new SynchronizedQueue<Runnable>(new ArrayQueue<Runnable>());

        // TODO
        /*
        this.minimumThreadCount = minimumThreadCount;
        this.maximumThreadCount = maximumThreadCount;

        // TODO Start minimum number of pool threads
        threadPool = new ArrayList<Thread>(maximumThreadCount);
        */
    }

    /**
     * Returns a reference to the dispatcher's pending runnable queue.
     *
     * @return
     * A synchronized queue from which the dispatcher will withdraw runnables.
     */
    public Queue<Runnable> getPendingQueue() {
        // TODO We need to check for isAlive() here because the Java Plugin
        // appears to kill the thread when navigating between pages. Revisit
        // this after J6u10 is generally available.

        if (queueMonitorThread == null
            || !queueMonitorThread.isAlive()) {
            queueMonitorThread = new MonitorThread();
            queueMonitorThread.start();
        }

        return pendingQueue;
    }
}
