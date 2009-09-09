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

create_tag() {
    if [ -z "$@" ]; then
        echo "Usage: `basename $0` tag"
        echo
        exit 1
    fi

    tag=$1

    ## Check that we're in an SVN repository folder
    svn info &> /dev/null
    if [ $? -ne 0 ]; then
        echo "Error: Current folder is not managed by SVN"
        echo
        exit 1
    fi

    ## Check that we're at the root of the Pivot repository
    pivot_root="https://svn.apache.org/repos/asf/incubator/pivot"
    svn_url=`svn info | sed -n 's/URL: //p'`
    if [ "$svn_url" != "$pivot_root" ]; then
        echo "Error: Current folder is not the repository root"
        echo "  Repository Root: $pivot_root"
        echo "  Current:         $svn_url"
        echo
        exit 1
    fi

    ## Get latest from SVN
    printf "%-*s" 50 "Getting latest code from SVN..."
    svn up &> /dev/null
    if [ $? -ne 0 ]; then
        echo "error"
        echo "SVN update failed"
        echo
        exit 1
    else
        echo "done"
    fi

    ## Verify that the tag name isn't already taken
    if [ -e tags/$tag ]; then
        echo "Error: tags/$tag already exists"
        echo
        exit 1
    fi

    ## Copy the trunk to the tag
    printf "%-*s" 50 "Creating $tag tag..."
    svn cp trunk tags/$tag &> /dev/null
    if [ $? -ne 0 ]; then
        echo "error"
        echo "Tag creation failed"
        echo
        exit 1
    else
        echo "done"
    fi

    ## Remove hidden files and folders from the release
    printf "%-*s" 50 "Removing hidden files and folders from tag..."
    find tags/$tag -name \.\* | grep -v \.svn | xargs svn rm &> /dev/null
    if [ $? -ne 0 ]; then
        echo "error"
        echo "SVN remove failed"
        echo
        exit 1
    else
        svn up &> /dev/null
        if [ $? -ne 0 ]; then
            echo "error"
            echo "SVN remove failed"
            echo
            exit 1
        fi

        echo "done"
    fi


    ## Remove project folder from the release
    printf "%-*s" 50 "Removing project folder from tag..."
    svn rm tags/$tag/project &> /dev/null
    if [ $? -ne 0 ]; then
        echo "error"
        echo "SVN remove failed"
        echo
        exit 1
    else
        svn up &> /dev/null
        if [ $? -ne 0 ]; then
            echo "error"
            echo "SVN remove failed"
            echo
            exit 1
        fi

        echo "done"
    fi

    echo

    printf "Submit tag (y/n)? [y] "
    read submit
    if [[ "$submit" == "" || "$submit" == "y" || "$submit" == "Y" ]]; then
        svn ci -m "Created $tag tag" tags/$tag
    fi

    echo
}

create_tag $*
