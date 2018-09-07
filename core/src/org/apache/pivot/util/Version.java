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
 * Represents a version number. Version numbers are defined as: <p>
 * <i>major</i>.<i>minor</i>.<i>maintenance</i>_<i>update</i> <p> for example,
 * "JDK 1.6.0_10".
 */
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3677773163272115116L;

    private short majorRevision = 0;
    private short minorRevision = 0;
    private short maintenanceRevision = 0;
    private long updateRevision = 0;
    private String build = null;

    public Version(final int majorRevision, final int minorRevision, final int maintenanceRevision,
        final long updateRevision) {
        this(majorRevision, minorRevision, maintenanceRevision, updateRevision, null);
    }

    public Version(final int majorRevision, final int minorRevision, final int maintenanceRevision,
        final long updateRevision, final String build) {
        Utils.checkInRangeOfShort(majorRevision, "majorRevision");
        Utils.checkInRangeOfShort(minorRevision, "minorRevision");
        Utils.checkInRangeOfShort(maintenanceRevision, "maintenanceRevision");

        this.majorRevision = (short) majorRevision;
        this.minorRevision = (short) minorRevision;
        this.maintenanceRevision = (short) maintenanceRevision;
        this.updateRevision = updateRevision;
        this.build = build;
    }

    public short getMajorRevision() {
        return majorRevision;
    }

    public short getMinorRevision() {
        return minorRevision;
    }

    public short getMaintenanceRevision() {
        return maintenanceRevision;
    }

    public long getUpdateRevision() {
        return updateRevision;
    }

    public long getNumber() {
        long number = ((long) ((majorRevision) & 0xffff) << (16 * 3)
            | (long) ((minorRevision) & 0xffff) << (16 * 2)
            | (long) ((maintenanceRevision) & 0xffff) << (16 * 1))
            + updateRevision;

        return number;
    }

    @Override
    public int compareTo(final Version version) {
        return Long.compare(getNumber(), version.getNumber());
    }

    @Override
    public boolean equals(final Object object) {
        return (object instanceof Version && compareTo((Version) object) == 0);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getNumber()).hashCode();
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

    /**
     * @return A three-component string with "major.minor.maintenance".
     */
    public String simpleToString() {
        return String.format("%1$d.%2$d.%3$d",
            this.majorRevision,
            this.minorRevision,
            this.maintenanceRevision);
    }

    public static Version decode(final String string) {
        Version version = null;

        short majorRevision = 0;
        short minorRevision = 0;
        short maintenanceRevision = 0;
        long updateRevision = 0;
        String build = null;

        String revision;
        // Some "version" strings separate fields with a space
        // While Java 9 uses a new scheme where "build" uses a "+"
        String[] parts = string.split("[ +\\-]");
        if (parts.length == 1) {
            revision = string;
        } else {
            int len = parts[0].length();
            revision = string.substring(0, len);
            build = string.substring(len + 1);
        }

        String[] revisionNumbers = revision.split("\\.");

        if (revisionNumbers.length > 0) {
            majorRevision = Short.parseShort(revisionNumbers[0]);

            if (revisionNumbers.length > 1) {
                minorRevision = Short.parseShort(revisionNumbers[1]);

                if (revisionNumbers.length > 2) {
                    String[] maintenanceRevisionNumbers = revisionNumbers[2].split("[_\\-]");

                    if (maintenanceRevisionNumbers.length > 0) {
                        maintenanceRevision = Short.parseShort(maintenanceRevisionNumbers[0]);

                        if (maintenanceRevisionNumbers.length > 1) {
                            updateRevision = Long.parseLong(maintenanceRevisionNumbers[1]);
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
