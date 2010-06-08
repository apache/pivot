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
package org.apache.pivot.tutorials.webqueries;

import java.math.BigDecimal;

import org.apache.pivot.wtk.validation.Validator;

/**
 * Verifies that text represents a valid dollar value.
 */
public class AmountValidator implements Validator {
    @Override
    public boolean isValid(String text) {
        boolean valid = true;

        if (text.length() > 0) {
            try {
                BigDecimal numericAmount = new BigDecimal(text);
                valid = (numericAmount.scale() <= 2 && numericAmount.signum() >= 0);
            } catch (NumberFormatException ex) {
                valid = false;
            }
        }

        return valid;
    }
}
