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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read the file(s) given on the command line, which are presumed to be
 * the output of the "check styles", and generate a summary of the results.
 */
public final class StyleErrors {
    /** Private constructor because we only use static methods here. */
    private StyleErrors() {
    }

    /**
     * A summary object holding one type of error and the number of times
     * it was encountered.
     */
    private static class Info {
        /** The error class from the check styles configuration. */
        private String errorClass;
        /** The final count of how many times this error was encountered. */
        private Integer count;

        /** Construct one of these with the given information.
         * @param errClass The checkstyle error.
         * @param c The final count of these errors.
         */
        Info(final String errClass, final Integer c) {
            this.errorClass = errClass;
            this.count = c;
        }
        /** @return The saved checkstyle error name. */
        String getErrorClass() {
            return errorClass;
        }
        /** @return The final count of this error type. */
        Integer getCount() {
            return count;
        }
    }

    /**
     * A comparator that sorts first by count and then by the error class name.
     */
    private static Comparator<Info> comparator = new Comparator<Info>() {
        @Override
        public int compare(final Info o1, final Info o2) {
            // Order first by count, then by class name
            int c1 = o1.count.intValue();
            int c2 = o2.count.intValue();
            if (c1 == c2) {
                return o1.errorClass.compareTo(o2.errorClass);
            } else {
                return Integer.signum(c1 - c2);
            }
        }
    };

    /** Pattern used to parse each input line. */
    private static final Pattern LINE_PATTERN =
        Pattern.compile("^\\[[A-Z]+\\]\\s+(([a-zA-Z]\\:)?([^:]+))(\\:[0-9]+\\:)([0-9]+\\:)?\\s+(.+)\\s+(\\[[a-zA-Z]+\\])$");
    /** The group in the {@link #LINE_PATTERN} that contains the file name. */
    private static final int FILE_NAME_GROUP = 1;
    /** The group in the {@link #LINE_PATTERN} that contains the checkstyle error name. */
    private static final int CLASS_NAME_GROUP = 7;
    /** A format string used to output all the information in a uniform manner. */
    private static final String FORMAT = "%1$-32s%2$5d%n";
    /** Format string used to print the underlines. */
    private static final String UNDER_FORMAT = "%1$-32s%2$5s%n";
    /** The set of unique file names found in the list. */
    private static Set<String> fileNameSet = new HashSet<>();
    /** For each type of checkstyle error, the name and running count for each. */
    private static Map<String, Integer> counts = new TreeMap<>();
    /** At the end of each file, the list used to sort by count and name. */
    private static List<Info> sortedList = new ArrayList<>();

    /**
     * The main method, executed from the command line, which reads through each file
     * and processes it.
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        for (String arg : args) {
            int lineNo = 0;
            counts.clear();
            sortedList.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(arg)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNo++;
                    Matcher m = LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        String fileName = m.group(FILE_NAME_GROUP);
                        fileNameSet.add(fileName);
                        String errorClass = m.group(CLASS_NAME_GROUP);
                        Integer count = counts.get(errorClass);
                        if (count == null) {
                            count = Integer.valueOf(1);
                        } else {
                            int i = count.intValue() + 1;
                            count = Integer.valueOf(i);
                        }
                        counts.put(errorClass, count);
                    } else if (line.equals("Starting audit...") || line.equals("Audit done.")) {
                        continue;
                    } else {
                        System.err.println("Line " + lineNo + ". Doesn't match the pattern.");
                        System.err.println("\t" + line);
                    }
                }
            } catch (IOException ioe) {
                System.err.println("Error reading the \"" + arg + "\" file: " + ioe.getMessage());
            }
            int total = 0;
            for (String key : counts.keySet()) {
                Integer count = counts.get(key);
                total += count.intValue();
                Info info = new Info(key, count);
                sortedList.add(info);
            }
            Collections.sort(sortedList, comparator);
            for (Info info : sortedList) {
                System.out.format(FORMAT, info.getErrorClass(), info.getCount());
            }
            System.out.format(UNDER_FORMAT, "----------------------------", "-----");
            System.out.format(FORMAT, "Grand Total", total);
            System.out.format(FORMAT, "Total Files With Errors", fileNameSet.size());
            System.out.println();
        }
    }

}

