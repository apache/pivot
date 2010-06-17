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
package org.apache.pivot.wtkx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that causes a loaded WTKX element to be bound to the annotated
 * field.
 *
 * @deprecated
 * This class has been moved to {@link org.apache.pivot.beans.BXML}.
 * You can use the <tt>bxml_upgrade.xml</tt> Ant script to update your
 * source code to use the new classes. Usage:
 * <p>
 * <tt>ant -f bxml_upgrade.xml -Dsrc=&lt;sourcedir&gt;</tt>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface WTKX {
    /**
     * The ID of the WTKX variable that references the element to bind. It
     * should be a valid <tt>wtkx:id</tt> from the loaded WTKX resource. If
     * unspecified, the name of the annotated field will be used.
     */
    public String id() default "\0";
}