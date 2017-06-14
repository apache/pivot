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
 * Utility class for a simple log to the console, for example from scripts.
 */
public class Console {

    private Console() {
    }

    public static final void log(String message) {
        logOutput(message);
    }

    public static final void log(Throwable t) {
        if (t != null) {
            t.printStackTrace();
        }
    }

    public static final void logExceptionMessage(Throwable t) {
        logOutput(t.getMessage());
    }

    public static final void logOutput(String message) {
        System.out.println(message != null ? message : "");
    }

    public static final void logError(String message) {
        System.err.println(message != null ? message : "");
    }

    /**
     * Log a message along with the calling method's name to the system console.
     *
     * @param message The message to be logged to {@link System#out},
     * or a format string using the remaining args (can be {@code null}).
     * @param args The optional arguments used to format the final message.
     */
    public static final void logMethod(String message, Object... args) {
        logOutput(ClassUtils.getCallingMethod(1) + ": " +
            (message == null ? "" : String.format(message, args)));
    }

    /**
     * Log a message with an identifying prefix, along with the calling
     * method's name to the system console.
     *
     * @param prefix Any kind of "marker" prefix to the message (can be {@code null}).
     * @param message The message to be logged to {@link System#out},
     * or a format string using the remaining args (can be {@code null}).
     * @param args The optional arguments used to format the final message.
     */
    public static final void logMethod(String prefix, String message, Object... args) {
        logOutput(
            (prefix == null ? "" : prefix + " ") +
            ClassUtils.getCallingMethod(1) +
            ": " +
            (message == null ? "" : String.format(message, args))
        );
    }

}
