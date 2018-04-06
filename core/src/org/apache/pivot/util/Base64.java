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

/**
 * Implements the "base64" binary encoding scheme as defined by <a
 * href="http://tools.ietf.org/html/rfc2045">RFC 2045</a>.
 */
public final class Base64 {
    private static final char[] LOOKUP = new char[64];
    private static final byte[] REVERSE_LOOKUP = new byte[256];

    static {
        // Populate the lookup array

        for (int i = 0; i < 26; i++) {
            LOOKUP[i] = (char) ('A' + i);
        }

        for (int i = 26, j = 0; i < 52; i++, j++) {
            LOOKUP[i] = (char) ('a' + j);
        }

        for (int i = 52, j = 0; i < 62; i++, j++) {
            LOOKUP[i] = (char) ('0' + j);
        }

        LOOKUP[62] = '+';
        LOOKUP[63] = '/';

        // Populate the reverse lookup array

        for (int i = 0; i < 256; i++) {
            REVERSE_LOOKUP[i] = -1;
        }

        for (int i = 'Z'; i >= 'A'; i--) {
            REVERSE_LOOKUP[i] = (byte) (i - 'A');
        }

        for (int i = 'z'; i >= 'a'; i--) {
            REVERSE_LOOKUP[i] = (byte) (i - 'a' + 26);
        }

        for (int i = '9'; i >= '0'; i--) {
            REVERSE_LOOKUP[i] = (byte) (i - '0' + 52);
        }

        REVERSE_LOOKUP['+'] = 62;
        REVERSE_LOOKUP['/'] = 63;
        REVERSE_LOOKUP['='] = 0;
    }

    /**
     * This class is not instantiable.
     */
    private Base64() {
    }

    /**
     * Encodes the specified data into a base64 string.
     *
     * @param bytes The unencoded raw data.
     * @return The base64 encoded string.
     */
    public static String encode(final byte[] bytes) {
        StringBuilder buf = new StringBuilder(4 * (bytes.length / 3 + 1));

        for (int i = 0, n = bytes.length; i < n;) {
            byte byte0 = bytes[i++];
            byte byte1 = (i++ < n) ? bytes[i - 1] : 0;
            byte byte2 = (i++ < n) ? bytes[i - 1] : 0;

            buf.append(LOOKUP[byte0 >> 2]);
            buf.append(LOOKUP[((byte0 << 4) | byte1 >> 4) & 63]);
            buf.append(LOOKUP[((byte1 << 2) | byte2 >> 6) & 63]);
            buf.append(LOOKUP[byte2 & 63]);

            if (i > n) {
                for (int m = buf.length(), j = m - (i - n); j < m; j++) {
                    buf.setCharAt(j, '=');
                }
            }
        }

        return buf.toString();
    }

    /**
     * Decodes the specified base64 string back into its raw data.
     *
     * @param encoded The base64 encoded string.
     * @return The decoded data bytes.
     */
    public static byte[] decode(final String encoded) {
        int padding = 0;

        for (int i = encoded.length() - 1; encoded.charAt(i) == '='; i--) {
            padding++;
        }

        int length = encoded.length() * 6 / 8 - padding;
        byte[] bytes = new byte[length];

        for (int i = 0, index = 0, n = encoded.length(); i < n; i += 4) {
            int word = REVERSE_LOOKUP[encoded.charAt(i)] << 18;
            word += REVERSE_LOOKUP[encoded.charAt(i + 1)] << 12;
            word += REVERSE_LOOKUP[encoded.charAt(i + 2)] << 6;
            word += REVERSE_LOOKUP[encoded.charAt(i + 3)];

            for (int j = 0; j < 3 && index + j < length; j++) {
                bytes[index + j] = (byte) (word >> (8 * (2 - j)));
            }

            index += 3;
        }

        return bytes;
    }
}
