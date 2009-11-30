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

<!-- Translates a demo XML document into an HTML demo page -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="release"/>
    <xsl:param name="root"/>

    <xsl:output method="html" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <!-- <document> gets translated into an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                    Pivot <xsl:value-of select="properties/title"/> Demo
                </title>
                <link rel="stylesheet" href="demo.css" type="text/css"/>
                <script xmlns="" type="text/javascript" src="http://java.com/js/deployJava.js"></script>
                <xsl:if test="boolean(properties/full-screen)">
                    <style type="text/css">
                        * {
                            padding: 0px;
                            margin: 0px;
                        }

                        html, body {
                            height: 100%;
                            overflow: hidden;
                        }
                    </style>
                </xsl:if>
                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <xsl:apply-templates select="body"/>
            </body>
        </html>
    </xsl:template>

    <!-- <head> content gets passed through -->
    <xsl:template match="head">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- <body> content gets passed through -->
    <xsl:template match="body">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- <root> gets resolved to the 'root' XSL parameter -->
    <xsl:template match="root">
        <xsl:value-of select="$root"/>
    </xsl:template>

    <!-- <application> gets translated to a JavaScript block that launches the applet -->
    <xsl:template match="application">
        <script type="text/javascript">
            var attributes = {
                code:"org.apache.pivot.wtk.BrowserApplicationContext$HostApplet",
                width:"<xsl:value-of select="@width"/>",
                height:"<xsl:value-of select="@height"/>"
            };

            <xsl:for-each select="attributes/*">
                attributes.<xsl:value-of select="name(.)"/> = '<xsl:value-of select="."/>';
            </xsl:for-each>

            var libraries = [];
            <xsl:apply-templates select="libraries/library">
                <xsl:with-param name="signed" select="boolean(@signed)"/>
            </xsl:apply-templates>
            attributes.archive = libraries.join(",");

            var parameters = {
                codebase_lookup:false,
                java_arguments:"-Dsun.awt.noerasebackground=true -Dsun.awt.erasebackgroundonresize=true",
                application_class_name:"<xsl:value-of select="@class"/>"
            };

            <xsl:if test="startup-properties">
                var startupProperties = [];

                <xsl:for-each select="startup-properties/*">
                    startupProperties.push("<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>

                parameters.startup_properties = startupProperties.join("&amp;");
            </xsl:if>

            deployJava.runApplet(attributes, parameters, "1.6");
        </script>
    </xsl:template>

    <!-- <library> gets translated to JavaScript that adds a JAR file to a JavaScript array -->
    <xsl:template match="library">
        <xsl:param name="signed"/>

        <xsl:choose>
            <xsl:when test=".='wtk'">
                <xsl:variable name="jar">
                    <xsl:value-of select="'lib/pivot-wtk-'"/>
                    <xsl:value-of select="$release"/>
                    <xsl:if test="$signed">
                        <xsl:value-of select="'.signed'"/>
                    </xsl:if>
                    <xsl:value-of select="'.jar'"/>
                </xsl:variable>
                libraries.push('<xsl:value-of select="$jar"/>');
                <xsl:variable name="jar-terra">
                    <xsl:value-of select="'lib/pivot-wtk-'"/>
                    <xsl:value-of select="$release"/>
                    <xsl:value-of select="'.terra'"/>
                    <xsl:if test="$signed">
                        <xsl:value-of select="'.signed'"/>
                    </xsl:if>
                    <xsl:value-of select="'.jar'"/>
                </xsl:variable>
                libraries.push('<xsl:value-of select="$jar-terra"/>');
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="jar">
                    <xsl:value-of select="'lib/pivot-'"/>
                    <xsl:value-of select="."/>
                    <xsl:value-of select="'-'"/>
                    <xsl:value-of select="$release"/>
                    <xsl:if test="$signed">
                        <xsl:value-of select="'.signed'"/>
                    </xsl:if>
                    <xsl:value-of select="'.jar'"/>
                </xsl:variable>
                libraries.push('<xsl:value-of select="$jar"/>');
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Everything else gets passed through -->
    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
