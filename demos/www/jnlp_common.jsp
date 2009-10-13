<%
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

// response.setHeader("Cache-Control", "no-cache");
// response.setHeader("Pragma", "no-cache");
// response.setDateHeader("Expires", 0);

String requestURL = request.getRequestURL().toString();
int lastSlash = requestURL.lastIndexOf('/');
String codebase = "";
String href = "";
if (requestURL != null) {
    if (lastSlash < 0) {
        lastSlash = 0;
    }

    codebase = requestURL.substring(0, lastSlash + 1);
    if ((lastSlash + 1) < requestURL.length()) {
        href = requestURL.substring(lastSlash + 1);
    }

}

%>
