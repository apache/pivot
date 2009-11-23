<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <!-- Output method -->
    <xsl:output method="xml" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <xsl:template match="head">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                    Pivot <xsl:value-of select="properties/title"/> Demo
                </title>
                <link rel="stylesheet" href="demo.css" type="text/css"/>
                <script src="http://java.com/js/deployJava.js"></script>
                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <xsl:apply-templates select="body"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="demo">
        <script type="text/javascript">
            var attributes = {code:"org.apache.pivot.wtk.BrowserApplicationContext$HostApplet"};

            <xsl:for-each select="attributes/*">
                attributes.<xsl:value-of select="name(.)"/> = '<xsl:value-of select="."/>';
            </xsl:for-each>

            var libraries = [];
            <xsl:for-each select="libraries/library">
                <xsl:choose>
                    <xsl:when test=".='core'">
                        libraries.push('<xsl:value-of select="$jar-core"/>');
                    </xsl:when>
                    <xsl:when test=".='wtk'">
                        libraries.push('<xsl:value-of select="$jar-wtk"/>');
                        libraries.push('<xsl:value-of select="$jar-wtk-terra"/>');
                    </xsl:when>
                    <xsl:when test=".='web'">
                        libraries.push('<xsl:value-of select="$jar-web"/>');
                    </xsl:when>
                    <xsl:when test=".='demos'">
                        libraries.push('<xsl:value-of select="$jar-demos"/>');
                    </xsl:when>
                    <xsl:when test=".='tutorials'">
                        libraries.push('<xsl:value-of select="$jar-tutorials"/>');
                    </xsl:when>
                    <xsl:when test=".='tools'">
                        libraries.push('<xsl:value-of select="$jar-tools"/>');
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
            attributes.archive = libraries.join(",");

            var parameters = {
                codebase_lookup:false,
                java_arguments:"-Dsun.awt.noerasebackground=true -Dsun.awt.erasebackgroundonresize=true",
                application_class_name:"<xsl:value-of select="@class"/>"
            };

            <xsl:for-each select="parameters/*">
                parameters.<xsl:value-of select="name(.)"/> = '<xsl:value-of select="."/>';
            </xsl:for-each>

            deployJava.runApplet(attributes, parameters, "1.6");
        </script>
    </xsl:template>

    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
