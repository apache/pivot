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

<!-- Translates a tutorial XML document into an HTML tutorial page -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="release"/>

    <xsl:output method="html" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <!-- <document> gets translated into an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                    Pivot <xsl:value-of select="properties/title"/> Tutorial
                </title>
                <link rel="stylesheet" href="tutorial.css" type="text/css"/>
                <script xmlns="" type="text/javascript" src="http://java.com/js/deployJava.js"/>

                <!-- NOTE: Syntax highlighting script is LGPL -->
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js"/>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJava.js"/>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushXml.js"/>
                <link type="text/css" rel="stylesheet" href="http://alexgorbatchev.com/pub/sh/current/styles/shCore.css"/>
                <link type="text/css" rel="stylesheet" href="http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css"/>
                <script type="text/javascript">
                    SyntaxHighlighter.all();
                </script>

                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <h1><xsl:value-of select="properties/title"/></h1>
                <xsl:apply-templates select="body"/>
                <p>
                    Next:
                    <a href="{properties/next}.html">
                      <!-- TODO Pull title from properties of next document -->
                      <xsl:value-of select="properties/next"/>
                    </a>
                </p>
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

    <!-- <application> gets translated to a JavaScript block that launches the applet -->
    <xsl:template match="application">
        <script type="text/javascript">
            <!-- Base attributes -->
            var attributes = {
                code:'org.apache.pivot.wtk.BrowserApplicationContext$HostApplet',
                width:'<xsl:value-of select="@width"/>',
                height:'<xsl:value-of select="@height"/>'
            };

            <!-- Additional attributes -->
            <xsl:for-each select="attributes/*">
                attributes.<xsl:value-of select="name(.)"/> = "<xsl:value-of select="."/>";
            </xsl:for-each>

            <!-- Archive attribute -->
            var libraries = [];
            <xsl:variable name="signed" select="@signed"/>
            <xsl:for-each select="libraries/library">
                <xsl:text><![CDATA[libraries.push("]]></xsl:text>
                <xsl:value-of select="'lib/pivot-'"/>
                <xsl:value-of select="."/>
                <xsl:value-of select="'-'"/>
                <xsl:value-of select="$release"/>
                <xsl:if test="$signed">
                    <xsl:value-of select="'.signed'"/>
                </xsl:if>
                <xsl:value-of select="'.jar'"/>
                <xsl:text><![CDATA[");
                ]]></xsl:text>
            </xsl:for-each>
            attributes.archive = libraries.join(",");

            <!-- Base parameters -->
            var parameters = {
                codebase_lookup:false,
                java_arguments:'-Dsun.awt.noerasebackground=true -Dsun.awt.erasebackgroundonresize=true',
                application_class_name:'<xsl:value-of select="@class"/>'
            };

            <!-- Startup properties -->
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

    <!-- <source> gets translated to pre-formatted source code block -->
    <xsl:template match="source">
        <xsl:element name="pre">
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="@type='xml'">
                        <xsl:text>brush:xml</xsl:text>
                    </xsl:when>
                    <xsl:when test="@type='java'">
                        <xsl:text>brush:java</xsl:text>
                    </xsl:when>
                    <xsl:when test="@type='javascript'">
                        <xsl:text>brush:javascript</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>brush</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- Everything else gets passed through -->
    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
