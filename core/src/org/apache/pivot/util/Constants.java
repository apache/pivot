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
 * A static class that contains constant values used throughout the system.
 */
public final class Constants {

    /** Private constructor for a utility class. */
    private Constants() {
    }

    /**
     * The non-default buffer size to use for <tt>BufferedReader</tt> and
     * and <tt>BufferedWriter</tt>.
     * <p> This should be larger than the default value (which seems to be
     * 8192 as of Java 7), or there is no point in using it.
     */
    public static final int BUFFER_SIZE = 16_384;

    /**
     * Standard URL encoding scheme.
     */
    public static final String URL_ENCODING = "UTF-8";

    /**
     * The Byte-Order-Mark that can be used to distinguish the byte ordering
     * of a UTF-16 stream.
     * <p> Meant to be ignored if present in a UTF-8 stream (for instance).
     */
    public static final int BYTE_ORDER_MARK = 0xFEFF;

    /** The plain-text HTTP protocol identifier. */
    public static final String HTTP_PROTOCOL = "http";
    /** The secure HTTP protocol identifier. */
    public static final String HTTPS_PROTOCOL = "https";

    /** Standard name of the HTTP header for the content type. */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    /** Standard name of the HTTP header for the content length. */
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    /** Standard name of the HTTP header for the location. */
    public static final String LOCATION_HEADER = "Location";

}
