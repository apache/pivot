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
package org.apache.pivot.scene.effect;

import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Node;
import org.apache.pivot.scene.Transform;

/**
 * Decorator that applies a mask shape to a node.
 */
public class MaskDecorator implements Decorator {
    private Node mask;

    public MaskDecorator(Node mask) {
        if (mask == null) {
            throw new IllegalArgumentException("mask is null.");
        }

        this.mask = mask;
    }

    public Node getMask() {
        return mask;
    }

    @Override
    public Graphics prepare(Node node, Graphics graphics) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub

    }

    @Override
    public Extents getExtents(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transform getTransform(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
}
