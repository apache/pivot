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
package org.apache.pivot.wtk.skin;

import java.util.Locale;

import org.apache.pivot.wtk.text.NumberedList;
import org.apache.pivot.wtk.text.NumberedList.Style;
import org.apache.pivot.wtk.text.NumberedListListener;

class TextPaneSkinNumberedListView extends TextPaneSkinListView implements NumberedListListener {

    private static class RomanValue {
        public final int integerVal;
        public final String romanNumeral;

        public RomanValue(int integerVal, String romanNumeral) {
            this.integerVal = integerVal;
            this.romanNumeral = romanNumeral;
        }
    }

    private static final RomanValue[] ROMAN_VALUE_TABLE = {
        new RomanValue(1000, "M"),
        new RomanValue(900, "CM"),
        new RomanValue(500, "D"),
        new RomanValue(400, "CD"),
        new RomanValue(100, "C"),
        new RomanValue(90, "XC"),
        new RomanValue(50, "L"),
        new RomanValue(40, "XL"),
        new RomanValue(10, "X"),
        new RomanValue(9, "IX"),
        new RomanValue(5, "V"),
        new RomanValue(4, "IV"),
        new RomanValue(1, "I")
     };

    private static String int2roman(int n) {
        StringBuffer result = new StringBuffer(10);

        // ... Start with largest value, and work toward smallest.
        for (RomanValue equiv : ROMAN_VALUE_TABLE) {
            // ... Remove as many of this value as possible (maybe none).
            while (n >= equiv.integerVal) {
                n -= equiv.integerVal;
                result.append(equiv.romanNumeral);
            }
        }
        return result.toString();
    }

    private static String int2alpha(int n) {
        return (char)('A' + n - 1) + "";
    }

    public TextPaneSkinNumberedListView(NumberedList numberedList) {
        super(numberedList);
    }

    @Override
    protected void attach() {
        super.attach();

        NumberedList numberedList = (NumberedList)getNode();
        numberedList.getNumberedListListeners().add(this);
    }

    @Override
    protected void detach() {
        super.detach();

        NumberedList numberedList = (NumberedList)getNode();
        numberedList.getNumberedListListeners().remove(this);
    }

    @Override
    protected void childLayout(int breakWidth) {
        NumberedList numberedList = (NumberedList)getNode();

        int index = 1;
        for (TextPaneSkinNodeView nodeView : this) {
            TextPaneSkinListItemView listItemView = (TextPaneSkinListItemView)nodeView;

            switch (numberedList.getStyle()) {
                case DECIMAL:
                    listItemView.setIndexText(index + ". ");
                    break;
                case LOWER_ALPHA:
                    listItemView.setIndexText(int2alpha(index).toLowerCase(Locale.ENGLISH) + ". ");
                    break;
                case UPPER_ALPHA:
                    listItemView.setIndexText(int2alpha(index) + ". ");
                    break;
                case LOWER_ROMAN:
                    listItemView.setIndexText(int2roman(index).toLowerCase(Locale.ENGLISH) + ". ");
                    break;
                case UPPER_ROMAN:
                    listItemView.setIndexText(int2roman(index) + ". ");
                    break;
            }

            index++;
        }

        this.maxIndexTextWidth = 0;
        for (TextPaneSkinNodeView nodeView : this) {
            TextPaneSkinListItemView listItemView = (TextPaneSkinListItemView)nodeView;
            this.maxIndexTextWidth = Math.max(this.maxIndexTextWidth,
                listItemView.getIndexTextWidth());
        }

        super.childLayout(breakWidth);
    }

    @Override
    public void styleChanged(NumberedList numberedList, Style previousStyle) {
        invalidateUpTree();
    }
}
