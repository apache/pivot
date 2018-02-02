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
    <xsl:import href="project.xsl"/>

    <!-- Parameters (overrideable) -->
    <xsl:param name="version"/>

    <!-- <document> gets translated into an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                  <xsl:value-of select="properties/title"/>
                  <xsl:text> | </xsl:text>
                  <xsl:value-of select="$project/title"/>
                </title>

                <link rel="stylesheet" href="tutorial.css" type="text/css"/>
                <link rel="stylesheet" href="tutorial_print.css" type="text/css" media="print"/>

                <script xmlns="" type="text/javascript" src="http://java.com/js/deployJava.js"/>

                <xsl:comment>NOTE: Syntax highlighting script is LGPL</xsl:comment>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js"/>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJava.js"/>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushXml.js"/>
                <script xmlns="" type="text/javascript" src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJScript.js"/>
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

                <xsl:variable name="index" select="document('../www/index.xml')/document"/>
                <xsl:variable name="id" select="@id"/>
                <xsl:variable name="index-node" select="$index//document-item[@id=$id]"/>

                <xsl:variable name="next-id">
                    <xsl:choose>
                        <xsl:when test="$index-node/document-item">
                            <xsl:value-of select="$index-node/document-item/@id"/>
                        </xsl:when>
                        <xsl:when test="$index-node/following::document-item">
                            <xsl:value-of select="$index-node/following::document-item/@id"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:variable>

                <xsl:if test="string-length($next-id)!=0">
                    <p>
                        <xsl:text>Next: </xsl:text>
                        <a href="{$next-id}.html">
                            <xsl:variable name="tutorial"
                                select="document(concat('../www/', $next-id, '.xml'))/document"/>
                            <xsl:value-of select="$tutorial/properties/title"/>
                        </a>
                    </p>
                </xsl:if>
            </body>
        </html>
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
            <xsl:variable name="signed" select="libraries/@signed"/>
            <xsl:for-each select="libraries/library">
                <xsl:text><![CDATA[libraries.push("]]></xsl:text>
                <xsl:value-of select="'lib/pivot-'"/>
                <xsl:value-of select="."/>
                <xsl:value-of select="'-'"/>
                <xsl:value-of select="$version"/>
                <xsl:if test="$signed">
                    <xsl:value-of select="'.signed'"/>
                </xsl:if>
                <xsl:value-of select="'.jar'"/>
                <xsl:text><![CDATA[");
                ]]></xsl:text>
            </xsl:for-each>

            <xsl:choose>
                <xsl:when test='$signed'>
                    libraries.push("lib/svgSalamander-tiny.signed.jar");
                </xsl:when>
                <xsl:otherwise>
                    libraries.push("lib/svgSalamander-tiny.jar");
                </xsl:otherwise>
            </xsl:choose>

            attributes.archive = libraries.join(",");

            <!-- Base parameters -->
            var parameters = {
                codebase_lookup:false,
                application_class_name:'<xsl:value-of select="@class"/>'
            };

            <!-- Java arguments -->
            var javaArguments = ["-Dsun.awt.noerasebackground=true",
                "-Dsun.awt.erasebackgroundonresize=true"];

            <xsl:if test="java-arguments">
                <xsl:for-each select="java-arguments/*">
                    javaArguments.push("-D<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
            </xsl:if>

            parameters.java_arguments = javaArguments.join(" ");

            <!-- Startup properties -->
            <xsl:if test="startup-properties">
                var startupProperties = [];
                <xsl:for-each select="startup-properties/*">
                    startupProperties.push("<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
                parameters.startup_properties = startupProperties.join("&amp;");
            </xsl:if>

            <!-- System properties -->
            <xsl:if test="system-properties">
                var systemProperties = [];
                <xsl:for-each select="system-properties/*">
                    systemProperties.push("<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
                parameters.system_properties = systemProperties.join("&amp;");
            </xsl:if>

            deployJava.runApplet(attributes, parameters, "1.7");
        </script>
    </xsl:template>

    <!-- <source> gets translated to pre-formatted source code block -->
    <xsl:template match="source">
        <xsl:element name="pre">
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="@type">
                        <xsl:text>brush:</xsl:text>
                        <xsl:value-of select="@type"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>brush</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="@line-numbers='false'">
                    <xsl:text>;gutter:false</xsl:text>
                </xsl:if>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
