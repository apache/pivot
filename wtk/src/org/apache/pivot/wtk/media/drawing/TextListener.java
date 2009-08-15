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
package org.apache.pivot.wtk.media.drawing;

import java.awt.Font;

import org.apache.pivot.wtk.HorizontalAlignment;


/**
 * Text listener interface.
 *
 */
public interface TextListener {
    /**
     * Called when a text shape's text has changed.
     *
     * @param text
     * @param previousText
     */
    public void textChanged(Text text, String previousText);

    /**
     * Called when a text shape's font has changed.
     *
     * @param text
     * @param previousFont
     */
    public void fontChanged(Text text, Font previousFont);

    /**
     * Called when a text shape's width has changed.
     *
     * @param text
     * @param previousWidth
     */
    public void widthChanged(Text text, int previousWidth);

    /**
     * Called when a text shape's horizontal alignment has changed.
     *
     * @param text
     * @param previousAlignment
     */
    public void alignmentChanged(Text text, HorizontalAlignment previousAlignment);
}
