<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<html>
<head>
<title>Pivot Demos - Web Start</title>
<link rel="stylesheet" href="demo.css" />
<link rel="icon" href="favicon.png" type="image/png" />
<link rel="shortcut icon" href="favicon.png" type="image/png" />
<style>
body {
    font-family:Verdana;
    font-size:11px;
}

p.caption {
    font-style:italic;
    padding-top:0px;
}

p.command {
    font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
    background-color:#E7E5DC;
    padding-top:12px;
    padding-left:24px;
    padding-bottom:12px;
    padding-right:24px;
}

pre.snippet {
    padding:6px;
    border:#E7E5DC solid 1px;
    font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
    font-size:1em;
}

tt {
    font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
}

table {
    font-size:11px;
}

td {
    vertical-align:middle;
}

img {
    vertical-align: middle;
}
</style>
<script src="http://java.com/js/deployJava.js"></script>
<%@ include file="jnlp_common.jsp" %>
</head>
<body>
<h2>Web Start Demos</h2>
<p>This page contains a collection of Pivot demos. All demos require Java 6 or greater.</p>
<p>The demos listed on this page are all executed via Java Web Start. To launch the demos within the browser as applets, click <a href="index.html">here</a>.</p>

<h3>"Kitchen Sink"</h3>
<p>Demonstrates a number of commonly used Pivot components.</p>
<p><img src="kitchen_sink.png"></p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>kitchen_sink.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
<td>
Signed:
<script>
var url = "<%= codebase %>kitchen_sink.signed.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
<td>
Signed, with custom color scheme:
<script>
var url = "<%= codebase %>kitchen_sink.custom_colors.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Component Explorer</h3>
<p>Allows a user to browse the properties, styles, and events provided by each standard
Pivot component.</p>
<p><img src="component_explorer.png"></p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>component_explorer.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Stock Tracker</h3>
<p>An example of a simple but practical "real world" application built using
Pivot. Monitors stock quotes provided by <a href="http://finance.yahoo.com/">Yahoo!
Finance</a>.</p>
<p><img src="stock_tracker.png"></p>

<table>
<tr>
<td>
Signed:
<script>
var url = "<%= codebase %>stock_tracker.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
<td>
Signed, in French locale (fr):
<script>
var url = "<%= codebase %>stock_tracker_fr.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>iTunes Search</h3>
<p>Simple application that allows a user to run search queries against the
iTunes Music Store and presents the results in a table view.</p>
<p><img src="itunes_search.png"></p>

<table>
<tr>
<td>
Signed:
<script>
var url = "<%= codebase %>itunes_search.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>JSON Viewer</h3>
<p>Allows users to visually browse a JSON structure using a TreeView component.</p>
<p><img src="json_viewer.png"></p>

<table>
<tr>
<td>
Signed:
<script>
var url = "<%= codebase %>json_viewer.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>XML Viewer</h3>
<p>Allows users to visually browse an XML document using a TreeView component.</p>
<p><img src="xml_viewer.png"></p>

<table>
<tr>
<td>
Signed:
<script>
var url = "<%= codebase %>xml_viewer.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Scripting</h3>
<p>Simple example of a Pivot application written using JavaScript.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>scripting.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>File Drag &amp; Drop</h3>
<p>Demonstrates Pivot's support for drag and drop.</p>

<table>
<tr>
<td>
Signed:
<script>
var url = "<%= codebase %>file_drag_drop.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Table Row Editor</h3>
<p>Example of a table row editor that uses a "Family Feud"-like flip effect to
edit rows.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>table_row_editor.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Animated Clock</h3>
<p>Demonstrates Pivot's MovieView component, which is used to present a clock
constructed using Pivot's drawing API.</p>
<p><img src="clock.png"></p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>animated_clock.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Large Data Sets</h3>
<p>Demonstrates Pivot's ability to handle large data sets of up to 1,000,000
rows.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>large_data.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>RSS Feed</h3>
<p>Demonstrates how to build a simple RSS client in Pivot.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>rss_feed.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Decorators</h3>
<p>Demonstrates the use of "decorators" in Pivot. Decorators allow a developer to
attach additional presentation to components, such as drop shadows, reflections,
image effects, etc. This example shows a window with a reflection decorator and
a frame with a fade decorator.</p>
<p><img src="decorators.png"></p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>decorators.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Fixed Column Table</h3>
<p>Explains how to create a table with fixed columns in Pivot. Fixed columns are
handy when displaying tables with many columns.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>fixed_column_table.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

<h3>Multiple Selection</h3>
<p>Demonstrates Pivot's use of ranges to maintain selection state in a ListView
component. This is more efficient than maintaining a list of individual
selected indexes.</p>

<table>
<tr>
<td>
Unsigned:
<script>
var url = "<%= codebase %>multiselect.jnlp";
deployJava.createWebStartLaunchButton(url, '1.6');
</script>
</td>
</tr>
</table>
<hr/>

</body>
</html>
