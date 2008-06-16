/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.ArrayList;
import pivot.collections.ArrayQueue;
import pivot.collections.Queue;
import pivot.collections.List;
import pivot.collections.concurrent.SynchronizedQueue;

/**
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

                // TODO Use the thread pool
                Thread workerThread = new Thread(runnable,
                    Dispatcher.this.getClass().getName() + "-WorkerThread");
                workerThread.start();
            }
        }
    }

    private int minimumThreadCount = 0;
    private int maximumThreadCount = 0;

    private Queue<Runnable> pendingQueue = null;
    private List<Thread> threadPool = null;

    private Thread queueMonitorThread = null;

    public Dispatcher() {
        this(0, 10);
    }

    public Dispatcher(int minimumThreadCount, int maximumThreadCount) {
        this.minimumThreadCount = minimumThreadCount;
        this.maximumThreadCount = maximumThreadCount;

        // TODO Use a linked queue for performance
        pendingQueue = new SynchronizedQueue<Runnable>(new ArrayQueue<Runnable>());

        // TODO Start minimum number of pool threads
        threadPool = new ArrayList<Thread>(maximumThreadCount);

        queueMonitorThread = new MonitorThread();
        queueMonitorThread.start();
    }

    /**
     * Returns a reference to the dispatcher's pending runnable queue.
     *
     * @return
     * A synchronized queue from which the dispatcher will withdraw runnables.
     */
    public Queue<Runnable> getPendingQueue() {
        return pendingQueue;
    }
}
