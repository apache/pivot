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

    /**
     * Default constructor for this component, with the default {@link Origin}
     * value (which is <tt>NORTH</tt>).
     */
    public Gauge() {
       this(Origin.NORTH);
    }

    /**
     * Constructor for a new gauge object, using the given {@link Origin} value.
     * @param origin The "origin" or zero point of the gauge (a compass direction).
     */
    public Gauge(Origin origin) {
        setOrigin(origin);
        installSkin(Gauge.class);
    }

    public Origin getOrigin() {
        return this.origin;
    }

    /**
     * Set the "origin" value for this gauge, that is, the point in the circle where
     * drawing of the value starts (one of the main compass directions).  The gauge
     * value will always be drawn clockwise starting from the origin location.
     * @param origin The new origin value.
     * @throws IllegalArgumentException if the origin value is {@code null}.
     */
    public void setOrigin(Origin origin) {
        Utils.checkNull(origin, "origin");

        Origin previousOrigin = this.origin;

        if (previousOrigin != origin) {
            this.origin = origin;
            gaugeListeners.originChanged(this, previousOrigin);
        }
    }

    /**
     * Since this is a generic component that can take any numeric type, for BXML we need
     * to specify the specific type of the value (which is also needed for the min/max and
     * warning/critical values).
     * @param typeName The type name for the value of this component.  For convenience you can
     * specify just "Integer", "Short", "BigDecimal", or any other subclass of {@link Number}
     * here, or you can give the fully-qualified name.
     */
    @SuppressWarnings("unchecked")
    public void setType(String typeName) {
        try {
            this.clazz = (Class<? extends Number>)((typeName.indexOf('.') < 0) ?
                Class.forName("java.lang." + typeName) :
                Class.forName(typeName));
        } catch (ClassNotFoundException cnfe) {
            if (typeName.indexOf('.') < 0) {
                // Try "java.math" (for BigDecimal, etc.) types
                try {
                    this.clazz = (Class<? extends Number>)Class.forName("java.math." + typeName);
                } catch (ClassNotFoundException cnfe2) {
                    throw new RuntimeException(cnfe);
                }
            }
        }
    }

    /**
     * If the {@link #clazz} was set by a prior call to {@link #setType setType()} then
     * check the runtime class of the value against it, otherwise call {@link #setType setType()}
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

    /**
     * @return The current (single) value assigned to this component.
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the current value displayed by the gauge.
     * @param value The new value, of the same type as declared for the gauge.
     * @throws ClassCastException if the type of this value is not what was given
     * by the {@link #setType setType()} call or any previous call to the <tt>"setXXXValue"</tt>
     * methods (which all call {@link #setOrCheckClass}).
     */
    public void setValue(T value) {
        Utils.checkNull(value, "value");
        setOrCheckClass(value);

        T previousValue = this.value;

        if (value != previousValue) {
            this.value = value;
            gaugeListeners.valueChanged(this, previousValue);
        }
    }

    /**
     * Used by BXML to set the value.  Converts String to a Number using the
     * {@link StringUtils#toNumber StringUtils.toNumber()} method, giving the class value set by
     * {@link #setType setType()}.
     * @param value The string value to convert to a number.
     * @see #setValue
     */
    @SuppressWarnings("unchecked")
    public void setValue(String value) {
        setValue((T)StringUtils.toNumber(value, clazz));
    }

    public T getMinValue() {
        return minValue;
    }

    /**
     * Set the minimum value for this gauge, which will correspond to the "origin"
     * value of the display.  Values given by {@link #setValue} will be offset by
     * this minimum value before display.
     * @param minValue The new minimum value for the gauge.
     * @throws ClassCastException if this value is not null and not of the same
     * type as previous values set on this gauge.
     */
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

    /**
     * Used by BXML to set the minimum value.  Converts string to number using the
     * {@link StringUtils#toNumber StringUtils.toNumber()} method.
     * @param minValue The string value to convert to a minimum value.
     * @throws IllegalArgumentException if the input is null or empty.
     */
    @SuppressWarnings("unchecked")
    public void setMinValue(String minValue) {
        setMinValue((T)StringUtils.toNumber(minValue, clazz));
    }

    public T getMaxValue() {
        return maxValue;
    }

    /**
     * Set the maximum value for this gauge, which will correspond to 360 degrees
     * after the origin of the display. Values given by {@link #setValue} will be
     * limited to this maximum value for display.
     * @param maxValue The new maximum value for the gauge.
     * @throws ClassCastException if this value is not null and not of the same
     * type as previous values set on this gauge.
     */
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

    /**
     * Used by BXML to set the maximum value.  Converts string to number using the
     * {@link StringUtils#toNumber StringUtils.toNumber()} method.
     * @param maxValue The string value to convert to a maximum value.
     * @throws IllegalArgumentException if the input is null or empty.
     */
    @SuppressWarnings("unchecked")
    public void setMaxValue(String maxValue) {
        setMaxValue((T)StringUtils.toNumber(maxValue, clazz));
    }

    public T getWarningLevel() {
        return this.warningLevel;
    }

    /**
     * Set a level at which the gauge will start showing "warning" indication
     * (basically the warning color set in the skin).
     * <p> If the warning (or critical) value is set to <tt>null</tt>, and/or
     * the warning (or critical) color is set to <tt>null</tt> then the display
     * will always be the same color, no matter the value.
     * <p> The assumption is that the warning level will be less than the critical
     * level, and both will be in the range of the min - max values (this is not
     * currently checked anywhere).
     * @param warningLevel A value of the same type as the other values for
     * this gauge at which to set the warning threshold (can be {@code null}).
     */
    public final void setWarningLevel(T warningLevel) {
        if (warningLevel != null) {
            setOrCheckClass(warningLevel);
        }

        this.warningLevel = warningLevel;
    }

    /**
     * Set the warning level from a string representation of a number.
     * @param warningLevel New value.
     * @see #setValue(String)
     */
    @SuppressWarnings("unchecked")
    public void setWarningLevel(String warningLevel) {
        setWarningLevel((T)StringUtils.toNumber(warningLevel, clazz));
    }

    public T getCriticalLevel() {
        return this.criticalLevel;
    }

    /**
     * Set a level at which the gauge will start showing "critical" indication
     * (basically the critical color set in the skin).
     * <p> If the critical (or warning) value is set to <tt>null</tt>, and/or
     * the critical (or warning) color is set to <tt>null</tt> then the display
     * will always be the same color, no matter the value.
     * <p> The assumption is that the warning level will be less than the critical
     * level, and both will be in the range of the min - max values (this is not
     * currently checked anywhere).
     * @param criticalLevel A value of the same type as the other values for
     * this gauge at which to set the critical threshold (can be {@code null}).
     */
    public final void setCriticalLevel(T criticalLevel) {
        if (criticalLevel != null) {
            setOrCheckClass(criticalLevel);
        }

        this.criticalLevel = criticalLevel;
    }

    /**
     * Set the critical level from a string representation of a number.
     * @param criticalLevel New value.
     * @see #setValue(String)
     */
    @SuppressWarnings("unchecked")
    public void setCriticalLevel(String criticalLevel) {
        setCriticalLevel((T)StringUtils.toNumber(criticalLevel, clazz));
    }

    public String getText() {
        return this.text;
    }

    /**
     * Set the text string to be displayed in the middle of the gauge.
     * @param text The new text to be shown (can be <tt>null</tt> or empty).
     */
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
