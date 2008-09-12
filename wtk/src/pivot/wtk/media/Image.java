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
package pivot.wtk.media;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.net.URL;
import pivot.util.concurrent.Dispatcher;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.util.concurrent.TaskExecutionException;
import pivot.wtk.Dimensions;
import pivot.wtk.Visual;

/**
 * <p>Abstract base class for images. An image is either a bitmapped "picture"
 * or a vector "drawing".</p>
 *
 * @author gbrown
 */
public abstract class Image implements Visual {
    /**
     * <p>Task that executes an image load operation.</p>
     *
     * @author gbrown
     */
    public static class LoadTask extends Task<Image> {
        private URL url = null;

        public LoadTask(URL url) {
            super();
            this.url = url;
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

                    // TODO Use an instance of ImageReader here instead of read().
                    // This will allow us to abort and time out image load operations.
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
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

    public Dimensions getSize() {
        return new Dimensions(getWidth(), getHeight());
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
