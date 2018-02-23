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
package org.apache.pivot.wtk;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;

/**
 * A circular gauge component that can display a single value of an arbitrary
 * numeric type.
 */
public class Gauge<T extends Number> extends Component {
    private Origin origin;
    private String text;
    private T value;
    private T minValue;
    private T maxValue;
    private T warningLevel;
    private T criticalLevel;
    private GaugeListener.Listeners<T> gaugeListeners = new GaugeListener.Listeners<T>();
    /** Runtime class (used to check values at runtime). */
    private Class<? extends Number> clazz;

    public Gauge() {
       this(Origin.NORTH);
    }

    public Gauge(Origin origin) {
        this.origin = origin;
        installSkin(Gauge.class);
    }

    public Origin getOrigin() {
        return this.origin;
    }

    public void setOrigin(Origin origin) {
        Utils.checkNull(origin, "origin");

        Origin previousOrigin = this.origin;

        if (previousOrigin != origin) {
            this.origin = origin;
            gaugeListeners.originChanged(this, previousOrigin);
        }
    }

    @SuppressWarnings("unchecked")
    public void setType(String type) {
        try {
            this.clazz = (Class<? extends Number>)((type.indexOf('.') < 0) ? Class.forName("java.lang." + type) :
                Class.forName(type));
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    /**
     * If the {@link #clazz} was set by a prior call to {@link #setType} then
     * check the runtime class of the value against it, otherwise call {@link #setType}
     * to establish it for the future.
     *
     * @param value A value presumably compatible with the declared type of this gauge.
     * @throws ClassCastException if the value is not compatible with the previously
     * established type.
     */
    private void setOrCheckClass(T value) {
        if (this.clazz != null) {
            if (!clazz.isInstance(value)) {
                throw new ClassCastException("Value is not an instance of " + clazz.getName());
            }
        } else {
            setType(value.getClass().getName());
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        Utils.checkNull(value, "value");
        setOrCheckClass(value);

        T previousValue = this.value;

        if (value != previousValue) {
            this.value = value;
            gaugeListeners.valueChanged(this, previousValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void setValue(String value) {
        setValue((T)StringUtils.toNumber(value, clazz));
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        if (minValue != null) {
            setOrCheckClass(minValue);
        }

        T previousMinValue = this.minValue;

        if (minValue != previousMinValue) {
            this.minValue = minValue;
            gaugeListeners.minMaxValueChanged(this, previousMinValue, this.maxValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void setMinValue(String minValue) {
        setMinValue((T)StringUtils.toNumber(minValue, clazz));
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        if (maxValue != null) {
            setOrCheckClass(maxValue);
        }

        T previousMaxValue = this.maxValue;

        if (previousMaxValue != maxValue) {
            this.maxValue = maxValue;
            gaugeListeners.minMaxValueChanged(this, this.minValue, previousMaxValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void setMaxValue(String maxValue) {
        setMaxValue((T)StringUtils.toNumber(maxValue, clazz));
    }

    public T getWarningLevel() {
        return this.warningLevel;
    }

    public final void setWarningLevel(T warningLevel) {
        if (warningLevel != null) {
            setOrCheckClass(warningLevel);
        }

        this.warningLevel = warningLevel;
    }

    @SuppressWarnings("unchecked")
    public void setWarningLevel(String warningLevel) {
        setWarningLevel((T)StringUtils.toNumber(warningLevel, clazz));
    }

    public T getCriticalLevel() {
        return this.criticalLevel;
    }

    public final void setCriticalLevel(T criticalLevel) {
        if (criticalLevel != null) {
            setOrCheckClass(criticalLevel);
        }

        this.criticalLevel = criticalLevel;
    }

    @SuppressWarnings("unchecked")
    public void setCriticalLevel(String criticalLevel) {
        setCriticalLevel((T)StringUtils.toNumber(criticalLevel, clazz));
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        // Null text is allowed
        String previousText = this.text;

        if ((previousText == null && text != null) ||
            (previousText != null && text == null) ||
            (!previousText.equals(text))) {
            this.text = text;
            gaugeListeners.textChanged(this, previousText);
        }
    }

    public ListenerList<GaugeListener<T>> getGaugeListeners() {
        return gaugeListeners;
    }
}
