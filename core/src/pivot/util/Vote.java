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
package pivot.util;

/**
 * Enumeration representing a vote. Votes are often used to determine the
 * result of an event preview.
 *
 * @author gbrown
 */
public enum Vote {
    APPROVE(true),
    DENY(false),
    DEFER(false);

    private boolean approved;

    private Vote(boolean approved) {
        this.approved = approved;
    }

    public Vote tally(Vote vote) {
        Vote tally;

        switch(vote) {
            case APPROVE: {
                tally = this;
                break;
            }

            case DENY: {
                tally = vote;
                break;
            }

            case DEFER: {
                tally = (this == DENY) ? this : vote;
                break;
            }

            default: {
                throw new IllegalArgumentException();
            }
        }

        return tally;
    }

    public boolean isApproved() {
        return approved;
    }

    public static Vote decode(String value) {
        return valueOf(value.toUpperCase());
    }
}
