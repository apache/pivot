package pivot.util;

import java.io.Serializable;

public class Version implements Comparable<Version>, Serializable {
    public static final long serialVersionUID = 0;

    private byte majorRevision = 0;
    private byte minorRevision = 0;
    private byte maintenanceRevision = 0;
    private byte updateRevision = 0;

    public Version(int majorRevision, int minorRevision, int maintenanceRevision,
        int updateRevision) {
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
    }

    public byte getMajorRevision() {
        return majorRevision;
    }

    public byte getMinorRevision() {
        return minorRevision;
    }

    public byte getMaintenanceRevision() {
        return maintenanceRevision;
    }

    public byte getUpdateRevision() {
        return updateRevision;
    }

    public int getNumber() {
        int number = (((int)majorRevision) & 0xff) << (8 * 3)
            | (((int)minorRevision) & 0xff) << (8 * 2)
            | (((int)maintenanceRevision) & 0xff) << (8 * 1)
            | (((int)updateRevision) & 0xff) << (8 * 0);

        return number;
    }

    public int compareTo(Version version) {
        return (getNumber() - version.getNumber());
    }

    @Override
    public boolean equals(Object object) {
        return (compareTo((Version)object) == 0);
    }

    @Override
    public int hashCode() {
        return getNumber();
    }

    @Override
    public String toString() {
        String string = majorRevision
            + "." + minorRevision
            + "." + maintenanceRevision
            + "_" + updateRevision;

        return string;
    }

    public static Version decode(String string) {
        byte majorRevision = 0;
        byte minorRevision = 0;
        byte maintenanceRevision = 0;
        byte updateRevision = 0;

        String[] revisionNumbers = string.split("\\.");

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
        }

        return new Version(majorRevision, minorRevision, maintenanceRevision,
            updateRevision);
    }
}
