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

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a set of path operations.
 */
public class PathGeometry {
    /**
     * Abstract base class for path operations.
     */
    public static abstract class Operation {
        /**
         * Appends this operation to the native path geometry.
         *
         * @param nativePathGeometry
         */
        protected abstract void append(Object nativePathGeometry);
    }

    /**
     * Moves to the given location.
     */
    public static final class MoveTo extends Operation {
        public final Point location;

        public MoveTo(Point location) {
            if (location == null) {
                throw new IllegalArgumentException();
            }

            this.location = location;
        }

        @Override
        protected void append(Object nativePathGeometry) {
            // TODO Call Platform#appendMoveTo(Object)
        }
    }

    // TODO Add more concrete operations

    /**
     * Enumeration representing a fill rule.
     */
    public enum FillRule {
        EVEN_ODD,
        WINDING
    }

    // TODO Wrap in inner class so we can clear nativePathGeometry when modified
    private ArrayList<Operation> operations = new ArrayList<Operation>();
    private FillRule fillRule = FillRule.WINDING;

    private Object nativePathGeometry = null;

    public List<Operation> getOperations() {
        return operations;
    }

    public FillRule getFillRule() {
        return fillRule;
    }

    public void setFillRule(FillRule fillRule) {
        if (fillRule == null) {
            throw new IllegalArgumentException("fillRule is null.");
        }

        this.fillRule = fillRule;

        nativePathGeometry = null;
    }

    public Object getNativePathGeometry() {
        if (nativePathGeometry == null) {
            nativePathGeometry = Platform.getPlatform().getNativePathGeometry(this);
        }

        return nativePathGeometry;
    }
}
