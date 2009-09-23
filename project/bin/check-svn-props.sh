#!/bin/bash
###
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in
# compliance with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###

function check-svn-props() {
    for f in `find . -type f | grep -Ev '(.svn|/\.)'` ; do
        ## Get the mime type of the file
        MIME_TYPE=`file -b --mime-type $f`

        ## Extract the main type and subtype
        INDEX=`expr index "$MIME_TYPE" '/'`
        TYPE=${MIME_TYPE:0:INDEX - 1}
        SUBTYPE=${MIME_TYPE:INDEX}

        ## Get for svn:eol-style
        SVN_EOL_STYLE=`svn pg svn:eol-style $f 2>/dev/null`
        if [ -z "$SVN_EOL_STYLE" ]; then
            if [[ "$TYPE" == "text" || "$MIME_TYPE" == "application/xml" ]]; then
                echo "svn ps svn:eol-style native $f"
            fi
        fi

        ## Check for svn:mime-type
        SVN_MIME_TYPE=`svn pg svn:mime-type $f 2>/dev/null`
        if [[ "$SVN_MIME_TYPE" != "$MIME_TYPE" && "$TYPE" != "text" ]]; then
            echo "svn ps svn:mime-type $MIME_TYPE $f"
        fi

        ## Check for origination-name on images
        ORIGINATION_NAME=`svn pg origination-name $f 2>/dev/null`
        if [[ -z "$ORIGINATION_NAME" && "$TYPE" == "image" ]]; then
            echo "svn ps origination-name ?? $f"
        fi
    done
}

check-svn-props
