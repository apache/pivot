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

/**
 * Called when the main app window is opened.
 */
function init() {
    ranges.selection = weekButton;
}

/**
 * Updates the scroll bar's extent and block increment based on the selected
 * range (in the ranges button group).
 */
function updateRange() {
    var amount;

    if (ranges.selection == dayButton) {
        amount = 1;
    } else if (ranges.selection == weekButton) {
        amount = 7;
    } else if (ranges.selection == fortnightButton) {
        amount = 14;
    } else {
        amount = 30;
    }

    scrollBar.extent = scrollBar.unitIncrement = amount;
    scrollBar.blockIncrement = 2 * amount;
}

/**
 * Updates the "timeline" label based on the scroll bar's value and extent.
 */
function updateLabel() {
    var first = scrollBar.value + 1;
    var last = scrollBar.value + scrollBar.extent;
    label.setText("Days " + first + " through " + last);
}
