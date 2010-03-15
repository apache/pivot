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
package org.apache.pivot.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Reader that echoes characters to the console as they are read.
 */
public class EchoReader extends Reader {
    private Reader in;
    private Writer echo;

    public EchoReader(Reader in) {
        this(in, new PrintWriter(System.out));
    }

    public EchoReader(Reader in, Writer echo) {
        if (in == null) {
            throw new IllegalArgumentException();
        }

        if (echo == null) {
            throw new IllegalArgumentException();
        }

        this.in = in;
        this.echo = echo;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = in.read(cbuf, off, len);
        echo.write(cbuf, off, len);

        return n;
    }
}
