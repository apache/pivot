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
package org.apache.pivot.charts;

public class HistogramView extends ChartView {

    public HistogramView() {
        installSkin(HistogramView.class);
    }

    public static class HistogramBin extends Element {
        private final double binStart;
        private final double binValue;
        private final double binEnd;
        private final double frequency;

        public HistogramBin(int series, int item, double binStart,
                            double binValue, double binEnd, double frequency) {
            super(series,item);
            this.binEnd = binEnd;
            this.binStart = binStart;
            this.binValue = binValue;
            this.frequency = frequency;			
        }

        public double getBinStart() {
            return binStart;
        }

        public double getBinValue() {
            return binValue;
        }

        public double getBinEnd() {
            return binEnd;
        }

        public double getFrequency() {
            return frequency;
        }

        public String toString() {
            return "bin #"+getElementIndex()+","+binStart+" - "+binEnd+", frequency "+frequency;
        }
	
    }

}

