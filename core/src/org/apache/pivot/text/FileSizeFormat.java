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
package org.apache.pivot.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * Converts a file size into a human-readable representation using binary
 * prefixes (1KB = 1024 bytes).
 */
public class FileSizeFormat extends Format {
    private static final long serialVersionUID = 9126510513247641698L;

    public static final int KILOBYTE = 1024;
    public static final String[] ABBREVIATIONS = {"K", "M", "G", "T", "P", "E", "Z", "Y"};

    private static final FileSizeFormat FILE_SIZE_FORMAT = new FileSizeFormat();

    private FileSizeFormat() {
    }

    /**
     * Formats a file size.
     *
     * @param object
     * A <tt>Number</tt> containing the length of the file, in bytes. May be
     * negative to indicate an unknown file size.
     *
     * @param stringBuffer
     * The string buffer to which the formatted output will be appended.
     *
     * @param fieldPosition
     * Not used.
     *
     * @return
     * The original string buffer, with the formatted value appended.
     */
    @Override
    public StringBuffer format(Object object, StringBuffer stringBuffer,
        FieldPosition fieldPosition) {
        Number number = (Number)object;

        long length = number.longValue();

        if (length >= 0) {
            double size = length;

            int i = -1;
            do {
                size /= KILOBYTE;
                i++;
            } while (size > KILOBYTE);

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            if (i == 0
                && size > 1) {
                numberFormat.setMaximumFractionDigits(0);
            } else {
                numberFormat.setMaximumFractionDigits(1);
            }

            stringBuffer.append(numberFormat.format(size) + " " + ABBREVIATIONS[i] + "B");
        }

        return stringBuffer;
    }

    /**
     * This method is not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public Object parseObject(String arg0, ParsePosition arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a shared file size format instance.
     *
     * @return
     * A shared file format size instance.
     */
    public static FileSizeFormat getInstance() {
        return FILE_SIZE_FORMAT;
    }
}
