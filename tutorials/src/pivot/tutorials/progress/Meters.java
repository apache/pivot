package pivot.tutorials.progress;

import pivot.collections.Dictionary;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskExecutionException;
import pivot.util.concurrent.TaskListener;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Meter;
import pivot.wtk.PushButton;
import pivot.wtk.TaskAdapter;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class Meters extends Bindable implements Application {
    public class SampleTask extends Task<Void> {
        private int percentage = 0;

        public Void execute() throws TaskExecutionException {
            // Simulate a long-running operation
            percentage = 0;

            while (percentage < 100
                && !abort) {
                try {
                    Thread.sleep(100);
                    percentage++;

                    // Update the meter on the UI thread
                    ApplicationContext.queueCallback(new Runnable() {
                        public void run() {
                            meter.setPercentage((double)percentage / 100);
                        }
                    });
                } catch(InterruptedException exception) {
                    throw new TaskExecutionException(exception);
                }
            }

            return null;
        }
    }

    @Load(resourceName="meters.wtkx") private Window window;
    @Bind(fieldName="window") private Meter meter;
    @Bind(fieldName="window") private PushButton progressButton;

    private SampleTask sampleTask = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        progressButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                if (sampleTask == null) {
                    // Create and start the simulated task; wrap it in a
                    // task adapter so the result handlers are called on the
                    // UI thread
                    sampleTask = new SampleTask();
                    sampleTask.execute(new TaskAdapter<Void>(new TaskListener<Void>() {
                        public void taskExecuted(Task<Void> task) {
                            reset();
                        }

                        public void executeFailed(Task<Void> task) {
                            reset();
                        }

                        private void reset() {
                            // Reset the meter and button
                            sampleTask = null;
                            meter.setPercentage(0);
                            updateProgressButton();
                        }
                    }));
                } else {
                    // Cancel the task
                    sampleTask.abort();
                }

                updateProgressButton();
            }
        });

        updateProgressButton();

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void updateProgressButton() {
        if (sampleTask == null) {
            progressButton.setButtonData("Start");
        } else {
            progressButton.setButtonData("Cancel");
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Meters.class, args);
    }
}
