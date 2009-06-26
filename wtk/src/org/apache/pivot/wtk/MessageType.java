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

/**
 * Enumeration defining a message's type.
 *
 * @author gbrown
 */
public enum MessageType {
    ERROR,
    WARNING,
    QUESTION,
    INFO,
    APPLICATION;

    public static MessageType decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        MessageType messageType;
        if (value.equals("error")) {
            messageType = ERROR;
        } else if (value.equals("warning")) {
            messageType = WARNING;
        } else if (value.equals("question")) {
            messageType = QUESTION;
        } else if (value.equals("info")) {
            messageType = INFO;
        } else if (value.equals("application")) {
            messageType = APPLICATION;
        } else {
            messageType = valueOf(value);
        }

        return messageType;
    }
}
