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
package org.apache.pivot.wtk.effects.easing;

/**
 * Exponential easing operation.
 */
public class Exponential implements Easing {
    @Override
    public float easeIn(float time, float begin, float change, float duration) {
        return (time == 0) ? begin
            : change * (float)Math.pow(2, 10 * (time / duration - 1f)) + begin;
    }

    @Override
    public float easeOut(float time, float begin, float change, float duration) {
        return (time == duration) ? begin + change
            : change * ((float)-Math.pow(2, -10 * time / duration) + 1f) + begin;
    }

    @Override
    public float easeInOut(float time, float begin, float change, float duration) {
        if (time == 0) {
            return begin;
        }

        if (time == duration) {
            return begin + change;
        }

        if ((time /= duration / 2f) < 1) {
            return change / 2f * (float)Math.pow(2, 10 * (time - 1)) + begin;
        }

        return change / 2f * ((float)-Math.pow(2, -10 * (time - 1)) + 2) + begin;
    }
}
