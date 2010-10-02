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
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Visual;

import com.kitfox.svg.SVGDiagram;

/**
 * Abstract base class for images. An image is either a bitmapped "picture"
 * or a vector "drawing".
 */
public abstract class Image implements Visual {
    /**
     * Image listener list.
     */
    protected static class ImageListenerList extends ListenerList<ImageListener>
        implements ImageListener {
        @Override
        public void sizeChanged(Image image, int previousWidth, int previousHeight) {
            for (ImageListener listener : this) {
                listener.sizeChanged(image, previousWidth, previousHeight);
            }
        }

        @Override
        public void baselineChanged(Image image, int previousBaseline) {
            for (ImageListener listener : this) {
                listener.baselineChanged(image, previousBaseline);
            }
        }

        @Override
        public void regionUpdated(Image image, int x, int y, int width, int height) {
            for (ImageListener listener : this) {
                listener.regionUpdated(image, x, y, width, height);
            }
        }
    }

    /**
     * Task that executes an image load operation.
     */
    public static class LoadTask extends IOTask<Image> {
        private URL location = null;

        public LoadTask(URL location) {
            this(location, DEFAULT_EXECUTOR_SERVICE);
        }

        public LoadTask(URL location, ExecutorService executorService) {
            super(executorService);
            this.location = location;
        }

        public URL getLocation() {
            return location;
        }

        @Override
        public Image execute() throws TaskExecutionException {
            Image image = null;

            try {
                InputStream inputStream = null;

                try {
                    // NOTE We don't open the stream until the callback executes because
                    // this is a potentially time-consuming operation
                    inputStream = new MonitoredInputStream(new BufferedInputStream(location.openStream()));

                    if (location.getFile().endsWith(SVGDiagramSerializer.SVG_EXTENSION)) {
                        SVGDiagramSerializer serializer = new SVGDiagramSerializer();
                        SVGDiagram diagram = serializer.readObject(inputStream);
                        image = new Drawing(diagram);
                    } else {
                        BufferedImageSerializer serializer = new BufferedImageSerializer();
                        BufferedImage bufferedImage = serializer.readObject(inputStream);
                        image = new Picture(bufferedImage);
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (IOException exception) {
                throw new TaskExecutionException(exception);
            } catch (SerializationException exception) {
                throw new TaskExecutionException(exception);
            }

            return image;
        }
    }

    protected ImageListenerList imageListeners = new ImageListenerList();

    @Override
    public int getBaseline() {
        return -1;
    }

    public Dimensions getSize() {
        return new Dimensions(getWidth(), getHeight());
    }

    public void update(int x, int y, int width, int height) {
        imageListeners.regionUpdated(this, x, y, width, height);
    }

    public ListenerList<ImageListener> getImageListeners() {
        return imageListeners;
    }

    public static Image load(URL location) throws TaskExecutionException {
        LoadTask loadTask = new LoadTask(location);
        return loadTask.execute();
    }

    public static Image.LoadTask load(URL location, TaskListener<Image> loadListener) {
        LoadTask loadTask = new LoadTask(location);
        loadTask.execute(loadListener);
        return loadTask;
    }
}
