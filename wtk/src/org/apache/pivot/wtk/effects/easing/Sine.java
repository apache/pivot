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
 * Easing operation based on a sine curve.
 */
public class Sine implements Easing {
    @Override
    public float easeIn(float time, float begin, float change, float duration) {
        return -change * (float)Math.cos(time / duration * (Math.PI/2)) + change + begin;
    }

    @Override
    public float easeOut(float time, float begin, float change, float duration) {
        return change * (float)Math.sin(time / duration * (Math.PI/2)) + begin;
    }

    @Override
    public float easeInOut(float time, float begin, float change, float duration) {
        return -change / 2f * (float)(Math.cos(Math.PI * time / duration) - 1) + begin;
    }
}
