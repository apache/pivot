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
package pivot.wtk.media.drawing;

/**
 * Arc listener interface.
 *
 * @author gbrown
 */
public interface ArcListener {
    /**
     * Called when an arc's size has changed.
     *
     * @param arc
     * @param previousWidth
     * @param previousHeight
     */
    public void sizeChanged(Arc arc, int previousWidth, int previousHeight);

    /**
     * Called when an arc's start angle has changed.
     * @param arc
     * @param previousStart
     */
    public void startChanged(Arc arc, float previousStart);

    /**
     * Called when an arc's extent has changed.
     *
     * @param arc
     * @param previousExtent
     */
    public void extentChanged(Arc arc, float previousExtent);

    /**
     * Called when an arc's closure type has changed.
     *
     * @param arc
     * @param previousType
     */
    public void typeChanged(Arc arc, Arc.Type previousType);
}
