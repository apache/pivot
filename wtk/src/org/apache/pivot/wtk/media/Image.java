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
package org.apache.pivot.wtk.media;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import org.apache.pivot.io.IOTask;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Visual;

import com.kitfox.svg.SVGDiagram;

/**
 * Abstract base class for images. An image is either a bitmapped "picture" or a
 * vector "drawing".
 */
public abstract class Image implements Visual {
    /**
     * Background {@link org.apache.pivot.util.concurrent.Task} that
     * executes an image load operation.
     */
    public static final class LoadTask extends IOTask<Image> {
        private URL location = null;

        public LoadTask(final URL location) {
            this(location, DEFAULT_EXECUTOR_SERVICE);
        }

        public LoadTask(final URL location, final ExecutorService executorService) {
            super(executorService);
            this.location = location;
        }

        public URL getLocation() {
            return location;
        }

        @Override
        public Image execute() throws TaskExecutionException {
            Image image = null;

            // NOTE We don't open the stream until the callback executes
            // because this is a potentially time-consuming operation
            try (InputStream inputStream =
                    new MonitoredInputStream(new BufferedInputStream(location.openStream()))) {

                if (location.getFile().endsWith(SVGDiagramSerializer.SVG_EXTENSION)) {
                    SVGDiagramSerializer serializer = new SVGDiagramSerializer();
                    SVGDiagram diagram = serializer.readObject(inputStream);
                    image = new Drawing(diagram);
                } else {
                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    BufferedImage bufferedImage = serializer.readObject(inputStream);
                    image = new Picture(bufferedImage);
                }
            } catch (IOException | SerializationException exception) {
                throw new TaskExecutionException(exception);
            }

            return image;
        }
    }

    protected ImageListener.Listeners imageListeners = new ImageListener.Listeners();

    @Override
    public int getBaseline() {
        return -1;
    }

    public void update(final int x, final int y, final int width, final int height) {
        imageListeners.regionUpdated(this, x, y, width, height);
    }

    public ListenerList<ImageListener> getImageListeners() {
        return imageListeners;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + getWidth() + "x" + getHeight() + "]";
    }

    /**
     * Load an image from the given URL location, but without any listener.  The load will
     * be done synchronously, on the current thread.  Any errors will be wrapped in the
     * {@link TaskExecutionException} that is thrown.
     *
     * @param location The URL where the image can be found.
     * @return The loaded image.
     * @throws TaskExecutionException if there were problems loading the image.
     * @see LoadTask
     */
    public static Image load(final URL location) throws TaskExecutionException {
        LoadTask loadTask = new LoadTask(location);
        return loadTask.execute();
    }

    /**
     * Load an image from the given URL location, in the background, and return a reference
     * to the background task.
     *
     * @param location The URL where the image can be found.
     * @param loadListener A listener for completion of the background task.
     * @return A reference to the background task.
     */
    public static Image.LoadTask load(final URL location, final TaskListener<Image> loadListener) {
        LoadTask loadTask = new LoadTask(location);
        loadTask.execute(loadListener);
        return loadTask;
    }

    /**
     * Load an image.  First try to find it in the resource cached kept by the
     * {@link ApplicationContext}.  If not found, load it in the foreground and
     * cache the result.
     *
     * @param location The URL where the image can be found.
     * @return The loaded image.
     * @throws IllegalArgumentException wrapping the TaskExecutionException, which
     * in turn will wrap the underlying problem.
     */
    public static Image loadFromCache(final URL location) {
        Utils.checkNull(location, "image location");

        Image image = (Image) ApplicationContext.getResourceCache().get(location);
        if (image == null) {
            try {
                image = Image.load(location);
                ApplicationContext.getResourceCache().put(location, image);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

        }

        return image;
    }

}
