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

function check_release_signatures() {
    src_release=`find . -name \*-src.tar.gz | xargs basename | sed -e 's/\.tar\.gz//'`
    bin_release=`echo $src_release | sed -e 's/-src//'`

    check_signatures $src_release.tar.gz
    check_signatures $src_release.zip
    check_signatures $bin_release.tar.gz
    check_signatures $bin_release.zip

    echo
}

function check_signatures() {
    check_asc_signature $1
    check_md5_signature $1
    check_sha_signature $1
}

function check_asc_signature() {
    printf "%-*s" 50 "$1.asc"
    gpg --verify $1.asc $1 &> /dev/null
    if [ $? -ne 0 ]; then
        echo "error"
    else
        echo "ok"
    fi
}

function check_md5_signature() {
    printf "%-*s" 50 "$1.md5"
    md5_1=`md5sum $1 | sed -e 's/ .*//'`
    md5_2=`cat $1.md5`
    if [ "$md5_1" != "$md5_2" ]; then
        echo "error"
    else
        echo "ok"
    fi
}

function check_sha_signature() {
    printf "%-*s" 50 "$1.sha"
    sha_1=`sha1sum $1 | sed -e 's/ .*//'`
    sha_2=`cat $1.sha`
    if [ "$sha_1" != "$sha_2" ]; then
        echo "error"
    else
        echo "ok"
    fi
}

check_release_signatures
