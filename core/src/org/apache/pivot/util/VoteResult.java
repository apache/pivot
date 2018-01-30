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

/**
 * An object holding the result of a {@link Vote#tally} that can be
 * used with a <tt>forEach</tt> or lambda expressionm, where the value
 * used during the iteration must be final or effectively final.
 */
public class VoteResult {
    /** The latest running vote tally. */
    private Vote result;

    /**
     * Construct one of these and set the initial vote value to
     * {@link Vote#APPROVE} (which is the usual starting point).
     */
    public VoteResult() {
        this(Vote.APPROVE);
    }

    /**
     * Construct one of these and set the initial vote to the given value.
     *
     * @param initialVote The initial vote value.
     */
    public VoteResult(Vote initialVote) {
        result = initialVote;
    }

    /**
     * Tally the internal vote with the next vote in line.
     * <p> The internal <tt>Vote</tt> is updated with the
     * result of the vote tally.
     *
     * @param vote The next vote to tally against all the
     * previous ones.
     * @see Vote#tally
     */
    public void tally(Vote vote) {
        result = result.tally(vote);
    }

    /**
     * @return The final tallied <tt>Vote</tt> value.
     */
    public Vote get() {
        return result;
    }

}
