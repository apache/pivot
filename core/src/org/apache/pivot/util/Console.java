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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.NoSuchFileException;

/**
 * Utility class for a simple log to the console, for example from scripts.
 */
public final class Console {

    /** Used to output a null message value, so user knows a null "something" was logged. */
    private static final String NULL = "<null>";

    /** For static logging, the singleton instance pointing to the system console to use. */
    private static final Console CONSOLE = new Console();

    /** The print stream to use for "output" logging.  Defaults to {@link System#out}. */
    private PrintStream out = System.out;
    /** The print stream to use for "error" logging.  Defaults to {@link System#err}. */
    private PrintStream err = System.err;


    public Console() {
    }

    public Console(final PrintStream stream) {
        setStreams(stream);
    }

    public Console(final PrintStream output, final PrintStream error) {
        setOutputStream(output);
        setErrorStream(error);
    }

    public static Console getDefault() {
        return CONSOLE;
    }

    private void checkNotDefault() {
        if (this == CONSOLE) {
            throw new IllegalStateException("Cannot modify default Console object.");
        }
    }

    public void setStreams(final PrintStream stream) {
        checkNotDefault();
        out = err = stream;
    }

    public void setOutputStream(final PrintStream output) {
        checkNotDefault();
        out = output;
    }

    public void setErrorStream(final PrintStream error) {
        checkNotDefault();
        err = error;
    }

    /**
     * Log the given message to the "output" stream.
     *
     * @param message The message to log.
     * @see #logOutput
     */
    public void log(final String message) {
        logOutput(message);
    }

    /**
     * Log the given exception (basically print the stack trace) to the "error" stream.
     *
     * @param t The exception to log.
     */
    public void log(final Throwable t) {
        if (t != null) {
            t.printStackTrace(err);
        }
    }

    /**
     * Get a user-friendly message from the given exception.
     *
     * @param t The throwable (exception) in question.
     * @return Start with the localized exception message, but use the
     * simple name of the exception if there is no message, or if the
     * exception is one of a short list of "funny" exceptions where the
     * message by itself is ambiguous, prepend the simple name of the
     * exception to the message.
     */
    public static String getExceptionMessage(final Throwable t) {
        String msg = t.getLocalizedMessage();
        if (msg == null || msg.isEmpty()) {
            msg = t.getClass().getSimpleName();
        } else if ((t instanceof UnknownHostException)
                || (t instanceof NoClassDefFoundError)
                || (t instanceof ClassNotFoundException)
                || (t instanceof NullPointerException)
                || (t instanceof CharacterCodingException)
                || (t instanceof FileNotFoundException)
                || (t instanceof NoSuchFileException)) {
            msg = String.format("%1$s: %2$s", t.getClass().getSimpleName(), msg);
        }
        return msg;
    }

    public void logExceptionMessage(final Throwable t) {
        logOutput(getExceptionMessage(t));
    }

    public void logOutput(final String message) {
        out.println(message == null ? NULL : message);
    }

    public void logError(final String message) {
        err.println(message == null ? NULL : message);
    }

    /**
     * Log a message along with the calling method's name to the system console.
     *
     * @param message The message to be logged to {@link System#out},
     * or a format string using the remaining args (can be {@code null}).
     * @param args The optional arguments used to format the final message.
     */
    public void logMethod(final String message, final Object... args) {
        logOutput(
              ClassUtils.getCallingMethod(1)
            + ": "
            + (message == null ? NULL : String.format(message, args))
        );
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
    public void logMethod(final String prefix, final String message, final Object... args) {
        logOutput(
              (prefix == null ? "" : prefix + " ")
            + ClassUtils.getCallingMethod(1)
            + ": "
            + (message == null ? NULL : String.format(message, args))
        );
    }

}
