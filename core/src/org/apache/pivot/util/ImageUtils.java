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
package org.apache.pivot.util;

import java.net.URL;

/**
 * Utility class for dealing with images.
 */
public class ImageUtils {

    /**
     * Find an image in the application's resources given the name.
     *
     * @param imageName The name of the image resource to find (whose
     * leading character is stripped off -- likely "@" or "/").
     * @param imageType A user-friendly name of what this resource is (for
     * error messages).
     * @return The URL from which to load the image if it is found.
     * @throws IllegalArgumentException if the image resource cannot be found,
     * or if the {@code imageName} is null or empty.
     */
    public static URL findByName(String imageName, String imageType) {
        Utils.checkNullOrEmpty(imageName, "imageName");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(imageName.substring(1));
        if (url == null) {
            throw new IllegalArgumentException("Cannot find " + imageType + " resource: " + imageName);
        }
        return url;
    }

}
