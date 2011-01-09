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
package org.apache.pivot.scene;

/**
 * Class representing an affine transform.
 */
public class Transform {
    public final float m11;
    public final float m12;
    public final float m21;
    public final float m22;
    public final float dx;
    public final float dy;

    public Transform(float m11, float m12, float m21, float m22, float dx, float dy) {
        this.m11 = m11;
        this.m12 = m12;
        this.m21 = m21;
        this.m22 = m22;

        this.dx = dx;
        this.dy = dy;
    }

    public Transform scale(float x, float y) {
        // TODO
        return null;
    }

    public Transform rotate(float angle) {
        // TODO
        return null;
    }

    public Transform translate(float x, float y) {
        // TODO
        return null;
    }

    public boolean isIdentity() {
        // TODO
        return true;
    }

    @Override
    public boolean equals(Object o) {
        // TODO
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // TODO
        return super.hashCode();
    }
}
