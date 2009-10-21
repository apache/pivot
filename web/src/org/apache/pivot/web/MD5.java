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
package org.apache.pivot.web;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encode / Decode an MD5 digest into / from a String.
 * <p>
 * The 128 bit MD5 hash is converted into a 32 character long String.
 * Each character of the String is the hexadecimal representation of 4 bits
 * of the digest.
 * <br/>
 * Portions of code here are taken from Apache Tomcat.
 *
 * @see org.apache.catalina.util.MD5Encoder
 */
public final class MD5 {
    public static final int MD5_DIGEST_LENTGH_IN_BYTES = 16;
    public static final String MD5_ALGORITHM_NAME = "MD5";

    /** The MD5 message digest generator. */
    private static MessageDigest md5;

    /** Constructor for private usage */
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

        synchronized (MD5.class) {
            if (md5 == null) {
                try {
                    md5 = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
                } catch (NoSuchAlgorithmException e) {
                    // e.printStackTrace();
                    md5 = null;
                }
            }

            if (md5 != null) {
                dataDigested = md5.digest(data);
            }

        }

        return dataDigested;
    }

    /**
     * Transform the given string in a byte array, using the given encoding.
     * Note that any Exception is handled inside.
     *
     * @param string the string
     * @param encoding the encoding, or if null a default will be used
     * @return the string transformed to byte array
     */
    public static final byte[] digest(final String string, final String encoding) {
        byte[] data = null;
        try {
            data = HexUtils.toByteArray(string, encoding);
        } catch (UnsupportedEncodingException e) {
            // e.printStackTrace();
        }

        byte[] dataDigested = digest(data);
        return dataDigested;
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        // Two arrays are equal if they have the same length and each element
        // is equal to the corresponding element in the other array;
        // otherwise, they�re not.

        if (digesta == null || digestb == null) {
            return false;
        }
        else if (digesta.length == MD5_DIGEST_LENTGH_IN_BYTES
            && digestb.length == MD5_DIGEST_LENTGH_IN_BYTES) {
            for (int i = 0; i < MD5_DIGEST_LENTGH_IN_BYTES; i++) {
                if (digesta[i] != digestb[i])
                    return false;
            }

            return true;
        } else {
            return false;
        }

    }

    /**
     * Encodes the 128 bit (16 bytes) MD5 into a 32 character String.
     *
     * @param binaryData The Array containing the digest
     * @return Encoded MD5, or null if encoding failed
     */
    public static final String encode(byte[] binaryData) {
        if (binaryData.length != MD5_DIGEST_LENTGH_IN_BYTES) {
            throw new IllegalArgumentException("binaryData must be an array of 16 bytes");
        }

        char[] buffer = new char[MD5_DIGEST_LENTGH_IN_BYTES * 2];

        for (int i = 0; i < MD5_DIGEST_LENTGH_IN_BYTES; i++) {
            int low = (int) (binaryData[i] & 0x0f);
            int high = (int) ((binaryData[i] & 0xf0) >> 4);

            buffer[i * 2] = HexUtils.hexadecimal[high];
            buffer[i * 2 + 1] = HexUtils.hexadecimal[low];
        }

        return new String(buffer);
    }

    /**
     * Decodes the specified base64 string back into its raw data.
     *
     * @param encodedData The MD5 encoded string.
     */
    public static final byte[] decode(String encodedData) {
        if (encodedData.length() != 32) {
            throw new IllegalArgumentException("encodedData must be a String of length 32 chars");
        }

        throw new UnsupportedOperationException("Decoding from an MD5 Hash is not supported");
    }

}
