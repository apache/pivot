/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encode/decode an MD5 digest to/from a string.
 * <p>
 * The 128 bit MD5 hash is converted into a 32-character string. Each
 * character of the String is the hexadecimal representation of 4 bits
 * of the digest.
 * <p>
 * Portions of code here are taken from Apache Tomcat; see
 * <tt>org.apache.catalina.util.MD5Encoder</tt>.
 */
public final class MD5 {
    public static final int MD5_DIGEST_LENTGH_IN_BYTES = 16;
    public static final String MD5_ALGORITHM_NAME = "MD5";

    private static MessageDigest md5 = null;

    private static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f' };

    private MD5() {
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the specified
     * byte sequence. Note that any Exception is handled inside.
     *
     * @param data the data to digest.
     * @return the MD5 digest as an array of 16 bytes.
     */
    public static final byte[] digest(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("null data");
        }

        byte[] dataDigested = null;

        // TODO Why is this commented out?
        // synchronized (MD5.class) {
            if (md5 == null) {
                try {
                    md5 = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
                } catch (NoSuchAlgorithmException e) {
                    md5 = null;
                }
            }

            if (md5 != null) {
                dataDigested = md5.digest(data);
            }

        // }

        return dataDigested;
    }

    /**
     * Transform the given string in a byte array, using the given encoding.
     * Note that any Exception is handled inside.
     *
     * @param string
     * The string to transform.
     *
     * @param encoding
     * The encoding to use, or <tt>null</tt> to use the default encoding.
     *
     * @return
     * The string transformed into a byte array.
     */
    public static final byte[] digest(final String string, final String encoding) {
        byte[] data = null;
        try {
            if (encoding == null) {
                data = string.getBytes();
            } else {
                data = string.getBytes(encoding);
            }
        } catch (UnsupportedEncodingException e) {
        }

        byte[] dataDigested = digest(data);
        return dataDigested;
    }

    /**
     * Encodes the 128 bit (16 bytes) MD5 into a 32 character String.
     *
     * @param binaryData
     * The byte array containing the digest.
     *
     * @return
     * The encoded MD5 string.
     */
    public static final String encode(byte[] binaryData) {
        if (binaryData.length != MD5_DIGEST_LENTGH_IN_BYTES) {
            throw new IllegalArgumentException("binaryData must be an array of 16 bytes");
        }

        char[] buffer = new char[MD5_DIGEST_LENTGH_IN_BYTES * 2];

        for (int i = 0; i < MD5_DIGEST_LENTGH_IN_BYTES; i++) {
            int low = binaryData[i] & 0x0f;
            int high = (binaryData[i] & 0xf0) >> 4;

            buffer[i * 2] = hexadecimal[high];
            buffer[i * 2 + 1] = hexadecimal[low];
        }

        return new String(buffer);
    }

    /**
     * Transform the given string in a digested version, using the given encoding.
     *
     * @param string
     * The string to digest.
     *
     * @param encoding
     * The encoding to use, or <tt>null to use the default encoding.</tt>
     *
     * @return
     * The digested string.
     */
    public static final String digestAsString(final String string, final String encoding) {
        byte[] digestBytes = MD5.digest(string, encoding);
        String dataDigested = MD5.encode(digestBytes);

        return dataDigested;
    }

    /**
     * Convert a byte array into a printable format containing a string of
     * hexadecimal digit characters (two per byte).
     *
     * @param bytes
     * Byte array representation.
     */
    public static String toHexString(byte bytes[]) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(convertDigit(bytes[i] >> 4));
            sb.append(convertDigit(bytes[i] & 0x0f));
        }

        return (sb.toString());
    }

    private static char convertDigit(int value) {
        value &= 0x0f;

        if (value >= 10) {
            return ((char) (value - 10 + 'a'));
        }

        return ((char) (value + '0'));
    }
}
