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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import org.junit.Test;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.VoteResult;


public class VoteResultTest {
    @Test
    public void test1() {
        ArrayList<Vote> votes = new ArrayList<>();
        votes.add(Vote.APPROVE);
        votes.add(Vote.DEFER);
        votes.add(Vote.DENY);

        // These are the expected results as each vote is tallied
        ArrayList<Vote> results = new ArrayList<>();
        results.add(Vote.APPROVE);
        results.add(Vote.DEFER);
        results.add(Vote.DENY);

        VoteResult result = new VoteResult();
        Iterator<Vote> resultIter = results.iterator();

        for (Vote vote : votes) {
            result.tally(vote);
            assertEquals(result.get(), resultIter.next());
        }
    }

}
