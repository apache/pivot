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
package org.apache.pivot.wtk.content;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.ListView;


/**
 * A {@link ListView.ItemBindMapping} that loads and stores just
 * the index itself instead of the selected item.  This is a convenience
 * class for users where the data stored is just the index of the item.
 */
public class ListViewIndexBindMapping implements ListView.ItemBindMapping
{
	/**
	 * Called during <tt>load</tt>, and <tt>value</tt> is what is
	 * stored in our data object (which is the <tt>Integer</tt>
	 * index value).
	 *
	 * @param listData The <tt>ListView</tt>'s data list.
	 * @param value The object value to map to an index in this list
	 * (which is an <tt>Integer</tt> value).
	 * @return The value converted to an integer, or <tt>-1</tt>
	 * if the value is out of range of the list size.
	 */
	@Override
	public int indexOf(List<?> listData, Object value) {
	    if (value instanceof Integer) {
		int iValue = ((Integer)value).intValue();
		if (iValue >= -1 && iValue < listData.getLength()) {
		    return iValue;
		}
	    }
	    return -1;
	}

	/**
	 * Called during <tt>store</tt>, and <tt>index</tt> is the
	 * selected item index.  We are going to just return an
	 * <tt>Integer</tt> representing the index itself.
	 *
	 * @param listData The underlying data for the <tt>ListView</tt>.
	 * @param index The index value to convert to a "storable" value.
	 * @return The <tt>Integer</tt> value of the index.
	 */
	@Override
	public Object get(List<?> listData, int index) {
	    return Integer.valueOf(index);
	}

}
