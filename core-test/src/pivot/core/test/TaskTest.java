package pivot.core.test;

import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskGroup;
import pivot.util.concurrent.TaskListener;
import pivot.util.concurrent.TaskSequence;

public class TaskTest {
    public static class SleepTask extends Task<Void> {
        private long timeout = 0;

        public SleepTask(long timeout) {
            this.timeout = timeout;
        }

        @Override
        public Void execute() {
            System.out.println("Starting task " + this + "...");

            try {
                Thread.sleep(timeout);
            } catch (InterruptedException exception) {
                System.out.println(exception);
            }

            System.out.println("...done");

            return null;
        }

        @Override
        public String toString() {
            return Long.toString(timeout);
        }
    }

    public static void main(String[] args) {
        TaskListener<Void> taskListener = new TaskListener<Void>() {
            public synchronized void taskExecuted(Task<Void> task) {
                System.out.println("EXECUTED");
                notify();
            }

            public synchronized void executeFailed(Task<Void> task) {
                System.out.println("FAILED: " + task.getFault());
                notify();
            }
        };

        testTaskSequence(taskListener);
        testTaskGroup(taskListener);
    }

    private static void testTaskSequence(TaskListener<Void> taskListener) {
        System.out.println("Testing task sequence");

        TaskSequence<Void> taskSequence = new TaskSequence<Void>();

        SleepTask task1 = new SleepTask(2000);
        taskSequence.add(task1);

        SleepTask task2 = new SleepTask(500);
        taskSequence.add(task2);

        SleepTask task3 = new SleepTask(1000);
        taskSequence.add(task3);

        synchronized (taskListener) {
            taskSequence.execute(taskListener);

            try {
                taskListener.wait();
            } catch (InterruptedException exception) {
            }
        }
    }

    private static void testTaskGroup(TaskListener<Void> taskListener) {
        System.out.println("Testing task group");

        TaskGroup<Void> taskGroup = new TaskGroup<Void>();

        SleepTask task1 = new SleepTask(2000);
        taskGroup.add(task1);

        SleepTask task2 = new SleepTask(500);
        taskGroup.add(task2);

        SleepTask task3 = new SleepTask(1000);
        taskGroup.add(task3);

        synchronized (taskListener) {
            taskGroup.execute(taskListener);

            try {
                taskListener.wait();
            } catch (InterruptedException exception) {
            }
        }
    }
}
