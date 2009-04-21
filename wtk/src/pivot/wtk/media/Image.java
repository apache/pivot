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
package pivot.wtk.media;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import pivot.io.IOTask;
import pivot.util.ListenerList;
import pivot.util.concurrent.Dispatcher;
import pivot.util.concurrent.TaskListener;
import pivot.util.concurrent.TaskExecutionException;
import pivot.wtk.Dimensions;
import pivot.wtk.Visual;

/**
 * Abstract base class for images. An image is either a bitmapped "picture"
 * or a vector "drawing".
 *
 * @author gbrown
 */
public abstract class Image implements Visual {
    protected class ImageListenerList extends ListenerList<ImageListener>
        implements ImageListener {
        public void sizeChanged(Image image, int previousWidth, int previousHeight) {
            for (ImageListener listener : this) {
                listener.sizeChanged(image, previousWidth, previousHeight);
            }
        }

        public void regionInvalidated(Image image, int x, int y, int width, int height) {
            for (ImageListener listener : this) {
                listener.regionInvalidated(image, x, y, width, height);
            }
        }
    }

    /**
     * Task that executes an image load operation.
     *
     * @author gbrown
     */
    public static class LoadTask extends IOTask<Image> {
        private URL url = null;

        private static Dispatcher DEFAULT_DISPATCHER = new Dispatcher();

        public LoadTask(URL url) {
            this(url, DEFAULT_DISPATCHER);
        }

        public LoadTask(URL url, Dispatcher dispatcher) {
            super(dispatcher);
            this.url = url;
        }

        @Override
        public Image execute() throws TaskExecutionException {
            Image image = null;

            try {
                InputStream inputStream = null;

                try {
                    // NOTE We don't open the stream until the callback
                    // executes because this is a potentially time-consuming
                    // operation
                    inputStream = new BufferedInputStream(url.openStream());

                    // TODO Need a way to identify the type of image to load
                    // (picture or drawing) - an argument/property may be
                    // appropriate. If the attribute is optional, we can try to
                    // determine the type from the file extension in the URL, or
                    // by looking at the first few bytes of the input stream.

                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    BufferedImage bufferedImage =
                        serializer.readObject(new MonitoredInputStream(inputStream));
                    image = new Picture(bufferedImage);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch(Exception exception) {
                throw new TaskExecutionException(exception);
            }

            return image;
        }
    }

    protected ImageListenerList imageListeners = new ImageListenerList();

    public Dimensions getSize() {
        return new Dimensions(getWidth(), getHeight());
    }

    public ListenerList<ImageListener> getImageListeners() {
        return imageListeners;
    }

    public static Image load(URL url) {
        LoadTask loadTask = new LoadTask(url);

        Image image = null;
        try {
            image = loadTask.execute();
        } catch(TaskExecutionException exception) {
            throw new RuntimeException(exception);
        }

        return image;
    }

    public static Image.LoadTask load(URL url, TaskListener<Image> loadListener) {
        LoadTask loadTask = new LoadTask(url);
        loadTask.execute(loadListener);
        return loadTask;
    }
}
