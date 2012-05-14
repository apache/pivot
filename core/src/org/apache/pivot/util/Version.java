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
package org.apache.pivot.util;

import java.io.Serializable;

/**
 * Represents a version number. Version numbers are defined as:
 * <p>
 * <i>major</i>.<i>minor</i>.<i>maintenance</i>_<i>update</i>
 * <p>
 * for example, "JDK 1.6.0_10".
 */
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3677773163272115116L;

    private byte majorRevision = 0;
    private byte minorRevision = 0;
    private byte maintenanceRevision = 0;
    private byte updateRevision = 0;
    private String build = null;

    public Version(int majorRevision, int minorRevision, int maintenanceRevision,
        int updateRevision) {
        this(majorRevision, minorRevision, maintenanceRevision, updateRevision, null);
    }

    public Version(int majorRevision, int minorRevision, int maintenanceRevision,
        int updateRevision, String build) {
        if (majorRevision > 0x7f) {
            throw new IllegalArgumentException("majorRevision must be less than "
                + 0x7f + ".");
        }

        if (minorRevision > 0xff) {
            throw new IllegalArgumentException("minorRevision must be less than "
                + 0xff + ".");
        }

        if (maintenanceRevision > 0xff) {
            throw new IllegalArgumentException("maintenanceRevision must be less than "
                + 0xff + ".");
        }

        if (updateRevision > 0xff) {
            throw new IllegalArgumentException("updateRevision must be less than "
                + 0xff + ".");
        }

        this.majorRevision = (byte)majorRevision;
        this.minorRevision = (byte)minorRevision;
        this.maintenanceRevision = (byte)maintenanceRevision;
        this.updateRevision = (byte)updateRevision;
        this.build = build;
    }

    public byte getMajorRevision() {
        return this.majorRevision;
    }

    public byte getMinorRevision() {
        return this.minorRevision;
    }

    public byte getMaintenanceRevision() {
        return this.maintenanceRevision;
    }

    public byte getUpdateRevision() {
        return this.updateRevision;
    }

    public int getNumber() {
        int number = ((this.majorRevision) & 0xff) << (8 * 3)
            | ((this.minorRevision) & 0xff) << (8 * 2)
            | ((this.maintenanceRevision) & 0xff) << (8 * 1)
            | ((this.updateRevision) & 0xff) << (8 * 0);

        return number;
    }

    @Override
    public int compareTo(Version version) {
        return (getNumber() - version.getNumber());
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Version
            && compareTo((Version)object) == 0);
    }

    @Override
    public int hashCode() {
        return getNumber();
    }

    @Override
    public String toString() {
        String string = this.majorRevision
            + "." + this.minorRevision
            + "." + this.maintenanceRevision
            + "_" + String.format("%02d", this.updateRevision);

        if (this.build != null) {
            string += "-" + this.build;
        }

        return string;
    }

    public static Version decode(String string) {
        Version version = null;

        byte majorRevision = 0;
        byte minorRevision = 0;
        byte maintenanceRevision = 0;
        byte updateRevision = 0;
        String build = null;

        String revision;
        int i = string.indexOf("-");
        if (i == -1) {
            revision = string;
        } else {
            revision = string.substring(0, i);
            build = string.substring(i + 1);
        }

        String[] revisionNumbers = revision.split("\\.");

        if (revisionNumbers.length > 0) {
            majorRevision = Byte.parseByte(revisionNumbers[0]);

            if (revisionNumbers.length > 1) {
                minorRevision = Byte.parseByte(revisionNumbers[1]);

                if (revisionNumbers.length > 2) {
                    String[] maintenanceRevisionNumbers = revisionNumbers[2].split("_");

                    if (maintenanceRevisionNumbers.length > 0) {
                        maintenanceRevision = Byte.parseByte(maintenanceRevisionNumbers[0]);

                        if (maintenanceRevisionNumbers.length > 1) {
                            updateRevision = Byte.parseByte(maintenanceRevisionNumbers[1]);
                        }
                    }
                }
            }

            version = new Version(majorRevision, minorRevision, maintenanceRevision,
                updateRevision, build);
        }

        return version;
    }
}
