/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Sequence;

/**
 * Interface representing a data source. Data sources provide content for the
 * clipboard and drag/drop operations.
 * <p>
 * Callers can use the iterator to enumerate the list of MIME types supported
 * by the data source.
 *
 * @author gbrown
 */
public interface DataSource extends Iterable<String> {
    /**
     * Returns the value corresponding to the given MIME type and class.
     *
     * @param mimeType
     * The MIME type of the value to return.
     *
     * @param type
     * The class of the object to return, or <tt>null</tt> to return an
     * instance of the default type for the given MIME type.
     *
     * @return
     * The value for the given MIME type and class (if provided), or
     * <tt>null</tt> if no such value exists.
     */
    public Object get(String mimeType, Class<?> type);

    /**
     * Tests for the existence of a value with a given MIME type and class
     * in this data source.
     *
     * @param mimeType
     * The MIME type to test.
     *
     * @param type
     * The class type to test, or <tt>null</tt> to test for the existence of
     * the MIME type only.
     *
     * @return
     * <tt>true</tt> if a value with the given MIME type and class (if
     * provided) exists; <tt>false</tt>, otherwise.
     */
    public boolean contains(String mimeType, Class<?> type);

    /**
     * Returns the supported classes for a given MIME type.
     *
     * @param mimeType
     * The MIME type whose classes are to be returned.
     *
     * @return
     * A sequence containing the types available for the given MIME type, or
     * <tt>null</tt> if the MIME type is not supported.
     */
    public Sequence<Class<?>> getTypes(String mimeType);
}
