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
package org.apache.pivot.tests.issues;

import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;

public final class Pivot751WithoutGUI implements Application {

    /** Hide utility class constructor. */
    private Pivot751WithoutGUI() { }

    public static void main(String[] args) {
        final TabPane tabPane = new TabPane();

        tabPane.getTabPaneSelectionListeners().add(new TabPaneSelectionListener() {
            @Override
            public void selectedIndexChanged(TabPane tabPaneArgument, int previousSelectedIndex) {
                System.out.println(String.format("tabs     : %-16d actual selectedIndex    : %d",
                    tabPaneArgument.getTabs().getLength(), tabPaneArgument.getSelectedIndex()));
                System.out.println(String.format("indirect : %-16s 'previousSelectedIndex' : %d",
                    (previousSelectedIndex == tabPaneArgument.getSelectedIndex()),
                    previousSelectedIndex));
            }
        });

        System.out.println("Empty TabPane sequence");
        System.out.println(String.format("tabs     : %-16d actual selectedIndex    : %d",
            tabPane.getTabs().getLength(), tabPane.getSelectedIndex()));

        System.out.println("\nAdding first Label to the sequence");
        tabPane.getTabs().add(new Label("1"));

        System.out.println("\nAdding second Label at the end of the sequence");
        tabPane.getTabs().add(new Label("2"));

        System.out.println("\nInserting third Label at the start of the sequence");
        tabPane.getTabs().insert(new Label("3"), 0);

        System.out.println("\nAdding a fourth Label at the end of the sequence");
        tabPane.getTabs().add(new Label("4"));

        System.out.println("\nExplicitly select the last tab");
        tabPane.setSelectedIndex(3);

        System.out.println("\nRemoving the first 2 Labels from the start of the sequence");
        tabPane.getTabs().remove(0, 2);

        System.out.println("\nRemoving the tab at the end of the sequence");
        tabPane.getTabs().remove(1, 1);
    }

}
