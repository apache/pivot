package pivot.core.test;

import pivot.util.ListenerList;

public class ListenerListTest {
    public static class TestHandler implements TestListener {
        private int id;

        public TestHandler(int id) {
            this.id = id;
        }

        public void eventFired(TestSource testSource) {
            System.out.println("Event processed by " + id);
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }
    }

    public static void main(String[] args) {
        TestListener listener1 = new TestHandler(1);

        TestListener listener2 = new TestHandler(2) {
            public void eventFired(TestSource testSource) {
                System.out.println("Removing listener 2");
                testSource.getTestListeners().remove(this);
            }
        };

        TestListener listener3 = new TestHandler(3);
        TestListener listener4 = new TestHandler(4);

        final TestSource testSource = new TestSource();
        testSource.getTestListeners().add(listener1);
        testSource.getTestListeners().add(listener2);
        testSource.getTestListeners().add(listener3);
        testSource.getTestListeners().add(listener4);
        testSource.getTestListeners().add(listener3);
        testSource.getTestListeners().add(listener4);

        // testSource.getTestListeners().remove(listener1);
        // testSource.getTestListeners().remove(listener4);

        testSource.getTestListeners().add(listener1);

        testSource.fireEvent();
    }
}

interface TestListener {
    public void eventFired(TestSource testSource);
}

class TestSource {
    private static class TestListenerList extends ListenerList<TestListener>
        implements TestListener {
        public void eventFired(TestSource testSource) {
            for (TestListener listener : this) {
                listener.eventFired(testSource);
            }
        }
    }

    private TestListenerList testListeners = new TestListenerList();

    public void fireEvent() {
        testListeners.eventFired(this);
    }

    public ListenerList<TestListener> getTestListeners() {
        return testListeners;
    }
}