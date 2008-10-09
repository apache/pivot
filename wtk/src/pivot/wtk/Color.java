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
package pivot.wtk;

/**
 * Class providing utility methods dealing with {@link java.awt.Color} objects.
 *
 * @author tvolkert
 */
public final class Color {
    private Color() {
    }

    /**
     * Converts a <tt>String</tt> to an integer and returns the specified
     * <tt>Color</tt>. This method handles string formats that are used to
     * represent octal and hexidecimal numbers.
     * <p>
     * Unlike {@link java.awt.Color.decode(String)}, this method supports
     * colors with an alpha channel.
     */
    public static java.awt.Color decode(String name) throws NumberFormatException {
        // Handle hexadecimal qualifier, if present
        int numericIndex = 0;
        if (name.startsWith("0x")
            || name.startsWith("0X")) {
            numericIndex += 2;
        } else if (name.startsWith("#")) {
            numericIndex++;
        }

        int rgb = Integer.decode(name);
        java.awt.Color color;

        if (((rgb >> 24) & 0xff) != 0
            || name.length() - numericIndex > 6) {
            // Extract an alpha channel
            float red = ((rgb >> 16) & 0xff) / 255f;
            float green = ((rgb >> 8) & 0xff) / 255f;
            float blue = (rgb & 0xff) / 255f;
            float alpha = ((rgb >> 24) & 0xff) / 255f;

            color = new java.awt.Color(red, green, blue, alpha);
        } else {
            color = new java.awt.Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        }

        return color;
    }
}
