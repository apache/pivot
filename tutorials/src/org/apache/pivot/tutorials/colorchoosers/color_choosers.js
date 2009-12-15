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
    colorChooser.selectedColor = "#59a2b0";
}

/**
 * Converts a hex string into a Color instance.
 */
function hexToColor(hex) {
    if (!hex.startsWith("#")) {
        hex = "#" + hex;
    }

    return java.awt.Color.decode(hex);
}

/**
 * Converts a Color instance into a hex string.
 */
function colorToHex(color) {
    var result = "";

    var primaries = ["red", "green", "blue"];
    for (var i = 0, n = primaries.length; i < n; i++) {
        var value = color[primaries[i]].toString(16);
        if (value.length == 1) {
            // Pad the value with a leading zero
            value = "0" + value;
        }
        result += value;
    }

    return result;
}

/**
 * Called when the selected color changes.
 */
function onColorChange() {
    var color = colorChooser.selectedColor;
    sampleBorder.styles.put("backgroundColor", color);
    hexInput.text = colorToHex(color);
}

/**
 * Called when the hex input changes its focus state.
 */
function onInputFocusChange() {
    if (!hexInput.focused) {
        try {
            colorChooser.selectedColor = hexToColor(hexInput.text);
        } catch (ex) {
            var color = colorChooser.selectedColor;
            if (color) {
                hexInput.text = colorToHex(color);
            }
        }
    }
}
