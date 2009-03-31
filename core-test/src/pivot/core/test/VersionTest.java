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
