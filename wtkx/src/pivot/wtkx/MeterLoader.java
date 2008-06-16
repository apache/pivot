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
package pivot.wtkx;

import org.w3c.dom.Element;

import pivot.wtk.Component;
import pivot.wtk.Meter;

class MeterLoader extends Loader {
    public static final String METER_TAG = "Meter";
    public static final String PERCENTAGE_ATTRIBUTE = "percentage";
    public static final String TEXT_ATTRIBUTE = "text";

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Meter meter = new Meter();

        double percentage = 0;
        if (element.hasAttribute(PERCENTAGE_ATTRIBUTE)) {
            String percentageAttribute = element.getAttribute(PERCENTAGE_ATTRIBUTE);
            percentage = Double.parseDouble(percentageAttribute);
        }

        meter.setPercentage(percentage);

        String textAttribute = element.getAttribute(TEXT_ATTRIBUTE);
        meter.setText(rootLoader.resolve(textAttribute).toString());

        return meter;
    }
}
