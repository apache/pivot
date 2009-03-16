package pivot.core.test;

import pivot.util.Version;

public class VersionTest {
    public static void main(String[] args) {
        Version version = Version.decode(System.getProperty("java.version"));
        System.out.println(version);

        Version version_1_5_0_13 = new Version(1, 5, 0, 13);
        System.out.println(version.compareTo(version_1_5_0_13));

        Version version_1_4_1_5 = new Version(1, 4, 1, 5);
        System.out.println(version.compareTo(version_1_4_1_5));

        Version version_1_6_0_10 = new Version(1, 6, 0, 10);
        System.out.println(version.compareTo(version_1_6_0_10));

        Version maxVersion = new Version(0x7f, 0xff, 0xff, 0xff);
        System.out.println(maxVersion.compareTo(version));
    }
}
