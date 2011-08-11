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
package org.apache.pivot.wtk.text;


/**
 * Element representing an inline range of styled characters.
 * @deprecated class has been renamed to TextSpan
 */
@Deprecated
public class Span extends TextSpan {
    public Span() {
        super();
    }

    public Span(Span span, boolean recursive) {
        super(span, recursive);
    }

    @Override
    public Span duplicate(boolean recursive) {
        return new Span(this, recursive);
    }

    @Override
    public Span getRange(int offset, int characterCount) {
        return (Span) super.getRange(offset, characterCount);
    }
}
