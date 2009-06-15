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

/**
 * Cubic curve listener interface.
 *
 * @author gbrown
 */
public interface CubicCurveListener {
    /**
     * Called when a cubic curve's endpoints have changed.
     *
     * @param cubicCurve
     * @param previousX1
     * @param previousY1
     * @param previousX2
     * @param previousY2
     */
    public void endpointsChanged(CubicCurve cubicCurve, int previousX1, int previousY1,
        int previousX2, int previousY2);

    /**
     * Called when a cubic curve's control points have changed.
     *
     * @param cubicCurve
     * @param previousControlX1
     * @param previousControlY1
     * @param previousControlX2
     * @param previousControlY2
     */
    public void controlPointsChanged(CubicCurve cubicCurve, int previousControlX1,
        int previousControlY1, int previousControlX2, int previousControlY2);
}
