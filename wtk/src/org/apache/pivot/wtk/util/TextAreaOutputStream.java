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
package org.apache.pivot.wtk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.TextArea;

/**
 * Creates an {@link OutputStream} that outputs to a {@link TextArea}
 * (in the EDT thread, using callbacks) for display.
 * <p> Can be used with the {@link org.apache.pivot.util.Console} class for output (using the
 * {@link #toPrintStream} method).
 */
public final class TextAreaOutputStream extends OutputStream {
    /** The TextArea we are going to stream to. */
    private TextArea textArea;

    /** The buffered line for this stream. */
    private ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream(256);

    /**
     * Only constructor given the {@link TextArea} to stream to.
     *
     * @param textAreaToUse The TextArea to use for output.
     */
    public TextAreaOutputStream(final TextArea textAreaToUse) {
        this.textArea = textAreaToUse;
    }

    /**
     * @throws IOException if this stream is already closed.
     */
    private void checkIfOpen() throws IOException {
        if (textArea == null || lineBuffer == null) {
            throw new IOException("TextAreaOutputStream is closed.");
        }
    }

    /**
     * Flush the (byte) line buffer if there is anything cached.
     * @param addNewLine If there is anything to flush, also add a newline
     * ('\n') character at the end.
     */
    private void flushLineBuffer(final boolean addNewLine) {
        if (lineBuffer.size() > 0) {
            byte[] bytes = lineBuffer.toByteArray();
            // TODO: should we have a charset to use here??
            String text = new String(bytes);
            int length = textArea.getCharacterCount();
            textArea.insertText(text, length);
            if (addNewLine) {
                int newLength = length + text.length();
                textArea.insertText("\n", newLength);
            }
            lineBuffer.reset();
            Bounds beginningOfLineBounds = textArea.getCharacterBounds(length);
            ApplicationContext.queueCallback(() -> textArea.scrollAreaToVisible(beginningOfLineBounds));
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        this.textArea = null;
        this.lineBuffer = null;
    }

    @Override
    public void flush() throws IOException {
        checkIfOpen();
        flushLineBuffer(false);
    }

    @Override
    public void write(final int b) throws IOException {
        if (b == '\n') {
            flushLineBuffer(true);
        } else if (b != '\r') {
            lineBuffer.write(b);
        }
    }

    /**
     * @return A new {@link PrintStream} using this object as the basis.
     */
    public PrintStream toPrintStream() {
        return new PrintStream(this);
    }

}
